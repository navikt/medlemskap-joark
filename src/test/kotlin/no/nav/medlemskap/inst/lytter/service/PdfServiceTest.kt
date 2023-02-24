package no.nav.medlemskap.inst.lytter.service

import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Test

class PdfServiceTest {
    @Test
    fun testKorrektMapping(){
        val fileContent = this::class.java.classLoader.getResource("ValideringTestPerson_kafka.json").readText(Charsets.UTF_8)
        val medlemskapVurdering = JaksonParser().parseToObject(fileContent)
        val request =  PdfService().mapRecordToRequestObject(medlemskapVurdering)
        println(request.toJsonPrettyString())
    }
}