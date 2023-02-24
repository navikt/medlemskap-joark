package no.nav.medlemskap.inst.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.ktor.util.reflect.*
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PdfServiceTest {
    @Test
    fun testKorrektMapping(){
        val fileContent = this::class.java.classLoader.getResource("ValideringTestPerson_kafka.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)
        val request =  PdfService().mapRecordToRequestObject(medlemskapVurdering)
        println(request.toJsonPrettyString())
    }
    @Test
    fun testKorrektMappingDagPengerUavklart(){
        val fileContent = this::class.java.classLoader.getResource("regel_19_1_sample.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)
        val request =  PdfService().mapRecordToRequestObject(medlemskapVurdering)
        val node = JaksonParser().ToJson(request)
        println(request.toJsonPrettyString())
        Assertions.assertTrue(node.has("fnr"),"Det er ikke fnr på request")
        Assertions.assertTrue(node.has("fom"),"Det er ikke fom på request")
        Assertions.assertTrue(node.has("tom"),"Det er ikke tom på request")
        Assertions.assertTrue(node.has("navn"),"Det er navn tom på request")
        Assertions.assertTrue(node.has("ytelse"),"Det er ytelse tom på request")
        //TODO: Under må legges til på modellen
        Assertions.assertFalse(node.get("statsborger").asText().isNullOrBlank(),"Det er ikke  statsborger  på request")
        Assertions.assertTrue(node.get("årsaker").get(0).has("beskrivelse"),"Det er ikke beskrivelkse til på årsak")

    }
}