package no.nav.medlemskap.inst.lytter.pdfgenerator

import com.fasterxml.jackson.databind.JsonNode

import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import java.time.LocalDate

class PdfService()
{
    val configuration = Configuration()
    val restClients = RestClientsImpl(
        configuration = configuration
    )
    private val pdfClient = restClients.pdfGen(configuration.register.pdfGenBaseUrl)

    suspend fun opprettPfd(record: MedlemskapVurdertRecord):ByteArray{

        val pdfRequest = mapRecordToRequest(record.json)
        val status = getStatusFromJson(pdfRequest)
        val response = pdfClient.kallPDFGenerator(record.key,status,pdfRequest.toPrettyString())
        return response
    }

    private fun getStatusFromJson(json: JsonNode): MedlemskapVurdering {
    return MedlemskapVurdering.JA
    }

    fun mapRecordToRequest(json:String):JsonNode{

        val medlemskapVurdering = JaksonParser().parseToObject(json)
        return JaksonParser().ToJson(
            JaResponse(
                LocalDate.now().toString(),
                "12345678901", //TODO: få in fnr i grunnlag
                medlemskapVurdering.datagrunnlag.periode.fom.toString(),
                medlemskapVurdering.datagrunnlag.periode.tom.toString(),
                "Kari Nordlending", //TODO: få in navn i grunnlag
                medlemskapVurdering.erNorskStatsborger,
                medlemskapVurdering.erTredjelandsBorger,
                MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar))
        )
    }

    interface Response{
        fun getstatus():MedlemskapVurdering

    }

    data class JaResponse(val tidspunkt:String,
                          val fnr:String,
                          val fom:String,
                          val tom:String,
                          val navn:String,
                          val erNorskStatsborger:Boolean,
                          val erTredjelandsborger:Boolean,
                          val medlemskapVurdering:MedlemskapVurdering
    )
        : Response {
        override fun getstatus(): MedlemskapVurdering {
            return medlemskapVurdering
        }

    }
    class NeiResponse(): Response {
        override fun getstatus(): MedlemskapVurdering {
            TODO("Not yet implemented")
        }

    }
    class UavklartResponse(): Response {
        override fun getstatus(): MedlemskapVurdering {
            TODO("Not yet implemented")
        }

    }

}

