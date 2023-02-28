package no.nav.medlemskap.inst.lytter.service

import no.nav.medlemskap.inst.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RegelLookupTest {

    @Test
    fun testREGEL_19_1(){

        val text = JaksonParser().lookupRegel("REGEL_19_1")
        Assertions.assertFalse(text == "INGEN BESKRIVELSE FUNNET FOR DENNE REGELEN","Ingen regel teskt er deifinert")

    }
    @Test
    fun testREGEL_Ukjent(){

        val text = JaksonParser().lookupRegel("REGEL_UKJENT")
        Assertions.assertTrue(text == "INGEN BESKRIVELSE FUNNET FOR DENNE REGELEN","ukjent regel skal ha spesifisert text")

    }
}