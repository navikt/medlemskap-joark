package no.nav.medlemskap.inst.lytter.service

import no.nav.medlemskap.inst.lytter.domain.MedlemskapVurdert
import no.nav.medlemskap.inst.lytter.journalpost.JournalpostService
import no.nav.medlemskap.inst.lytter.pdfgenerator.PdfService

class DagpengeHandler (pdfService: PdfService,journalpostService: JournalpostService):IHandleVurderinger {
    override fun skalOpprettePDF(medlemskapVurdert: MedlemskapVurdert) :Boolean {
        TODO("Not yet implemented")
    }

}

interface IHandleVurderinger {
    fun skalOpprettePDF(medlemskapVurdert: MedlemskapVurdert) :Boolean


}
