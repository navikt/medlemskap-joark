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

) {
    fun finnRegelResultat(regel: String): Resultat? {
        return resultat.finnRegelResultat(resultat, regel)
    }
}

data class Brukerinput(
    val arbeidUtenforNorge:Boolean
)

data class Datagrunnlag(
    val fnr: String,
    val ytelse: String,
    val førsteDagForYtelse: String?,
    val startDatoForYtelse: String?,
    val periode: Periode,
    val pdlpersonhistorikk: Personhistorikk,
    val brukerinput:Brukerinput
)

data class Personhistorikk(val navn: List<Navn>)

data class Navn(val fornavn: String, val mellomnavn: String?, val etternavn: String)

data class Periode(val fom: LocalDate, val tom: LocalDate)

data class Resultat(
    val svar: String,
    val dekning: String,
    val avklaring: String,
    val regelId: String,
    val delresultat: List<Resultat>
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
