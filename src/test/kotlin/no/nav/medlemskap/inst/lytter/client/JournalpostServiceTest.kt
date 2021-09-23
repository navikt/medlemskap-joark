package no.nav.medlemskap.inst.lytter.client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.medlemskap.inst.lytter.journalpost.*
import java.time.LocalDate
import java.util.*

suspend fun main() {
    val runner = JournalpostServiceTest()
    runner.run()
}
class JournalpostServiceTest {

     suspend fun run(){
        val uuid = UUID.randomUUID().toString()
        val pdf = "dett er et journaldokument".toByteArray()
        val request = JournalpostRequest(
            "Automatisk vurdering: Er medlem i folketrygden pr "+LocalDate.now(),
            JournalPostType.NOTAT,
            tema = "MED",
            kanal=null,
            behandlingstema = null,
            journalfoerendeEnhet="9999",
            avsenderMottaker = null,
            Bruker(id="03067492438"),
            eksternReferanseId=uuid,
            sak=Fagsak(),
            listOf(
                JournalpostDokument(
                    tittel = "Automatisk vurdering: Er medlem i folketrygden pr "+LocalDate.now(),
                    DokumentKategori.IB,
                    dokumentvarianter = listOf(
                        DokumentVariant.ArkivPDF(fysiskDokument = Base64.getEncoder().encodeToString(pdf))

                    ))))
        val service = JournalpostService()

        GlobalScope.run {
           val response =  service.lagrePdfTilJoark(uuid,request)
            println(response)
            val response2 =  service.lagrePdfTilJoark(uuid,request)
            println(response2)

        }


    }
}