package no.nav.medlemskap.inst.lytter.clients


import no.nav.medlemskap.clients.azuread.AzureAdClient
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfGeneratorClient
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.config.retryRegistry
import no.nav.medlemskap.inst.lytter.http.cioHttpClient
import no.nav.medlemskap.inst.lytter.journalpost.JoarkClient


interface RestClients {
    fun pdfGen(endpointBaseUrl: String) :PdfGeneratorClient
    fun joarkClient(endpointBaseUrl: String) :JoarkClient
}
class RestClientsImpl(configuration: Configuration) :RestClients
{
    private val medlRetry = retryRegistry.retry("MEDL-OPPSLAG-PDFGEN")
    private val httpClient = cioHttpClient

    override fun pdfGen(endpointBaseUrl: String) = PdfGeneratorClient(endpointBaseUrl, httpClient, medlRetry)
    override fun joarkClient(endpointBaseUrl: String) = JoarkClient(endpointBaseUrl, httpClient, medlRetry, AzureAdClient(Configuration())
    )
}


