package no.nav.medlemskap.inst.lytter.domain

import java.time.LocalDate



data class MedlemskapVurdert(
    val tidspunkt:String,
    //val førsteDagForYtelse:String,
    val resultat: Resultat,
    val datagrunnlag: Datagrunnlag,
    val erNorskStatsborger: Boolean = resultat.finnRegelResultat(resultat,"REGEL_11")?.svar=="JA",
    val erEOSBorger: Boolean = resultat.finnRegelResultat(resultat,"REGEL_2")?.svar=="JA",
    val erTredjelandsBorger:Boolean = !erEOSBorger

    )
{
    fun finnRegelResultat(regel:String):Resultat?{
        return resultat.finnRegelResultat(resultat,regel)
    }
}
data class Datagrunnlag(
    val ytelse:String,
    val førsteDagForYtelse:String?,
    val startDatoForYtelse:String?,
    val periode:Periode
)

data class Periode(val fom: LocalDate, val tom:LocalDate)
data class Resultat(
    val svar:String,
    val dekning:String,
    val avklaring:String,
    val regelId:String,
    val delresultat:List<Resultat>) {
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

        fun finnDelresultat(resultat: Resultat, regelId: String): Resultat? {
            return resultat.delresultat.find { it.regelId == regelId }
        }

    }
