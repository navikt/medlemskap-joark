package no.nav.medlemskap.inst.lytter

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.pdfgenerator.MedlemskapVurdering
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import java.io.Writer

fun naisLiveness(consumeJob: Job) = embeddedServer(Netty, applicationEngineEnvironment {
    connector { port = 8080 }
    module {

        install(MicrometerMetrics) {
            registry = Metrics.registry
        }

        routing {
            get("/isAlive") {
                val pdfHealth = callPdfGen()
                val consumejobHealth = getConsuejobHealth(consumeJob)
                val health = Health(listOf(pdfHealth,consumejobHealth))
                if (consumeJob.isActive) {
                    call.respondText(JaksonParser().ToJson(health).toPrettyString(), ContentType.Text.Plain, HttpStatusCode.OK)
                } else {
                    call.respondText(JaksonParser().ToJson(health).toPrettyString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError)
                }
            }
            get("/samplePdf") {
                val pdfClient = RestClientsImpl(Configuration()).pdfGen(Configuration().register.pdfGenBaseUrl)
                val request:PdfService.Response =
                    PdfService.JaResponse(
                        "13:23:07 30.08.2021",
                        "12345678901",
                        "01.08.2021",
                        "22.08.2021",
                        "Kari Nordlending",
                        true,
                        false,
                        MedlemskapVurdering.JA
                    )

                    val response = pdfClient.kallPDFGenerator("1234",MedlemskapVurdering.JA,request)
                    call.respondBytes { response }
                }

            get("/isReady") {
                call.respondText("Ready!", ContentType.Text.Plain, HttpStatusCode.OK)
            }
            get("/metrics") {
                call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                    writeMetrics004(this, Metrics.registry)
                }
            }
        }
    }
})

fun getConsuejobHealth(consumeJob: Job): Componenthealth {
    return if (consumeJob.isActive){
        Componenthealth(Status.UP,"consumeJob",Configuration().kafkaConfig.bootstrapServers,"")
    }
    else{
        Componenthealth(Status.DOWN,"consumeJob",Configuration().kafkaConfig.bootstrapServers,"")
    }

}

suspend fun callPdfGen(): Componenthealth {
    val pdfClient = RestClientsImpl(Configuration()).pdfGen(Configuration().register.pdfGenBaseUrl)
    val pdfRequest =
        PdfService.JaResponse(
            "13:23:07 30.08.2021",
            "12345678901",
            "01.08.2021",
            "22.08.2021",
            "Kari Nordlending",
            true,
            false,
            MedlemskapVurdering.JA
        )
    return try{
        val response = pdfClient.kallPDFGenerator("1234",MedlemskapVurdering.JA,pdfRequest)
        return Componenthealth(Status.UP,"PDF-GEN",Configuration().register.pdfGenBaseUrl,"")
    }
    catch (exception:Exception){
        return Componenthealth(Status.DOWN,"PDF-GEN",Configuration().register.pdfGenBaseUrl,"Error: "+exception.message)
    }

}


suspend fun writeMetrics004(writer: Writer, registry: PrometheusMeterRegistry) {
    withContext(Dispatchers.IO) {
        kotlin.runCatching {
            TextFormat.write004(writer, registry.prometheusRegistry.metricFamilySamples())
        }
    }
}

