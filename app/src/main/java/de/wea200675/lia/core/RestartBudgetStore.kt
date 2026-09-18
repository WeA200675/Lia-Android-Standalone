package de.wea200675.lia.core

/** Encrypted, bounded configuration for the local AI restart budget. */
class RestartBudgetStore(
    private val secureStore: SecureStore,
    private val key: String = "lia.runtime.restart-budget.v1"
) {
    fun load(default: Int = DEFAULT): Int = runCatching {
        secureStore.get(key)?.toString(Charsets.UTF_8)?.toIntOrNull()?.coerceIn(MIN, MAX) ?: default.coerceIn(MIN, MAX)
    }.getOrDefault(DEFAULT)

    fun save(requested: Int): Int {
        val bounded = requested.coerceIn(MIN, MAX)
        runCatching { secureStore.put(key, bounded.toString().toByteArray(Charsets.UTF_8)) }
        return bounded
    }

    companion object {
        const val MIN = 1
        const val MAX = 5
        const val DEFAULT = 3
    }
}
