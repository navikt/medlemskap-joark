package no.nav.medlemskap.inst.lytter.pdfgenerator

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
        val pdfRequest = mapRecordToRequestObject(record.json)
        val response = pdfClient.kallPDFGenerator(record.key,pdfRequest.getstatus(),pdfRequest.toJsonPrettyString())
        return response
    }

    fun mapRecordToRequestObject(json: String): Response {

        val medlemskapVurdering = JaksonParser().parseToObject(json)
        if (medlemskapVurdering.resultat.svar=="JA"){
            return JaResponse(
                LocalDate.now().toString(),
                "12345678901", //TODO: få in fnr i grunnlag
                medlemskapVurdering.datagrunnlag.periode.fom.toString(),
                medlemskapVurdering.datagrunnlag.periode.tom.toString(),
                "Kari Nordlending", //TODO: få in navn i grunnlag
                medlemskapVurdering.erNorskStatsborger,
                medlemskapVurdering.erTredjelandsBorger,
                MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar)
            )

            }
            else{
            return UavklartResponse()
        }

    }

    interface Response{
        fun getstatus():MedlemskapVurdering
        fun toJsonPrettyString():String

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

        override fun toJsonPrettyString(): String {
            return JaksonParser().ToJson(this).toPrettyString()
        }

    }
    class UavklartResponse(): Response {
        override fun getstatus(): MedlemskapVurdering {
            return MedlemskapVurdering.UAVKLART
        }

        override fun toJsonPrettyString(): String {
            return JaksonParser().ToJson(this).toPrettyString()
        }

    }

}

