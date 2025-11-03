package no.nav.medlemskap.inst.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.*
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import no.nav.medlemskap.inst.lytter.journalpost.IKanJournalforePDF
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostServiceDagpenger
import no.nav.medlemskap.inst.lytter.pdfgenerator.IkanOpprettePdf
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import org.slf4j.MarkerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class JoarkService(private val configuration: Configuration) {
    var journalpostService:IKanJournalforePDF = JournalpostService()
    var pdfService:IkanOpprettePdf = PdfService()
    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

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

            record.loggOpprettetPDF(medlemskapVurdering)

            val response = journalpostService.lagrePdfTilJoark(record, pdf)

            if (response != null) {
                record.logDokumentLagretIJoark(medlemskapVurdering)
                //publiser til topic ZZZ
            } else {
                record.loggDokumentIkkeLagretIJoark(medlemskapVurdering)
                throw RuntimeException("Teknisk feil oppstått mot Dokarkiv ved lagring av notat for callId: ${record.key} " +
                            "offset: ${record.offset}, partition: ${record.partition}, topic: ${record.topic}")
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
            //vi skal kun opprette dokumenter for JA OG NEI svar og for de som blir kalt via Kafa
            (medlemskapVurdert.finnsvar() ==  "JA" || medlemskapVurdert.finnsvar() ==  "NEI")  && medlemskapVurdert.kanal=="kafka" && !medlemskapVurdert.datagrunnlag.brukerinput.harBrukerUtfortArbeidUtenforNorge()

        } catch (e: Exception) {
            false
        }
    }
     private fun MedlemskapVurdertRecord.logFiltrert() =
        log.warn(
            "Melding filtrert pga validerings logikk ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )

    private fun MedlemskapVurdertRecord.loggOpprettetPDF(medlemskapVurdert: MedlemskapVurdert) =
        log.info(
            teamLogs,
            "PDF er generert for ytelse : ${this.getYtelse()}, id: ${key}, offsett: $offset, partition: $partition, topic: $topic",
            kv("callId", key),
            kv("fnr", medlemskapVurdert.datagrunnlag.fnr),
            kv("svar", medlemskapVurdert.finnsvar())
        )


    private fun MedlemskapVurdertRecord.logDokumentLagretIJoark(medlemskapVurdert: MedlemskapVurdert) =
        log.info(
            teamLogs,
            "Dokument opprettet i Joark for ytelse : ${this.getYtelse()}, id: ${key}, offsett: $offset, partition: $partition, topic: $topic",
            kv("callId", key),
            kv("fnr", medlemskapVurdert.datagrunnlag.fnr),
            kv("svar", medlemskapVurdert.finnsvar())
        )

    private fun MedlemskapVurdertRecord.loggDokumentIkkeLagretIJoark(medlemskapVurdert: MedlemskapVurdert) =
        log.info(
            teamLogs,
            "Dokument ikke opprettet i Joark for ytelse : ${this.getYtelse()}, id: ${key}, offsett: $offset, partition: $partition, topic: $topic",
            kv("callId", key),
            kv("fnr", medlemskapVurdert.datagrunnlag.fnr)
        )
}

fun LocalDate.TilNorskDatoformat(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return this.format(formatter)
}