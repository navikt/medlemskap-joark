package no.nav.medlemskap.inst.lytter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.config.KafkaConfig
import no.nav.medlemskap.inst.lytter.config.Environment
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

    init {
        consumer.subscribe(listOf(config.topic))
    }

    fun pollMessages(): List<String> = //listOf("Message A","Message B","Message C")

        consumer.poll(Duration.ofSeconds(4))
            .map { it.value() }
            .map { it.toString() }
            .also {
                //Metrics.incReceivedTotal(it.count())
                //it.forEach { hendelse ->
                //    Metrics.incReceivedKilde(hendelse.kilde)
                //}
            }


            //.filter { it.kilde == Hendelse.Kilde.KDI }

    fun flow(): Flow<List<String>> =
        flow {
            while (true) {
                emit(pollMessages())
                delay(Duration.ofSeconds(5))
            }
        }.onEach {
            println("received: " + it.size)
            it.forEach { record ->
                log.info { "motatt melding: $record" }
            }
        }.onEach {
            consumer.commitAsync()
        }.onEach {
            Metrics.incProcessedTotal(it.count())
        }

}