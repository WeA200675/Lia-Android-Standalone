package de.wea200675.lia.core

import java.io.File

class RuntimeCoordinator(private val primary:ModelSpec, private val recovery:ModelSpec) {
    var restartBudget=3; private set
    var state="INIT"; private set
    fun selectModel(root:File):ModelSpec? {
        val p=File(root,primary.fileName); if(ModelVerifier.verified(p,primary.sha256)){ state="PRIMARY_READY"; return primary }
        val r=File(root,recovery.fileName); if(ModelVerifier.verified(r,recovery.sha256)){ state="RECOVERY_READY"; return recovery }
        state="BLOCKED"; return null
    }
    fun recordFailure(){ restartBudget--; if(restartBudget<=0) state="BLOCKED" else state="BACKOFF" }
    fun resetBudget(){ restartBudget=3; state="READY" }
}
