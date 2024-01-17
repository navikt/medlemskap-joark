package no.nav.medlemskap.inst.lytter.domain

import no.nav.medlemskap.inst.lytter.jakson.JaksonParser

data class MedlemskapVurdertRecord(val partition:Int,val offset:Long,val value : String, val key:String,val topic:String,val json  :String)


 fun MedlemskapVurdertRecord.getYtelse():String{
    return JaksonParser().parseToObject(this.json).datagrunnlag.ytelse
}

fun MedlemskapVurdertRecord.getSvar():String {
    return JaksonParser().parseToObject(this.json).resultat.svar
}