package no.nav.medlemskap.inst.lytter.clients


import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfGeneratorClient
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.config.retryRegistry
import no.nav.medlemskap.inst.lytter.http.cioHttpClient


interface RestClients {
    fun pdfGen(endpointBaseUrl: String) :PdfGeneratorClient
}
class RestClientsImpl(configuration: Configuration) :RestClients
{
    private val medlRetry = retryRegistry.retry("MEDL-OPPSLAG-PDFGEN")
    private val httpClient = cioHttpClient

    override fun pdfGen(endpointBaseUrl: String) = PdfGeneratorClient(endpointBaseUrl, httpClient, medlRetry)
}


