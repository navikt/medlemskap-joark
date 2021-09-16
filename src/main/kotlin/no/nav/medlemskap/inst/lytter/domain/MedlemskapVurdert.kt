package no.nav.medlemskap.inst.lytter.domain

import java.time.LocalDate

data class MedlemskapVurdert(
    val tidspunkt:String,
    //val førsteDagForYtelse:String,
    val resultat: Resultat,
    val datagrunnlag: Datagrunnlag,
    val erNorskStatsborger: Boolean = resultat.delresultat
        .find { r->r.regelId.equals("REGEL_STATSBORGERSKAP") }
        ?.delresultat
        ?.find { r->r.regelId.equals("REGEL_11") }?.svar.equals("JA"),
    val erEOSBorger: Boolean = resultat.delresultat
        .find { r->r.regelId.equals("REGEL_STATSBORGERSKAP") }
        ?.delresultat
        ?.find { r->r.regelId.equals("REGEL_2") }?.svar.equals("JA"),
    val erTredjelandsBorger:Boolean = !erEOSBorger

    )
data class Datagrunnlag(
    val ytelse:String,
    val førsteDagForYtelse:String?,
    val startDatoForYtelse:String?,
    val periode:Periode
)

data class Periode(val fom:LocalDate, val tom:LocalDate)
data class Resultat(
    val svar:String,
    val dekning:String,
    val avklaring:String,
    val regelId:String,
    val delresultat:List<Resultat>)
