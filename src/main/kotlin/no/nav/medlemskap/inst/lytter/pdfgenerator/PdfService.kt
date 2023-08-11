package no.nav.medlemskap.inst.lytter.pdfgenerator

import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.*
import no.nav.medlemskap.inst.lytter.domain.Medlemskap.Companion.brukerensFørsteMEDLUnntakIPeriode
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import java.lang.IllegalStateException

class PdfService() : IkanOpprettePdf {
    val configuration = Configuration()
    val restClients = RestClientsImpl(
            configuration = configuration
    )

    companion object {
        private val secureLogger = KotlinLogging.logger("tjenestekall")
    }

    private val pdfClient = restClients.pdfGen(configuration.register.pdfGenBaseUrl)

    override suspend fun opprettPfd(
            record: MedlemskapVurdertRecord,
            medlemskapVurdering: MedlemskapVurdert
    ): ByteArray {
        val pdfRequest = mapRecordToRequestObject(medlemskapVurdering)
        secureLogger.info { "kaller PdfGenerator med følgende parameter : " + pdfRequest.toJsonPrettyString() }
        val response = pdfClient.kallPDFGenerator(record.key, pdfRequest.getstatus(), pdfRequest)
        return response
    }

    override suspend fun opprettPfd(callID: String, medlemskapVurdering: MedlemskapVurdert): ByteArray {
        val pdfRequest = mapRecordToRequestObject(medlemskapVurdering)
        secureLogger.info { "kaller PdfGenerator med følgende parameter : " + pdfRequest.toJsonPrettyString() }
        val response = pdfClient.kallPDFGenerator(callID, pdfRequest.getstatus(), pdfRequest)
        return response
    }

    fun mapRecordToRequestObject(medlemskapVurdering: MedlemskapVurdert): Response {
        return when (medlemskapVurdering.resultat.svar) {
            "JA" -> {
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
            }

            "UAVKLART" -> {
                UavklartResponse(
                        tidspunkt = medlemskapVurdering.tidspunkt,
                        fnr = medlemskapVurdering.datagrunnlag.fnr,
                        fom = medlemskapVurdering.datagrunnlag.periode.fom.toString(),
                        tom = medlemskapVurdering.datagrunnlag.periode.tom.toString(),
                        navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                        erNorskStatsborger = medlemskapVurdering.erNorskStatsborger,
                        erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger,
                        medlemskapVurdering = MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar),
                        ytelse = medlemskapVurdering.datagrunnlag.ytelse,
                        årsaker = medlemskapVurdering.resultat.årsaker,
                        statsborger = hentStatsborgerskap(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.statsborgerskap)
                )
            }

            "NEI" -> {
                val periode = medlemskapVurdering.datagrunnlag.periode
                val medlInnslag = medlemskapVurdering.datagrunnlag.medlemskap.brukerensFørsteMEDLUnntakIPeriode(periode)
                NeiResponse(
                        tidspunkt = medlemskapVurdering.tidspunkt,
                        opprettet = medlemskapVurdering.datagrunnlag.fnr,
                        ytelse = medlemskapVurdering.datagrunnlag.ytelse,
                        navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                        fnr = medlemskapVurdering.datagrunnlag.fnr,
                        fom = periode.fom.toString(),
                        tom = periode.tom.toString(),
                        MEDLfom = medlInnslag.fraOgMed.toString(),
                        MEDLtom = medlInnslag.tilOgMed.toString(),
                        lovvalgsland = medlInnslag.lovvalgsland.toString(),
                        erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger
                )
            }

            else -> throw IllegalStateException("${medlemskapVurdering.resultat.svar} er en ulovlig verdi")
        }

    }

    fun slåSammenNavn(pdlNavn: Navn): String {
        return when (pdlNavn.mellomnavn) {
            null -> "${pdlNavn.fornavn} ${pdlNavn.etternavn}"
            else -> "${pdlNavn.fornavn} ${pdlNavn.mellomnavn} ${pdlNavn.etternavn}"
        }
    }

    fun hentStatsborgerskap(statsborger: List<Statsborgerskap>): String {
        return statsborger.filter { !it.historisk }
                .map { it.landkode }
                .joinToString(" og ")
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

    data class UavklartResponse(
            val tidspunkt: String,
            val fnr: String,
            val fom: String,
            val tom: String,
            val navn: String,
            val statsborger: String,
            val ytelse: String,
            val årsaker: List<Årsak>,
            val erNorskStatsborger: Boolean,
            val erTredjelandsborger: Boolean,
            val medlemskapVurdering: vurdering
    ) : Response {
        override fun getstatus(): MedlemskapVurdering {
            return MedlemskapVurdering.UAVKLART
        }


        override fun toJsonPrettyString(): String {
            return JaksonParser().ToJson(this).toPrettyString()
        }

    }

    data class NeiResponse(
            val tidspunkt: String,
            val opprettet: String,
            val ytelse: String,
            val navn: String,
            val fnr: String,
            val fom: String,
            val tom: String,
            val MEDLfom: String,
            val MEDLtom: String,
            val lovvalgsland: String,
            val erTredjelandsborger: Boolean
    ) : Response {
        override fun getstatus(): MedlemskapVurdering {
            return MedlemskapVurdering.NEI
        }


        override fun toJsonPrettyString(): String {
            return JaksonParser().ToJson(this).toPrettyString()
        }

    }


}

interface IkanOpprettePdf {
    suspend fun opprettPfd(record: MedlemskapVurdertRecord, medlemskapVurdering: MedlemskapVurdert): ByteArray
    suspend fun opprettPfd(callID: String, medlemskapVurdering: MedlemskapVurdert): ByteArray
}

