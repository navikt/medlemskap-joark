package no.nav.medlemskap.inst.lytter.pdfgenerator

enum class MedlemskapVurdering(val url: String) {
    JA("/api/v1/genpdf/medlemskapresultater/medlemskapvurdert"),
    UAVKLART("/api/v1/genpdf/medlemskapresultater/medlemskapuavklart")
}
