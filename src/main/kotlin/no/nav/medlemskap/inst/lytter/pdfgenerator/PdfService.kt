package no.nav.medlemskap.inst.lytter.pdfgenerator

import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.*
import no.nav.medlemskap.inst.lytter.domain.MedlInnslag.Companion.brukerensFørsteMEDLUnntakIPeriode
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import no.nav.medlemskap.inst.lytter.service.TilNorskDatoformat

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
        return if (medlemskapVurdering.resultat.svar == "JA") {
            val brukerSpørsmålArbeidUtlandNy: Boolean =
                medlemskapVurdering.datagrunnlag.brukerinput.utfortAarbeidUtenforNorge != null
            val brukerSpørsmålOppholdUtenforNorge: Boolean =
                medlemskapVurdering.datagrunnlag.brukerinput.oppholdUtenforNorge != null
            val brukerSpørsmålOppholdUtenforEØS: Boolean =
                medlemskapVurdering.datagrunnlag.brukerinput.oppholdUtenforEos != null
            val brukerSpørsmålOppholdstillatelse: Boolean =
                medlemskapVurdering.datagrunnlag.brukerinput.oppholdstilatelse != null
            JaResponse(
                tidspunkt = medlemskapVurdering.tidspunkt,
                fnr = medlemskapVurdering.datagrunnlag.fnr,
                fom = medlemskapVurdering.datagrunnlag.periode.fom.TilNorskDatoformat(),
                tom = medlemskapVurdering.datagrunnlag.periode.tom.TilNorskDatoformat(),
                navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                erNorskStatsborger = medlemskapVurdering.erNorskStatsborger,
                erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger,
                medlemskapVurdering = MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar),
                brukerSpørsmålArbeidUtlandGammel = true,
                brukerSpørsmålArbeidUtlandNy = brukerSpørsmålArbeidUtlandNy,
                brukerSpørsmålOppholdUtenforNorge = brukerSpørsmålOppholdUtenforNorge,
                brukerSpørsmålOppholdUtenforEØS = brukerSpørsmålOppholdUtenforEØS,
                brukerSpørsmålOppholdstillatelse = brukerSpørsmålOppholdstillatelse

            )

        } else {
            if (medlemskapVurdering.konklusjon.isNotEmpty() && medlemskapVurdering.konklusjon.first().status == Svar.JA) {
                val brukerSpørsmålArbeidUtlandNy: Boolean =
                    medlemskapVurdering.datagrunnlag.brukerinput.utfortAarbeidUtenforNorge != null
                val brukerSpørsmålOppholdUtenforNorge: Boolean =
                    medlemskapVurdering.datagrunnlag.brukerinput.oppholdUtenforNorge != null
                val brukerSpørsmålOppholdUtenforEØS: Boolean =
                    medlemskapVurdering.datagrunnlag.brukerinput.oppholdUtenforEos != null
                val brukerSpørsmålOppholdstillatelse: Boolean =
                    medlemskapVurdering.datagrunnlag.brukerinput.oppholdstilatelse != null
                return JaResponse(
                    tidspunkt = medlemskapVurdering.tidspunkt,
                    fnr = medlemskapVurdering.datagrunnlag.fnr,
                    fom = medlemskapVurdering.datagrunnlag.periode.fom.TilNorskDatoformat(),
                    tom = medlemskapVurdering.datagrunnlag.periode.tom.TilNorskDatoformat(),
                    navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                    erNorskStatsborger = medlemskapVurdering.erNorskStatsborger,
                    erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger,
                    medlemskapVurdering = MedlemskapVurdering.valueOf(medlemskapVurdering.konklusjon.first().status.name),
                    brukerSpørsmålArbeidUtlandGammel = true,
                    brukerSpørsmålArbeidUtlandNy = brukerSpørsmålArbeidUtlandNy,
                    brukerSpørsmålOppholdUtenforNorge = brukerSpørsmålOppholdUtenforNorge,
                    brukerSpørsmålOppholdUtenforEØS = brukerSpørsmålOppholdUtenforEØS,
                    brukerSpørsmålOppholdstillatelse = brukerSpørsmålOppholdstillatelse
                )
            }
            if (medlemskapVurdering.resultat.svar == "NEI") {
                val periode = medlemskapVurdering.datagrunnlag.periode
                val medlInnslag = medlemskapVurdering.datagrunnlag.medlemskap.brukerensFørsteMEDLUnntakIPeriode(periode)
                return NeiResponse(
                    tidspunkt = medlemskapVurdering.tidspunkt,
                    opprettet = medlemskapVurdering.datagrunnlag.fnr,
                    ytelse = medlemskapVurdering.datagrunnlag.ytelse,
                    navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                    fnr = medlemskapVurdering.datagrunnlag.fnr,
                    fom = periode.fom.TilNorskDatoformat(),
                    tom = periode.tom.TilNorskDatoformat(),
                    medlfom = medlInnslag.fraOgMed.TilNorskDatoformat(),
                    medltom = medlInnslag.tilOgMed.TilNorskDatoformat(),
                    lovvalgsland = medlInnslag.lovvalgsland.toString(),
                    erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger
                )

            }
            val uavklartResponse = UavklartResponse(
                tidspunkt = medlemskapVurdering.tidspunkt,
                fnr = medlemskapVurdering.datagrunnlag.fnr,
                fom = medlemskapVurdering.datagrunnlag.periode.fom.TilNorskDatoformat(),
                tom = medlemskapVurdering.datagrunnlag.periode.tom.TilNorskDatoformat(),
                navn = slåSammenNavn(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.navn.first()),
                erNorskStatsborger = medlemskapVurdering.erNorskStatsborger,
                erTredjelandsborger = medlemskapVurdering.erTredjelandsBorger,
                medlemskapVurdering = MedlemskapVurdering.valueOf(medlemskapVurdering.resultat.svar),
                ytelse = medlemskapVurdering.datagrunnlag.ytelse,
                årsaker = medlemskapVurdering.resultat.årsaker,
                statsborger = hentStatsborgerskap(medlemskapVurdering.datagrunnlag.pdlpersonhistorikk.statsborgerskap)
            )
            return uavklartResponse
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
        val medlemskapVurdering: MedlemskapVurdering,
        val brukerSpørsmålArbeidUtlandGammel: Boolean = true,
        val brukerSpørsmålArbeidUtlandNy: Boolean = false,
        val brukerSpørsmålOppholdUtenforNorge: Boolean = false,
        val brukerSpørsmålOppholdUtenforEØS: Boolean = false,
        val brukerSpørsmålOppholdstillatelse: Boolean = false,
    ) : Response {
        override fun getstatus(): MedlemskapVurdering {
            return medlemskapVurdering
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
        val medlfom: String,
        val medltom: String,
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

}

interface IkanOpprettePdf {
    suspend fun opprettPfd(record: MedlemskapVurdertRecord, medlemskapVurdering: MedlemskapVurdert): ByteArray
    suspend fun opprettPfd(callID: String, medlemskapVurdering: MedlemskapVurdert): ByteArray
}

