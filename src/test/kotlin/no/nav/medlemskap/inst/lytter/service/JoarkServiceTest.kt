package no.nav.medlemskap.inst.lytter.service
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.ytelserSomKanGenererePDF
import no.nav.medlemskap.inst.lytter.pdfgenerator.MedlemskapVurdering
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class JoarkServiceTest {

    @Test
    fun `mapping til JaResponse objekt for status Ja`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)
        val pdfService = PdfService()
        val jaRequest = pdfService.mapRecordToRequestObject(JaksonParser().parseToObject(fileContent))
        Assertions.assertTrue(jaRequest is PdfService.JaResponse)
        if (jaRequest is PdfService.JaResponse){
            Assertions.assertTrue(jaRequest.fnr==("19026500128"))
            Assertions.assertTrue(jaRequest.erTredjelandsborger)
            Assertions.assertFalse(jaRequest.erNorskStatsborger)
            Assertions.assertTrue(jaRequest.medlemskapVurdering==MedlemskapVurdering.JA)

        }
    }

    @Test
    fun `validering av record`() {
        val fileContent = this::class.java.classLoader.getResource("ValideringTestPerson.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)

        Assertions.assertFalse(JoarkService(Configuration()).skalOpprettePDF(medlemskapVurdering))
    }
    @Test
    fun `validering av record med Kafka endepunkt`() {
        val fileContent = this::class.java.classLoader.getResource("ValideringTestPerson_kafka.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)

        Assertions.assertTrue(JoarkService(Configuration()).skalOpprettePDF(medlemskapVurdering))
    }
}