package no.nav.medlemskap.inst.lytter.pdfgenerator

enum class MedlemskapVurdering(override val url: String):vurdering {
    JA("/api/v1/genpdf/medlemskapresultater/medlemskapvurdert"),
    NEI("/api/v1/genpdf/medlemskapresultater/medlemskapnei"),
    UAVKLART("/api/v1/genpdf/medlemskapresultater/medlemskapuavklart");
}

enum class MedlemskapVurderingDagpenger(override val url: String):vurdering {
    JA("/api/v1/genpdf/medlemskapresultater/medlemskapvurdert_dagpenger"),
    UAVKLART("/api/v1/genpdf/medlemskapresultater/medlemskapuavklart_dagpenger");

}

interface vurdering{
    val url:String

}
