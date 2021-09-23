package no.nav.medlemskap.clients.azuread

import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import no.nav.medlemskap.inst.lytter.clients.Token
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.http.apacheHttpClient


class AzureAdClient(private val configuration: Configuration) {

    suspend fun hentTokenScopetMotJoark(): Token {
        val formUrlEncode = listOf(
            "client_id" to configuration.azureAd.clientId,
            "scope" to "api://${configuration.register.joarkClientId}/.default",
            "client_secret" to configuration.azureAd.clientSecret,
            "grant_type" to "client_credentials"
        ).formUrlEncode()

        return apacheHttpClient.post {
            url(configuration.azureAd.tokenEndpoint)
            body = TextContent(formUrlEncode, ContentType.Application.FormUrlEncoded)
        }
    }
}
