package no.nav.medlemskap.inst.lytter.domain

import java.time.LocalDate


data class MedlemskapVurdert(
    val kanal: String?,
    val tidspunkt: String,
    val resultat: Resultat,
    val datagrunnlag: Datagrunnlag,
    val erNorskStatsborger: Boolean = resultat.finnRegelResultat(resultat, "REGEL_11")?.svar == "JA",
    val erEOSBorger: Boolean = resultat.finnRegelResultat(resultat, "REGEL_2")?.svar == "JA",
    val erTredjelandsBorger: Boolean = !erEOSBorger,
    val konklusjon: List<Konklusjon> = emptyList()

) {
    fun finnRegelResultat(regel: String): Resultat? {
        return resultat.finnRegelResultat(resultat, regel)
    }

    fun finnsvar():String{
        runCatching { konklusjon.first().status.name }
            .onFailure { return resultat.svar }
            .onSuccess { return it }
        return ""
    }
}



data class Datagrunnlag(
    val fnr: String,
    val ytelse: String,
    val førsteDagForYtelse: String?,
    val startDatoForYtelse: String?,
    val periode: Periode,
    val medlemskap: List<MedlInnslag>,
    val pdlpersonhistorikk: Personhistorikk,
    val brukerinput:Brukerinput
)
data class MedlInnslag(
    val dekning: String?,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val erMedlem: Boolean,
    val lovvalg: MedlLovvalg?,
    val lovvalgsland: String?,
    val periodeStatus: PeriodeStatus?
){
    private val periode = Periode(fraOgMed, tilOgMed)

    fun overlapper(annenPeriode: Periode): Boolean {
        return periode.overlapper(annenPeriode)
    }

    companion object {
        fun List<MedlInnslag>.brukerensMEDLInnslagIPeriode(periode: Periode):List<MedlInnslag> =
            this.filter {
                it.overlapper(periode) &&
                        (it.lovvalg == null || it.lovvalg == MedlLovvalg.ENDL) &&
                        (it.periodeStatus == null || it.periodeStatus == PeriodeStatus.GYLD)
            }


        fun List<MedlInnslag>.brukerensFørsteMEDLUnntakIPeriode(periode: Periode):MedlInnslag =
            this.brukerensMEDLInnslagIPeriode(periode).first { !it.erMedlem }
    }


}


enum class MedlLovvalg() {
    ENDL, FORL, UAVK
}

enum class PeriodeStatus() {
    GYLD, AVST, UAVK
}

data class Personhistorikk(val navn: List<Navn>,val statsborgerskap:List<Statsborgerskap>)

data class Statsborgerskap(
val landkode:String,
val historisk:Boolean
)


data class Navn(val fornavn: String, val mellomnavn: String?, val etternavn: String)
data class Årsak (
    val svar: String,
    val avklaring: String,
    val regelId: String,
    val begrunnelse: String,
    var beskrivelse:String?
)


data class Resultat(
    val svar: String,
    val dekning: String,
    val avklaring: String,
    val regelId: String,
    val delresultat: List<Resultat>,
    val årsaker:List<Årsak>
) {

    fun finnRegelResultat(resultat: Resultat, regelId: String): Resultat? {
        var regelResultat = finnDelresultat(resultat, regelId)
        if (regelResultat != null) {
            return regelResultat
        }

        resultat.delresultat.forEach { delresultat ->
            regelResultat = finnRegelResultat(delresultat, regelId)
            if (regelResultat != null) {
                return regelResultat
            }
        }

        return regelResultat
    }

    private fun finnDelresultat(resultat: Resultat, regelId: String): Resultat? {
        return resultat.delresultat.find { it.regelId == regelId }
    }

}
