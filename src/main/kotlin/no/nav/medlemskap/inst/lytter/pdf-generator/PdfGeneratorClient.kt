package no.nav.medlemskap.inst.lytter.`pdf-generator`

import io.github.resilience4j.retry.Retry
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.medlemskap.inst.lytter.http.runWithRetryAndMetrics

class PdfGeneratorClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val retry: Retry? = null
) {

    suspend fun kallPDFGenerator(callId: String, medlemskapVurdering: MedlemskapVurdering, json: String): String {
        return runWithRetryAndMetrics("MEDL-OPPSLAG", "vurdermedlemskap", retry) {
            httpClient.post {
                url("${baseUrl}${medlemskapVurdering.url}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                body = json.trimIndent()
            }
        }
    }
}
