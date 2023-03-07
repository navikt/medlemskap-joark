package no.nav.medlemskap.inst.lytter.pdfgenerator

import io.github.resilience4j.retry.Retry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.medlemskap.inst.lytter.http.runWithRetryAndMetrics

class PdfGeneratorClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val retry: Retry? = null
) {

    suspend fun kallPDFGenerator(callId: String, medlemskapVurdering: vurdering, pdfRequest: PdfService.Response): ByteArray {
        return runWithRetryAndMetrics("MEDL-OPPSLAG-PDFGEN", "vurdermedlemskap", retry) {
            httpClient.post {
                url("${baseUrl}${medlemskapVurdering.url}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                setBody(pdfRequest)
            }.body()
        }
    }
}
