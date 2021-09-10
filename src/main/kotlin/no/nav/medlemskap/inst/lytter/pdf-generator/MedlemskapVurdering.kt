package no.nav.medlemskap.inst.lytter.`pdf-generator`

enum class MedlemskapVurdering(val url: String) {
    Ja("/api/v1/genpdf/medlemskapresultater/medlemskapvurdert")
}
