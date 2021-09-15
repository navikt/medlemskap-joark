package no.nav.medlemskap.inst.lytter.pdfgenerator

import com.fasterxml.jackson.databind.JsonNode

import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser

class PdfService()
{
    val configuration = Configuration()
    val restClients = RestClientsImpl(
        configuration = configuration
    )
    private val pdfClient = restClients.pdfGen(configuration.register.pdfGenBaseUrl)

    suspend fun opprettPfd(record: MedlemskapVurdertRecord):ByteArray{

        val json = mapRecordToRequest(JaksonParser().parse(record.json))
        val status = getStatusFromJson(json)
        val response = pdfClient.kallPDFGenerator(record.key,status,json.toPrettyString())
        return response
    }

    private fun getStatusFromJson(json: JsonNode): MedlemskapVurdering {
    return MedlemskapVurdering.Ja
    }

    fun mapRecordToRequest(json:JsonNode):JsonNode{
        val tidspunkt = json.get("tidspunkt").asText()
        val resultat = json.get("resultat")
        val datagrunnlag = json.get("datagrunnlag")
        val fom = datagrunnlag.get("periode").get("fom").asText()
        val tom = datagrunnlag.get("periode").get("tom").asText()


        return JaksonParser().ToJson(JaResponse(tidspunkt,"12345678901",fom,tom,"Kari Nordlending",true,true,MedlemskapVurdering.Ja))
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

