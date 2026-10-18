package de.wea200675.lia.core

/**
 * Single fail-closed decision for outbound knowledge requests.
 *
 * ASK_BEFORE_PERSONAL is intentionally not admitted here: the UI has no
 * per-request confirmation flow for personal questions, and those questions
 * must remain on device.
 */
object WebNetworkAdmission {
    fun allowed(mode: WebAccessMode, consentRecorded: Boolean): Boolean =
        consentRecorded && mode == WebAccessMode.AUTO_ANONYMIZED_GENERIC
}
