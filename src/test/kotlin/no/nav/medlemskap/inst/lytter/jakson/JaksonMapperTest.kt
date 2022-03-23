package no.nav.medlemskap.inst.lytter.jakson

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.medlemskap.inst.lytter.domain.ytelserSomKanGenererePDF
import no.nav.medlemskap.inst.lytter.http.httpClient
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.File
import java.net.URI
import java.net.URL

class JaksonMapperTest {

    @Test
    fun `mapping til request objekt for status Ja med ikke norsk statsborger`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)
        val parsed = JaksonParser().parseToObject(fileContent)
        Assertions.assertNotNull(parsed)
    }
    @Test
     fun `kontrakt testing medlemskap-oppslag`(){
        val fileContent = URL("https://raw.githubusercontent.com/navikt/medlemskap-oppslag/kafka/src/test/resources/testpersoner/bakoverkompatibeltest/forventetRespons.json").readText(Charsets.UTF_8)
        val parsed = JaksonParser().parseToObject(fileContent)
        Assertions.assertNotNull(parsed)
    }

    @Test
    fun `regler kjørt skal finnes i søk`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)
        val parsed = JaksonParser().parseToObject(fileContent)
        Assertions.assertNotNull(parsed)
        Assertions.assertTrue(parsed.finnRegelResultat("REGEL_10")?.svar=="JA")
    }
    @Test
    fun `ikke kjørte regler skal ikke feile ved søk`(){
        val fileContent = this::class.java.classLoader.getResource("JaVurdering_3landsBorger.json").readText(Charsets.UTF_8)
        val parsed = JaksonParser().parseToObject(fileContent)
        Assertions.assertNotNull(parsed)
        Assertions.assertFalse(parsed.finnRegelResultat("REGEL_109")?.svar=="TRUE")
    }




}