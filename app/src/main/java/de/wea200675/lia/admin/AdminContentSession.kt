package de.wea200675.lia.admin

/** Activity-local authorization with a fixed lifetime; reads never extend it. */
class AdminContentSession(
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000L },
    private val lifetimeMillis: Long = 5 * 60_000L
) {
    init { require(lifetimeMillis > 0) }
    private var unlockedAt: Long? = null

    val isUnlocked: Boolean
        get() {
            val start = unlockedAt ?: return false
            val elapsed = nowMillis() - start
            if (elapsed < 0 || elapsed >= lifetimeMillis) {
                lock()
                return false
            }
            return true
        }

    fun unlock() { unlockedAt = nowMillis() }
    fun lock() { unlockedAt = null }

    /** Do not even read/decrypt personal content while locked or expired. */
    fun <T> read(loader: () -> T): T? = if (isUnlocked) loader() else null
}
