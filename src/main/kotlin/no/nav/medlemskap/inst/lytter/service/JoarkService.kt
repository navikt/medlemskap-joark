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
        log.info(
            teamLogs,
            "Kafka: Leser melding fra medlemskap vurdert i joark-lytter",
            kv("callId", record.key),
            kv("topic", record.topic),
            kv("partition", record.partition),
            kv("offset", record.offset)
        )

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
            record.logOpprettetPdf(medlemskapVurdering)
            log.info(
                teamLogs,
                "PDF opprettet for ${medlemskapVurdering.datagrunnlag.fnr} vedrørende ytelse : ${medlemskapVurdering.datagrunnlag.ytelse}",
                kv("callId", record.key),
                kv("fnr", medlemskapVurdering.datagrunnlag.fnr)
            )
            val response = journalpostService.lagrePdfTilJoark(record, pdf)
            if (response != null) {
                record.logDokumentLagretIJoark(medlemskapVurdering)
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
            record.logOpprettetPdf(medlemskapVurdering)
            log.info(
                teamLogs,
                "PDF opprettet for ${medlemskapVurdering.datagrunnlag.fnr} vedrørende ytelse : ${medlemskapVurdering.datagrunnlag.ytelse}",
                kv("callId", record.key),
                kv("fnr", medlemskapVurdering.datagrunnlag.fnr)
            )
            val response = journalpostService.lagrePdfTilJoark(record, pdf)
            if (response != null) {
                record.logDokumentLagretIJoark(medlemskapVurdering)
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

    private fun MedlemskapVurdertRecord.logOpprettetPdf(medlemskapVurdering: MedlemskapVurdert) =
        log.info(
            "PDF opprettet for ytelse : ${this.getYtelse()} ID: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
            kv("svar", medlemskapVurdering.finnsvar())
        )

    private fun MedlemskapVurdertRecord.logDokumentLagretIJoark(medlemskapVurdert: MedlemskapVurdert) =
        log.info(
            "Dokument opprettet for ytelse : ${this.getYtelse()} i Joark ID: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
            kv("svar", medlemskapVurdert.finnsvar())
        )

    private fun MedlemskapVurdertRecord.logDokumentIkkeLagretIJoark() =
        log.warn(
            "Dokument Ikke opprettet for ytelse : ${this.getYtelse()} ID: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )
}

fun LocalDate.TilNorskDatoformat(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return this.format(formatter)
}