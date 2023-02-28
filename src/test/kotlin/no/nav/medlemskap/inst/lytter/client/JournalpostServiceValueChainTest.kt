package no.nav.medlemskap.inst.lytter.client
import no.nav.medlemskap.inst.lytter.config.Configuration
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.inst.lytter.service.JoarkService

suspend fun main() {
    val runner = JournalpostServiceValueChainTest()
    runner.run()
}
class JournalpostServiceValueChainTest {

     suspend fun run(){
         val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)

         val record= MedlemskapVurdertRecord(1,1,fileContent,"test","",fileContent)
        val service = JoarkService(Configuration())
         service.handle(record)

        }


    }
