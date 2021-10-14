package no.nav.medlemskap.inst.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import java.lang.Exception


class JoarkService(
    private val configuration: Configuration,
    )
{
    val journalpostService = JournalpostService()
    companion object {
        private val log = KotlinLogging.logger { }

    }
    suspend fun handle(record : MedlemskapVurdertRecord)
    {
        if (validateRecord(record)){
            //TODO:Endre api mot pdfService og journalpostService til å ta in MedlemskapVurdert objekt og ikke medlemskapVurdertRecord
            val pdf = PdfService().opprettPfd(record)
            record.logOpprettetPdf()
            val response = journalpostService.lagrePdfTilJoark(record, pdf)
            if (response!=null){
                record.logDokumentLagretIJoark()
                //publiser til topic ZZZ
            }
            else{
                record.logDokumentIkkeLagretIJoark()
            }

        }
        else{
            record.logFiltrert()
        }
    }
    private fun validateRecord(record: MedlemskapVurdertRecord) :Boolean{
        try{
            val medlemskapVurdering = JaksonParser().parseToObject(record.json)
            return medlemskapVurdering.resultat.svar=="JA"
        }
        catch (e: Exception){
            return false
        }
    }

    private fun MedlemskapVurdertRecord.logFiltrert() =
        JoarkService.log.warn(
            "Melding filtrert pga validering. Kun JA svar prosesseres videre ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )

    private fun MedlemskapVurdertRecord.logOpprettetPdf() =
        JoarkService.log.info(
            "PDF opprettet - sykmeldingId: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )
    private fun MedlemskapVurdertRecord.logDokumentLagretIJoark() =
        JoarkService.log.info(
            "Dokument opprettet i Joark- sykmeldingId: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )
    private fun MedlemskapVurdertRecord.logDokumentIkkeLagretIJoark() =
        JoarkService.log.warn(
            "Dokument Ikke opprettet i Joark- sykmeldingId: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", key),
        )
}