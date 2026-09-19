package de.wea200675.lia.core

/**
 * Bounded supervisor for local AI. Native failures are surfaced through the safe fallback,
 * then the verified runtime is recreated only while the restart budget permits it.
 */
class SupervisedLocalRuntime(
    private val runtimeFactory: () -> ResilientLocalRuntime,
    private val coordinator: RuntimeCoordinator
) : ModelRuntime {
    private var current: ResilientLocalRuntime = runtimeFactory()

    override suspend fun generate(prompt: String): Result<String> {
        val response = current.generate(prompt)
        if (current.consumeNativeFailure()) {
            coordinator.recordFailure()
            if (coordinator.lastRecovery.restartAllowed && coordinator.state != "BLOCKED") {
                current.close()
                current = runCatching { runtimeFactory() }
                    .getOrElse { ResilientLocalRuntime(UnavailableNativeInference()) }
            } else {
                current.close()
            }
        }
        return response
    }

    override fun isReady(): Boolean = current.isReady()

    fun isNativeReady(): Boolean = current.isNativeReady()

    fun nativeState(): NativeRuntimeState = current.nativeState()

    fun state(): String = coordinator.state

    fun restartBudget(): Int = coordinator.restartBudget

    fun close() = current.close()
}
