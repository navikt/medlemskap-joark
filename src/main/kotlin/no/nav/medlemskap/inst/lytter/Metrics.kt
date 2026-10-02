package no.nav.medlemskap.inst.lytter

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry

object Metrics {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, PrometheusRegistry.defaultRegistry, Clock.SYSTEM)

    fun incReceivedTotal(count: Int = 1) =
        receivedTotal.increment(count.toDouble())

    fun incProcessedTotal(count: Int = 1) =
        processedTotal.increment(count.toDouble())

    fun incSuccessfulPenPosts(count: Int = 1) =
        successfulJoarkPosts.increment(count.toDouble())

    fun incReceivedKilde(kilde: String, count: Int = 1) =
        counter("medlemskap_joark_lytte_received_kilde", "Mottatte meldinger per kilde", "kilde", kilde)
            .increment(count.toDouble())

    fun incFailedJoarkPosts(cause: String, count: Int = 1) =
        counter("medlemskap_joark_lytte_failed_joark_posts_counter", "Feilende meldinger sendt til joark", "cause", cause)
            .increment(count.toDouble())

    private val receivedTotal: Counter =
        counter("medlemskap_joark_lytter_received", "Totalt mottatte inst meldinger")
    private val processedTotal: Counter =
        counter("medlemskap_joark_lytte_processed_counter", "Totalt prosesserte meldinger")
    private val successfulJoarkPosts: Counter =
        counter("medlemskap_joark_lytte_successful_joark_posts_counter", "Vellykede meldinger sendt til joark")

    private fun counter(name: String, description: String, vararg tags: String): Counter =
        Counter.builder(name)
            .description(description)
            .tags(*tags)
            .register(registry)

    fun clientTimer(service: String?, operation: String?): Timer =
        Timer.builder("client_calls_latency")
            .tags("service", service ?: "UKJENT", "operation", operation ?: "UKJENT")
            .description("latency for calls to other services")
            .publishPercentileHistogram()
            .register(Metrics.globalRegistry)

    fun clientCounter(service: String?, operation: String?, status: String): io.micrometer.core.instrument.Counter =
        io.micrometer.core.instrument.Counter
            .builder("client_calls_total")
            .tags("service", service ?: "UKJENT", "operation", operation ?: "UKJENT", "status", status)
            .description("counter for failed or successful calls to other services")
            .register(Metrics.globalRegistry)

}
