package no.nav.medlemskap.inst.lytter.service
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class JoarkServiceTest {

    @Test
    fun `mapping til request objekt for status Ja`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)
        val pdfService = PdfService()
        val request = pdfService.mapRecordToRequest(fileContent)
        Assertions.assertTrue(request.get("erTredjelandsborger").asBoolean())
        Assertions.assertFalse(request.get("erNorskStatsborger").asBoolean())
        Assertions.assertTrue(request.get("medlemskapVurdering").asText().equals("JA"))

        Assertions.assertNotNull(request)
    }
}