package no.nav.medlemskap.inst.lytter.domain

data class MedlemskapVurdertRecord(val partition:Int,val offset:Long,val value : String, val key:String,val topic:String,val json  :String)


