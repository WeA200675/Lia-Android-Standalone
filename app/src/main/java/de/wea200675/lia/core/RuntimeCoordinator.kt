package de.wea200675.lia.core

import java.io.File

class RuntimeCoordinator(
    private val primary: ModelSpec,
    private val recovery: ModelSpec,
    physicalCores: Int = Runtime.getRuntime().availableProcessors(),
    logicalThreads: Int = Runtime.getRuntime().availableProcessors(),
    maxRestarts: Int = RestartBudgetStore.DEFAULT
) {
    private val physicalCores = physicalCores
    private val logicalThreads = logicalThreads
    private var healing = SelfHealingPolicy(maxRestarts = maxRestarts.coerceIn(RestartBudgetStore.MIN, RestartBudgetStore.MAX), physicalCores = physicalCores, logicalThreads = logicalThreads)
    private var configuredMaxRestarts = maxRestarts.coerceIn(RestartBudgetStore.MIN, RestartBudgetStore.MAX)
    var restartBudget = configuredMaxRestarts
        private set
    var state = "INIT"
        private set
    var lastRecovery: RecoveryDecision = healing.decide(0)
        private set

    fun selectModel(root: File): ModelSpec? {
        val p = File(root, primary.fileName)
        if (ModelVerifier.verified(p, primary.sha256)) { state = "PRIMARY_READY"; return primary }
        val r = File(root, recovery.fileName)
        if (ModelVerifier.verified(r, recovery.sha256)) { state = "RECOVERY_READY"; return recovery }
        state = "BLOCKED"
        return null
    }

    fun configureRestartBudget(requested: Int): Int {
        configuredMaxRestarts = requested.coerceIn(RestartBudgetStore.MIN, RestartBudgetStore.MAX)
        healing = SelfHealingPolicy(maxRestarts = configuredMaxRestarts, physicalCores = physicalCores, logicalThreads = logicalThreads)
        restartBudget = configuredMaxRestarts
        lastRecovery = healing.decide(0)
        state = "READY"
        return configuredMaxRestarts
    }

    fun configuredRestartBudget(): Int = configuredMaxRestarts

    fun recordFailure() {
        val failureCount = configuredMaxRestarts - restartBudget + 1
        lastRecovery = healing.decide(failureCount)
        restartBudget = (restartBudget - 1).coerceAtLeast(0)
        state = if (restartBudget <= 0 || !lastRecovery.restartAllowed) "BLOCKED" else "BACKOFF"
    }

    fun resetBudget() {
        restartBudget = configuredMaxRestarts
        lastRecovery = healing.decide(0)
        state = "READY"
    }
}
