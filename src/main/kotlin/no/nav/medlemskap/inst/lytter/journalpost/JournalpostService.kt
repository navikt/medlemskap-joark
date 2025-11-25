package no.nav.medlemskap.inst.lytter.journalpost

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import mu.KotlinLogging
import no.nav.medlemskap.inst.lytter.clients.RestClientsImpl
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class JournalpostService() :IKanJournalforePDF {
    val tema = "MED"
    val behandlingstema = null
    val kanal = null
    val configuration = Configuration()
    val restClients = RestClientsImpl(
        configuration = configuration
    )
    val journalfoerendeEnhet="9999"
    val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val log = KotlinLogging.logger { }

    val joarkClient = restClients.joarkClient(configuration.register.joarkBaseUrl)

    override suspend fun lagrePdfTilJoark(callId:String, journalpostRequest: JournalpostRequest):JsonNode?{
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
                log.info("Journalpost med Nav-call-id $callId er allerede ferdigstilt. Behandler som OK.")
                //Må ha body og wrapper for å identifisere 409 i kallende funksjon
                val body = cause.response.body<JsonNode>()
                val wrapper = jacksonObjectMapper().createObjectNode()
                wrapper.set<JsonNode>("body", body)
                wrapper.put("status", 409)
                return wrapper
            }
            //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
            log.error("HTTP error i kall mot Dokarkiv: ${cause.response.status.value} ", cause)
            return null
        }
        catch (cause: Throwable) {
            cause.printStackTrace()
            log.error("Feil i kall mot Dokarkiv: ", cause)
            return null
        }
    }
    override suspend fun lagrePdfTilJoark(record : MedlemskapVurdertRecord, pdf: ByteArray):JsonNode?{
        val request = mapRecordToRequestObject(record,pdf)
        return lagrePdfTilJoark(record.key,request)

    }
    fun mapRecordToRequestObject(record : MedlemskapVurdertRecord,pdf:ByteArray): JournalpostRequest {
        val medlemskapVurdert = JaksonParser().parseToObject(record.json)
        val tittel = medlemskapVurdert.getDokTittel(dateFormat)
        val request = JournalpostRequest(
            tittel,
            JournalPostType.NOTAT,
            tema,
            kanal,
            behandlingstema,
            journalfoerendeEnhet,
            null,
            Bruker(id=medlemskapVurdert.datagrunnlag.fnr,idType="FNR"),
            eksternReferanseId="${record.key}_${Configuration().kafkaConfig.groupID}",
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

interface IKanJournalforePDF {
    suspend fun lagrePdfTilJoark(record : MedlemskapVurdertRecord,pdf: ByteArray):JsonNode?
    suspend fun lagrePdfTilJoark(callId:String,journalpostRequest: JournalpostRequest):JsonNode?
}

fun MedlemskapVurdert.getDokTittel(dateFormat:DateTimeFormatter):String {
    return when(this.finnsvar()) {
        "JA" -> "Automatisk vurdering: Er medlem i folketrygden pr. ${this.datagrunnlag.periode.fom.format(dateFormat)}"
        "NEI" -> "Automatisk vurdering - Ikke medlem i folketrygden"
        else -> "Automatisk vurdering - Medlemskapet er uavklart"
    }
}
