package no.nav.medlemskap.inst.lytter.jakson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert


class JaksonParser {
    fun parseToObject(jsonString: String): MedlemskapVurdert {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        val medlemskapVurdert:MedlemskapVurdert = mapper.readValue(jsonString)
        leggtilRegelLosningsBeskrivelse(medlemskapVurdert)
        return  medlemskapVurdert

    }

    private fun leggtilRegelLosningsBeskrivelse(medlemskapVurdert: MedlemskapVurdert) {
        medlemskapVurdert.resultat.årsaker.forEach { it.beskrivelse=lookupRegel(it.regelId) }
    }

    fun lookupRegel(regelId: String): String {
        runCatching { this::class.java.classLoader.getResource(regelId+".html").readText(Charsets.UTF_8) }
            .onSuccess{return  it}
            .onFailure { return "INGEN BESKRIVELSE FUNNET FOR DENNE REGELEN" }
        return ""
    }

    fun parse(jsonString: String): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.readValue(jsonString)

    }
    fun ToJson(obj: Any): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.valueToTree(obj);

    }

}