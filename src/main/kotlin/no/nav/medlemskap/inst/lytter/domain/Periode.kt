package no.nav.medlemskap.inst.lytter.domain

import org.threeten.extra.Interval
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Periode(val fom: LocalDate, val tom: LocalDate){

    fun erGyldigPeriode(): Boolean {
        return (fomNotNull().isBefore(tomNotNull()) || fomNotNull().isEqual(tomNotNull()))
    }

    fun overlapper(annenPeriode: Periode): Boolean {
        if (!erGyldigPeriode()) {
            return false
        }

        return interval().overlaps(annenPeriode.interval())
    }

    fun interval(): Interval = Interval.of(this.intervalStartInclusive(), this.intervalEndExclusive())
    fun fomNotNull() = this.fom ?: LocalDate.MIN

    fun tomNotNull() = this.tom ?: LocalDate.MAX

    private fun intervalStartInclusive(): Instant = this.fom?.startOfDayInstant() ?: Instant.MIN

    private fun intervalEndExclusive(): Instant = this.tom?.plusDays(1)?.startOfDayInstant() ?: Instant.MAX

    fun LocalDate.startOfDayInstant() = this.atStartOfDay(ZoneId.systemDefault()).toInstant()
}
