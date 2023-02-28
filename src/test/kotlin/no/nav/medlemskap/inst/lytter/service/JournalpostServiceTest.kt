package no.nav.medlemskap.inst.lytter.service
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService_dagpenger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class JournalpostServiceTest {

    @Test
    fun `konvertering av string to date`(){
        val dateString ="2021-08-21"
        val localDate = JournalpostService().getStringAsDate(dateString)
        Assertions.assertEquals(dateString,localDate.toString())
    }
    @Test
    fun `Dagpenger skal ha eget dokument navn`(){
        val fileContent = this::class.java.classLoader.getResource("regel_19_1_sample.json").readText(Charsets.UTF_8)
        val record = MedlemskapVurdertRecord(0,0,"","","",fileContent)
        val service = JournalpostService_dagpenger()
        val response =  service.mapRecordToRequestObject(record,ByteArray(1))
        Assertions.assertEquals("UDI-tjenesten kan ikke gi et automatisk svar",response.tittel,"Tittel på dokument er ikke korrekt!")
        print(response)
    }
    @Test
    fun `Sykepenger skal ha eget dokument navn`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorgerMedMellomnavn.json").readText(Charsets.UTF_8)
        val record = MedlemskapVurdertRecord(0,0,"","","",fileContent)
        val service = JournalpostService()
        val response =  service.mapRecordToRequestObject(record,ByteArray(1))
        Assertions.assertEquals("Automatisk vurdering: Er medlem i folketrygden pr. 21.08.2021",response.tittel,"Tittel på dokument er ikke korrekt!")
        print(response)
    }
}