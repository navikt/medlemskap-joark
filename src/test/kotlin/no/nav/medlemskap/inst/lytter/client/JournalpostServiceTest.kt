package no.nav.medlemskap.inst.lytter.client
import kotlinx.coroutines.GlobalScope
import no.nav.medlemskap.inst.lytter.journalpost.*
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

 fun main() {
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
     println(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.YYYY")))
     println(LocalDate.now().format(dateFormat))
    //val runner = JournalpostServiceTest()
    //runner.run()
}
class JournalpostServiceTest {

     suspend fun run(){
        val uuid = UUID.randomUUID().toString()
        val pdf = "dett er et journaldokument".toByteArray()
        val request = JournalpostRequest(
            "Automatisk vurdering: Er medlem i folketrygden pr. "+LocalDate.now().format(DateTimeFormatter.ofPattern("")),
            JournalPostType.NOTAT,
            tema = "MED",
            kanal=null,
            behandlingstema = null,
            journalfoerendeEnhet="9999",
            avsenderMottaker = null,
            Bruker(id="02066407392"),
            eksternReferanseId=uuid, //lik sykepengesøknadID (eller tilsvarden)
            sak=Fagsak(),
            listOf(
                JournalpostDokument(
                    tittel = "Automatisk vurdering: Er medlem i folketrygden pr. "+LocalDate.now(),
                    DokumentKategori.IB,
                    dokumentvarianter = listOf(
                        DokumentVariant.ArkivPDF(fysiskDokument = Base64.getEncoder().encodeToString(pdf))

                    ))))
        val service = JournalpostService()

        GlobalScope.run {
           val response =  service.lagrePdfTilJoark(uuid,request)
            println(JaksonParser().ToJson(request))
            println(response)
            val response2 =  service.lagrePdfTilJoark(uuid,request)
            println(response2)

        }


    }
}