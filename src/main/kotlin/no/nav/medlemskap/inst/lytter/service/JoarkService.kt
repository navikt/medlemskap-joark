package no.nav.medlemskap.inst.lytter.service

import mu.KotlinLogging

import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import java.lang.Exception


class JoarkService(
    private val configuration: Configuration,
    )
{
    companion object {
        private val log = KotlinLogging.logger { }

    }
    suspend fun handle(record : MedlemskapVurdertRecord)
    {
        if (validateRecord(record)){

            val pdf = PdfService().opprettPfd(record)
            record.logOpprettetPdf()
            //TODO: Lagre til Joark

        }
        else{
            record.logIkkeOpprettetPdf()
        }
    }
    private fun validateRecord(record: MedlemskapVurdertRecord) :Boolean{
        try{
            val node = JaksonParser().parse(record.json)
        }
        catch (e: Exception){
            return false
        }
        return true
    }

    private fun MedlemskapVurdertRecord.logIkkeOpprettetPdf() =
        JoarkService.log.info(
            "Pdf ikke  opprettet grunnet validering ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            //kv("callId", key),
        )

    private fun MedlemskapVurdertRecord.logOpprettetPdf() =
        JoarkService.log.info(
            "PDF opprettet - sykmeldingId: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            //kv("callId", sykepengeSoknad.sykmeldingId),
        )
}