package no.nav.medlemskap.inst.lytter.journalpost

import com.fasterxml.jackson.databind.JsonNode
import io.github.resilience4j.retry.Retry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.medlemskap.inst.lytter.http.runWithRetryAndMetrics
import mu.KotlinLogging
import no.nav.medlemskap.clients.azuread.AzureAdClient
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser


class JoarkClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val retry: Retry? = null,
    val azureAdClient :AzureAdClient
)
{
    private val log = KotlinLogging.logger { }

     suspend fun journalfoerDok(callId: String,journalpostRequest: JournalpostRequest): JsonNode {
         val token = azureAdClient.hentTokenScopetMotJoark()
         return runWithRetryAndMetrics("MEDL-OPPSLAG-JOARK", "journalfoerDokument", retry) {

             httpClient.post {
                 url("${baseUrl}"+"/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true")
                 header(HttpHeaders.ContentType, ContentType.Application.Json)
                 header(HttpHeaders.Authorization, "Bearer ${token.token}")
                 header("Nav-Call-Id", callId)
                 header("X-Correlation-Id", callId)
                 setBody(JaksonParser().ToJson(journalpostRequest))
             }.body()

         }
     }
}


