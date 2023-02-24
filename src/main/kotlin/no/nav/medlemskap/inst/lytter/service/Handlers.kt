package no.nav.medlemskap.inst.lytter.service

import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.journalpost.IKanJournalforePDF

import no.nav.medlemskap.inst.lytter.pdfgenerator.IkanOpprettePdf


class DagpengeHandler (pdfService: IkanOpprettePdf,journalpostService: IKanJournalforePDF):IHandleVurderinger {
    override fun skalOpprettePDF(medlemskapVurdert: MedlemskapVurdert) :Boolean {
        TODO("Not yet implemented")
    }

}

interface IHandleVurderinger {
    fun skalOpprettePDF(medlemskapVurdert: MedlemskapVurdert) :Boolean


}
