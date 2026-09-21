package no.nav.medlemskap.inst.lytter.service

import no.nav.medlemskap.inst.lytter.domain.Statsborgerskap
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
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
    fun testKorrektMappingNyModell(){
        val fileContent = this::class.java.classLoader.getResource("JaVurderingMedKonklusjon.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)
        val request =  PdfService().mapRecordToRequestObject(medlemskapVurdering)
        println(request.toJsonPrettyString())
    }
    @Test
    fun testKorrektMappingAvStatsborgerSkap(){
        val list = listOf<Statsborgerskap>(Statsborgerskap(landkode = "NOR", historisk = false
        ),
            Statsborgerskap(landkode = "SWE",false))
        val tekst =  PdfService().hentStatsborgerskap(list)
        println(tekst)
        Assertions.assertFalse(tekst.isNullOrBlank())
        Assertions.assertTrue(tekst == "NOR og SWE")



    }
    @Test
    fun testKorrektMappingAvStatsborgerSkapMedHistoriskeFelter(){
        val list = listOf<Statsborgerskap>(Statsborgerskap(landkode = "NOR", historisk = false
        ),
            Statsborgerskap(landkode = "SWE",true))
        val tekst =  PdfService().hentStatsborgerskap(list)
        Assertions.assertFalse(tekst.isNullOrBlank())
        Assertions.assertTrue(tekst == "NOR")

    }

}