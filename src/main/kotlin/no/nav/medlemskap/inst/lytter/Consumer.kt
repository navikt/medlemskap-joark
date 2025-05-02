package no.nav.medlemskap.inst.lytter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.config.KafkaConfig
import no.nav.medlemskap.inst.lytter.config.Environment
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.service.JoarkService
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class Consumer(
    environment: Environment,
    private val config: KafkaConfig = KafkaConfig(environment),
    private val consumer: KafkaConsumer<String, String> = config.createConsumer(),
) {
    companion object {
        private val log = KotlinLogging.logger { }

    }

    val joarkService = JoarkService(Configuration())

    init {
        consumer.subscribe(listOf(config.topic))
    }

    fun pollMessages(): List<MedlemskapVurdertRecord> = //listOf("Message A","Message B","Message C")

        consumer.poll(Duration.ofSeconds(4))
            .map {
                MedlemskapVurdertRecord(
                    it.partition(),
                    it.offset(),
                    it.value(),
                    it.key(),
                    it.topic(),
                    it.value()
                )
            }

    fun flow(): Flow<List<MedlemskapVurdertRecord>> =
        flow {
            while (true) {
                emit(pollMessages())
                delay(Duration.ofSeconds(1))
            }
        }.onEach {
            it.forEach { record ->
                if (Configuration().register.persistence_enabled == "Ja") {
                    joarkService.handle(record)
                } else {
                    log.warn(
                        "Melding filtrert ut. Første måned skal ingen dokumenter lagres ${record.key}, offsett: ${record.offset}, partiotion: ${record.partition}, topic: ${record.topic}",
                        StructuredArguments.kv("callId", record.key),
                    )
                }
            }
        }.onEach {
            try {
                consumer.commitSync()
            } catch (e: CommitFailedException) {
                log.error { "Commit feilet med feilmeldingen: ${e.message}" }
            }
        }.onEach {
            Metrics.incProcessedTotal(it.count())
        }

}