package no.nav.medlemskap.inst.lytter.service
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.pdfgenerator.MedlemskapVurdering
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
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
            Assertions.assertEquals("Test Person", jaRequest.navn)
        }
    }

    @Test
    fun `mapping til JaResponse objekt for status Uavklart og konklusjon JA`(){
        val fileContent = this::class.java.classLoader.getResource("UavklartResultatMedJaKonklusjon.json").readText(Charsets.UTF_8)
        val pdfService = PdfService()
        val jaRequest = pdfService.mapRecordToRequestObject(JaksonParser().parseToObject(fileContent))
        Assertions.assertTrue(jaRequest is PdfService.JaResponse,"Feil svartypes")
        if (jaRequest is PdfService.JaResponse){
            Assertions.assertTrue(jaRequest.fnr==("18508847692"))
            Assertions.assertFalse(jaRequest.erTredjelandsborger)
            Assertions.assertTrue(jaRequest.erNorskStatsborger)
            Assertions.assertTrue(jaRequest.medlemskapVurdering==MedlemskapVurdering.JA)
            Assertions.assertEquals("ÄNGSLIG BLÅVEIS", jaRequest.navn)
        }
    }
    @Test
    fun `mapping til JaResponse objekt for status Ja med ny modell`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurderingMedKonklusjon.json").readText(Charsets.UTF_8)
        val pdfService = PdfService()
        val jaRequest = pdfService.mapRecordToRequestObject(JaksonParser().parseToObject(fileContent))
        Assertions.assertTrue(jaRequest is PdfService.JaResponse)
        if (jaRequest is PdfService.JaResponse){
            Assertions.assertTrue(jaRequest.fnr==("12345678901"))
            Assertions.assertTrue(jaRequest.medlemskapVurdering==MedlemskapVurdering.JA)
            Assertions.assertEquals("Test Person", jaRequest.navn)
        }
    }

    @Test
    fun `mapping til JaResponse objekt med mellomnavn for status Ja`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorgerMedMellomnavn.json").readText(Charsets.UTF_8)
        val pdfService = PdfService()
        val jaRequest = pdfService.mapRecordToRequestObject(JaksonParser().parseToObject(fileContent))
        Assertions.assertTrue(jaRequest is PdfService.JaResponse)
        if (jaRequest is PdfService.JaResponse){
            Assertions.assertTrue(jaRequest.fnr==("19026500128"))
            Assertions.assertTrue(jaRequest.erTredjelandsborger)
            Assertions.assertFalse(jaRequest.erNorskStatsborger)
            Assertions.assertTrue(jaRequest.medlemskapVurdering==MedlemskapVurdering.JA)
            Assertions.assertEquals("Test Test Person", jaRequest.navn)
        }
    }

    @Test
    fun `mapping til Uavklart Respons skal ha annen URL `(){
        val fileContent = this::class.java.classLoader.getResource("regel_19_1_sample.json").readText(Charsets.UTF_8)
        val pdfService = PdfService()
        val uavklart = pdfService.mapRecordToRequestObject(JaksonParser().parseToObject(fileContent))
        Assertions.assertTrue(uavklart is PdfService.UavklartResponse)
        if (uavklart is PdfService.UavklartResponse){
            Assertions.assertTrue(uavklart.medlemskapVurdering== MedlemskapVurdering.UAVKLART)
            Assertions.assertEquals("/api/v1/genpdf/medlemskapresultater/medlemskapuavklart",uavklart.medlemskapVurdering.url,"URL for uavklart er ikke korrekt")
        }
    }

    @Test
    fun `endepuntk som ikke er kafka verdikjede skal ikke generere PDF dokumenter`() {
        val fileContent = this::class.java.classLoader.getResource("ValideringTestPerson.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)

        Assertions.assertFalse(JoarkService(Configuration()).skalOpprettePDF(medlemskapVurdering))
    }


    @Test
    fun `arbeid utland skal ikke generere PDF dokumenter`() {
        val fileContent = this::class.java.classLoader.getResource("ValideringTestPersonArbeidUtlandTrue.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)

        Assertions.assertFalse(JoarkService(Configuration()).skalOpprettePDF(medlemskapVurdering))
    }
    @Test
    fun `arbeid utland false i ny modell  generere PDF dokumenter `() {
        val fileContent = this::class.java.classLoader.getResource("UavklartResultatMedJaKonklusjon.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)
        Assertions.assertTrue(JoarkService(Configuration()).skalOpprettePDF(medlemskapVurdering))
    }
}