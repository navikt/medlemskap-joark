package no.nav.medlemskap.inst.lytter.service

import mu.KotlinLogging

import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.domain.getYtelse
import no.nav.medlemskap.inst.lytter.domain.ytelserSomKanGenererePDF
import no.nav.medlemskap.inst.lytter.journalpost.IKanJournalforePDF
import no.nav.medlemskap.inst.lytter.pdfgenerator.IkanOpprettePdf
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostServiceDagpenger
import java.lang.Exception


class JoarkService(
    private val configuration: Configuration,
) {
    var journalpostService:IKanJournalforePDF = JournalpostService()
    var pdfService:IkanOpprettePdf = PdfService()
    companion object {
        val log = KotlinLogging.logger { }
        private val secureLogger = KotlinLogging.logger("tjenestekall")

    }

    suspend fun handle(record: MedlemskapVurdertRecord) {
        val medlemskapVurdering = JaksonParser().parseToObject(record.json)
        when (medlemskapVurdering.datagrunnlag.ytelse){
         "SYKEPENGER" ->handleSykepengeRecord(medlemskapVurdering, record)
         //"DAGPENGER" -> handleDagppengeRecord(medlemskapVurdering,record)
            else -> log.warn("Ytelsen ${medlemskapVurdering.datagrunnlag.ytelse} er ikke støttet. Ingen dokument opprettet i JOARK")
        }

    }

    private suspend fun handleSykepengeRecord(
        medlemskapVurdering: MedlemskapVurdert,
        record: MedlemskapVurdertRecord
    ) {
        if (skalOpprettePDF(medlemskapVurdering)) {
            //TODO:Endre api mot pdfService og journalpostService til å ta in MedlemskapVurdert objekt og ikke medlemskapVurdertRecord
            val pdf = pdfService.opprettPfd(record.key, medlemskapVurdering)
            record.logOpprettetPdf()
            secureLogger.info(
                "PDF opprettet for ${medlemskapVurdering.datagrunnlag.fnr} vedrørende ytelse : ${medlemskapVurdering.datagrunnlag.ytelse}",
                kv("callId", record.key),
                kv("fnr", medlemskapVurdering.datagrunnlag.fnr)
            )
            val response = journalpostService.lagrePdfTilJoark(record, pdf)
            if (response != null) {
                record.logDokumentLagretIJoark()
                //publiser til topic ZZZ
            } else {
                record.logDokumentIkkeLagretIJoark()
            }

        } else {
            record.logFiltrert()
        }
    }

    suspend fun handleDagppengeRecord(
        medlemskapVurdering: MedlemskapVurdert,
        record: MedlemskapVurdertRecord
    ) {
        val journalpostService:IKanJournalforePDF = JournalpostServiceDagpenger()
        val handler = DagpengeHandler(pdfService,journalpostService)
        if (handler.skalOpprettePDF(medlemskapVurdering)) {
            //TODO:Endre api mot pdfService og journalpostService til å ta in MedlemskapVurdert objekt og ikke medlemskapVurdertRecord
            val pdf =pdfService.opprettPfd(record.key, medlemskapVurdering)
            record.logOpprettetPdf()
            secureLogger.info(
                "PDF opprettet for ${medlemskapVurdering.datagrunnlag.fnr} vedrørende ytelse : ${medlemskapVurdering.datagrunnlag.ytelse}",
                kv("callId", record.key),
                kv("fnr", medlemskapVurdering.datagrunnlag.fnr)
            )
            val response = journalpostService.lagrePdfTilJoark(record, pdf)
            if (response != null) {
                record.logDokumentLagretIJoark()
                //publiser til topic ZZZ
            } else {
                record.logDokumentIkkeLagretIJoark()
            }

        } else {
            record.logFiltrert()
        }
    }

    fun skalOpprettePDF(medlemskapVurdering: MedlemskapVurdert): Boolean {
        when (medlemskapVurdering.datagrunnlag.ytelse){
            "SYKEPENGER" -> return validateRecord(medlemskapVurdering) && medlemskapVurdering.datagrunnlag.ytelse in ytelserSomKanGenererePDF
            "DAGPENGER" -> return medlemskapVurdering.resultat.svar=="UAVKLART"
            else -> return validateRecord(medlemskapVurdering) && medlemskapVurdering.datagrunnlag.ytelse in ytelserSomKanGenererePDF
        }
    }

    private fun validateRecord(medlemskapVurdert: MedlemskapVurdert): Boolean {
        return try {
            //vi skal kun opprette dokumenter for JA svar og for de som blir kalt via Kafa
            medlemskapVurdert.resultat.svar == "JA" && medlemskapVurdert.kanal=="kafka" && !medlemskapVurdert.datagrunnlag.brukerinput.arbeidUtenforNorge
        } catch (e: Exception) {
            false
        }
    }
     private fun MedlemskapVurdertRecord.logFiltrert() =
        log.warn(
            "Melding filtrert pga validerings logikk ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )

    private fun MedlemskapVurdertRecord.logOpprettetPdf() =
        log.info(
            "PDF opprettet for ytelse : ${this.getYtelse()} ID: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )

    private fun MedlemskapVurdertRecord.logDokumentLagretIJoark() =
        log.info(
            "Dokument opprettet for ytelse : ${this.getYtelse()} i Joark ID: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )

    private fun MedlemskapVurdertRecord.logDokumentIkkeLagretIJoark() =
        log.warn(
            "Dokument Ikke opprettet for ytelse : ${this.getYtelse()} ID: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )
}