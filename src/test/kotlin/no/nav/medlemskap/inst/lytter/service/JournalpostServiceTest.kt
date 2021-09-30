package no.nav.medlemskap.inst.lytter.service
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService
import no.nav.medlemskap.inst.lytter.pdfgenerator.MedlemskapVurdering
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class JournalpostServiceTest {

    @Test
    fun `konvertering av string to date`(){
        val dateString ="2021-08-21"
        val localDate =     JournalpostService().getStringAsDate(dateString)
        Assertions.assertEquals(dateString,localDate.toString())
    }
}