package de.wea200675.lia.core

import kotlin.math.max

data class CpuProfile(val logicalCores:Int,val recommendedThreads:Int,val smtActive:Boolean)
object CpuProfiles {
 fun detect(): CpuProfile { val n=max(1,Runtime.getRuntime().availableProcessors()); val physical=max(1,n/2); return CpuProfile(n,n,n>physical) }
}
