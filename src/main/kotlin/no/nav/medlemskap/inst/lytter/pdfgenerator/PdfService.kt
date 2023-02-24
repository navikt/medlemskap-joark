package no.nav.medlemskap.inst.lytter.pdfgenerator

import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.domain.Navn
import no.nav.medlemskap.inst.lytter.domain.Årsak
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser

class PdfService():IkanOpprettePdf {
    val configuration = Configuration()
    val restClients = RestClientsImpl(
        configuration = configuration
    )

    companion object {
        private val secureLogger = KotlinLogging.logger("tjenestekall")
    }

    private val pdfClient = restClients.pdfGen(configuration.register.pdfGenBaseUrl)

    override suspend fun opprettPfd(record: MedlemskapVurdertRecord, medlemskapVurdering: MedlemskapVurdert): ByteArray {
        val pdfRequest = mapRecordToRequestObject(medlemskapVurdering)
        secureLogger.info { "kaller PdfGenerator med følgende parameter : " + pdfRequest.toJsonPrettyString() }
        val response = pdfClient.kallPDFGenerator(record.key, pdfRequest.getstatus(), pdfRequest)
        return response
    }

    fun mapRecordToRequestObject(medlemskapVurdering: MedlemskapVurdert): Response {
        return if (medlemskapVurdering.resultat.svar == "JA") {
            JaResponse(
                medlemskapVurdering.tidspunkt,
                medlemskapVurdering.datagrunnlag.fnr,
                medlemskapVurdering.datagrunnlag.periode.fom.toString(),
                medlemskapVurdering.datagrunnlag.periode.tom.toString(),
                slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                medlemskapVurdering.erNorskStatsborger,
                medlemskapVurdering.erTredjelandsBorger,
                MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar)
            )

        } else {
            UavklartResponse(
                tidspunkt = medlemskapVurdering.tidspunkt,
                fnr = medlemskapVurdering.datagrunnlag.fnr,
                fom = medlemskapVurdering.datagrunnlag.periode.fom.toString(),
                tom = medlemskapVurdering.datagrunnlag.periode.tom.toString(),
                navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                erNorskStatsborger = medlemskapVurdering.erNorskStatsborger,
                erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger,
                medlemskapVurdering =  MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar),
                ytelse = medlemskapVurdering.datagrunnlag.ytelse,
                //medlemskapVurdering.resultat.
                årsaker = medlemskapVurdering.resultat.årsaker,
                statsborger = ""
            )
        }

    }

    fun slåSammenNavn(pdlNavn: Navn): String {
        return when(pdlNavn.mellomnavn) {
            null -> "${pdlNavn.fornavn} ${pdlNavn.etternavn}"
            else -> "${pdlNavn.fornavn} ${pdlNavn.mellomnavn} ${pdlNavn.etternavn}"
        }
    }

    interface Response {
        fun getstatus(): MedlemskapVurdering
        fun toJsonPrettyString(): String

    }

    data class JaResponse(
        val tidspunkt: String,
        val fnr: String,
        val fom: String,
        val tom: String,
        val navn: String,
        val erNorskStatsborger: Boolean,
        val erTredjelandsborger: Boolean,
        val medlemskapVurdering: MedlemskapVurdering
    ) : Response {
        override fun getstatus(): MedlemskapVurdering {
            return medlemskapVurdering
        }

        override fun toJsonPrettyString(): String {
            return JaksonParser().ToJson(this).toPrettyString()
        }

    }

    class Regel(
        val regelId:String,
        val avklaring:String,
        val svar:String,
        val begrunnelse:String,
        val beskrivelse:String
    ) {

    }

    data class UavklartResponse(
        val tidspunkt: String,
        val fnr: String,
        val fom: String,
        val tom: String,
        val navn: String,
        val statsborger:String,
        val ytelse:String,
        val årsaker :List<Årsak>,
        val erNorskStatsborger: Boolean,
        val erTredjelandsborger: Boolean,
        val medlemskapVurdering: MedlemskapVurdering
    ) : Response {
        override fun getstatus(): MedlemskapVurdering {
            return MedlemskapVurdering.UAVKLART
        }

        override fun toJsonPrettyString(): String {
            return JaksonParser().ToJson(this).toPrettyString()
        }

    }

}

interface IkanOpprettePdf {
    suspend fun opprettPfd(record: MedlemskapVurdertRecord, medlemskapVurdering: MedlemskapVurdert): ByteArray
    }

