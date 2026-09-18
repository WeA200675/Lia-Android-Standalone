package de.wea200675.lia.admin

/** An activity-local authorization gate. Never persist or restore an unlocked session. */
class AdminContentSession {
    var isUnlocked: Boolean = false
        private set

    fun unlock() { isUnlocked = true }
    fun lock() { isUnlocked = false }

    /** Do not even read/decrypt personal content while the admin session is locked. */
    fun <T> read(loader: () -> T): T? = if (isUnlocked) loader() else null
}
