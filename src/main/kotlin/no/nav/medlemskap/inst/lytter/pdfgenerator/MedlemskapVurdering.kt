package no.nav.medlemskap.inst.lytter.pdfgenerator

enum class MedlemskapVurdering(override val url: String):vurdering {
    JA("/api/v1/genpdf/medlemskapresultater/medlemskapvurdert"),
    NEI("/api/v1/genpdf/medlemskapresultater/medlemskapnei"),
    UAVKLART("/api/v1/genpdf/medlemskapresultater/medlemskapuavklart");
}

interface vurdering{
    val url:String

}
