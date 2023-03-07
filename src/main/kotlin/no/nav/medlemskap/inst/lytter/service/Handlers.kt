package no.nav.medlemskap.inst.lytter.service

import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.journalpost.IKanJournalforePDF

import no.nav.medlemskap.inst.lytter.pdfgenerator.IkanOpprettePdf


class DagpengeHandler (pdfService: IkanOpprettePdf,journalpostService: IKanJournalforePDF):IHandleVurderinger {
    override fun skalOpprettePDF(medlemskapVurdert: MedlemskapVurdert) :Boolean {
        return medlemskapVurdert.resultat.svar=="UAVKLART"
                && medlemskapVurdert.datagrunnlag.ytelse=="DAGPENGER"
                && medlemskapVurdert.resultat.årsaker.size == 1
                && medlemskapVurdert.resultat.årsaker.first().regelId=="REGEL_19_1"
    }

}

interface IHandleVurderinger {
    fun skalOpprettePDF(medlemskapVurdert: MedlemskapVurdert) :Boolean


}
