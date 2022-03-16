package no.nav.medlemskap.inst.lytter.domain

enum class Ytelse {
    LOVME_GCP;

    companion object {

        val clientIdToYtelseMap: Map<String, Ytelse> = hashMapOf(
            "2719da58-489e-4185-9ee6-74b7e93763d2" to LOVME_GCP, // dev
            "23600ac9-019c-445d-87a4-2df4996e6f63" to LOVME_GCP // Prod verifisering
        )

        fun fromClientId(clientId: String?): Ytelse? = clientIdToYtelseMap[clientId]
        fun Ytelse.name(): String = this.name.toLowerCase().capitalize()
    }
}
