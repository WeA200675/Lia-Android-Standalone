package de.wea200675.lia.core

enum class AdminDestructiveAction {
    DELETE_LEARNING_PROFILE,
    DELETE_CONFIRMED_KNOWLEDGE
}

/** Requires two matching requests inside a short window; different actions never confirm each other. */
class AdminDestructiveActionGuard(
    private val confirmationWindowMs: Long = 30_000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private var armedAction: AdminDestructiveAction? = null
    private var armedAtEpochMs: Long? = null

    init { require(confirmationWindowMs in 5_000L..120_000L) }

    fun confirm(action: AdminDestructiveAction): Boolean {
        val now = clock()
        val armedAt = armedAtEpochMs
        val confirmed = armedAction == action && armedAt != null && now >= armedAt &&
            now - armedAt <= confirmationWindowMs
        if (confirmed) {
            cancel()
            return true
        }
        armedAction = action
        armedAtEpochMs = now
        return false
    }

    fun cancel() {
        armedAction = null
        armedAtEpochMs = null
    }
}
