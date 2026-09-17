package de.wea200675.lia.core

import java.io.File

class RuntimeCoordinator(
    private val primary: ModelSpec,
    private val recovery: ModelSpec,
    physicalCores: Int = Runtime.getRuntime().availableProcessors(),
    logicalThreads: Int = Runtime.getRuntime().availableProcessors()
) {
    private val healing = SelfHealingPolicy(physicalCores = physicalCores, logicalThreads = logicalThreads)
    var restartBudget = 3
        private set
    var state = "INIT"
        private set
    var lastRecovery: RecoveryDecision = healing.decide(0)
        private set

    fun selectModel(root: File): ModelSpec? {
        val p = File(root, primary.fileName)
        if (ModelVerifier.verified(p, primary.sha256)) {
            state = "PRIMARY_READY"
            return primary
        }
        val r = File(root, recovery.fileName)
        if (ModelVerifier.verified(r, recovery.sha256)) {
            state = "RECOVERY_READY"
            return recovery
        }
        state = "BLOCKED"
        return null
    }

    fun recordFailure() {
        val failures = 3 - restartBudget + 1
        lastRecovery = healing.decide(failures - 1)
        restartBudget--
        state = if (restartBudget <= 0) "BLOCKED" else "BACKOFF"
    }

    fun resetBudget() {
        restartBudget = 3
        lastRecovery = healing.decide(0)
        state = "READY"
    }
}
