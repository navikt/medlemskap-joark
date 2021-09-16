package no.nav.medlemskap.inst.lytter.jakson
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
class JaksonMapperTest {

    @Test
    fun `mapping til request objekt for status Ja med ikke norsk statsborger`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)
        val parsed = JaksonParser().parseToObject(fileContent)
        Assertions.assertNotNull(parsed)
    }
}