package no.nav.medlemskap.inst.lytter

import no.nav.medlemskap.inst.lytter.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

fun main(args: Array<String>) {
    val config:KafkaConfig = KafkaConfig(System.getenv())
    val consumer: KafkaConsumer<String, String> = config.createConsumer()
    val medlemskapVurdertTopic = "medlemskap.medlemskap-vurdert"

    consumer.subscribe(listOf(medlemskapVurdertTopic))
    consumer.seekToBeginning(consumer.assignment())

    val pollTimeout = Duration.ofSeconds(2)
    while (true) {
        val records = consumer.poll(pollTimeout)

        if (!records.isEmpty) {
            records.map { transform(it) }
            records.forEach {
                handleMessage(it)
            }
            consumer.commitSync()
        } else {
            println("ingen meldinger funnet på kø")
        }
    }
}

fun transform(it: ConsumerRecord<String, String>?) {

}

fun handleMessage(it: ConsumerRecord<String, String>?) {
    val topic = it?.topic()
    val partition = it?.partition()
    val offset = it?.offset()

    println("topic: $topic , partition: $partition, offset: $offset")
}
