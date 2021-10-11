package no.nav.medlemskap.inst.lytter.journalpost

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.features.*
import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class JournalpostService() {
    val dokumentnavnJA  = "Automatisk vurdering: Er medlem i folketrygden pr "
    val dokumentnavnNEI = "Automatisk vurdering: Er unntatt fra medlemskap i folketrygden pr  "
    val dokumentnavnUAVKLART = "Automatisk vurdert til «Uavklart»: Medlemskapet i folketrygden pr %dato kan ikke vurderes automatisk"
    val tema = "MED"
    val behandlingstema = null
    val kanal = null
    val configuration = Configuration()
    val restClients = RestClientsImpl(
        configuration = configuration
    )
    val journalfoerendeEnhet="9999"
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    companion object {
        private val log = KotlinLogging.logger { }

    }
    val joarkClient = restClients.joarkClient(configuration.register.joarkBaseUrl)

    suspend fun lagrePdfTilJoark(callId:String,journalpostRequest: JournalpostRequest):JsonNode?{
        try{
            val response = joarkClient.journalfoerDok(callId,journalpostRequest)
            val journalpostId = response.get("journalpostId").asText()
            val ferdigstilt = response.get("journalpostferdigstilt").asBoolean(false)

            if (!ferdigstilt){
                log.warn("Dokument lagret, men er ikke ferdigstilt!. Kontroller journalpost $journalpostId")
            }
            return response
        }
        catch (cause: ResponseException){
            if (cause.response.status.value == 409) {
                log.warn("Duplikat journalpost. Dropper melding med navCallID $callId", cause)
            }
            //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
            return null
        }
        catch (cause: Throwable) {
            cause.printStackTrace()
            log.error("Feil i kall mot Dokarkiv: ", cause)
            return null
        }
    }
    suspend fun lagrePdfTilJoark(record : MedlemskapVurdertRecord,pdf: ByteArray):JsonNode?{
        val request = mapRecordToRequestObject(record,pdf)
        return lagrePdfTilJoark(record.key,request)

    }
    fun mapRecordToRequestObject(record : MedlemskapVurdertRecord,pdf:ByteArray): JournalpostRequest {
        val medlemskapVurdert = JaksonParser().parseToObject(record.json)
        val tittel = dokumentnavnJA+medlemskapVurdert.datagrunnlag.periode.fom.format(dateFormat)
        val request = JournalpostRequest(
            tittel,
            JournalPostType.NOTAT,
            tema,
            kanal,
            behandlingstema,
            journalfoerendeEnhet,
            null,
            Bruker(id=medlemskapVurdert.datagrunnlag.fnr,idType="FNR"),
            eksternReferanseId=record.key,
            Fagsak(),
            listOf(
                JournalpostDokument(
                    tittel = tittel,
                    dokumentKategori = null,
                    dokumentvarianter = listOf(
                        DokumentVariant.ArkivPDF(fysiskDokument = Base64.getEncoder().encodeToString(pdf))

           ))))

       return request
    }
    fun getStringAsDate(string:String):LocalDate{
        return LocalDate.parse(string)
    }
}