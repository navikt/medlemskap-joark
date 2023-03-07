package no.nav.medlemskap.inst.lytter.client
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import no.nav.medlemskap.inst.lytter.journalpost.IKanJournalforePDF
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostRequest
import no.nav.medlemskap.inst.lytter.pdfgenerator.IkanOpprettePdf
import no.nav.medlemskap.inst.lytter.service.JoarkService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ValueChainTest {
    @Test
    fun test() {
        runBlocking {
            val fileContent =
                this::class.java.classLoader.getResource("ValideringTestPerson_kafka.json").readText(Charsets.UTF_8)

            val record = MedlemskapVurdertRecord(1, 1, fileContent, "test", "kafka", fileContent)
            val service = JoarkService(Configuration())
            val jps = MockJournalpostService()
            service.journalpostService = jps
            service.pdfService = MockPdfGenerator()


            service.handle(record)
            Assertions.assertNotNull(jps.record)
            val medlemskapVurdert = JaksonParser().parseToObject(record.json)
            println(medlemskapVurdert)
        }
    }

    class MockJournalpostService() : IKanJournalforePDF {
         var record:MedlemskapVurdertRecord? = null
         var journalpostRequest: JournalpostRequest? = null

        override suspend fun lagrePdfTilJoark(record: MedlemskapVurdertRecord, pdf: ByteArray): JsonNode? {
            this.record=record
            return ObjectMapper().createObjectNode();
        }

        override suspend fun lagrePdfTilJoark(callId: String, journalpostRequest: JournalpostRequest): JsonNode? {
            this.journalpostRequest=journalpostRequest
            return ObjectMapper().createObjectNode();
        }
    }
    class MockPdfGenerator():IkanOpprettePdf {
        override suspend fun opprettPfd(
            record: MedlemskapVurdertRecord,
            medlemskapVurdering: MedlemskapVurdert
        ): ByteArray {
           return "TEST TEST TEST".toByteArray()
        }

        override suspend fun opprettPfd(callID: String, medlemskapVurdering: MedlemskapVurdert): ByteArray {
            return "TEST TEST TEST".toByteArray()
        }
    }
}