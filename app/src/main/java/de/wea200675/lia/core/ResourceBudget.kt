package de.wea200675.lia.core

data class ResourceBudget(val threads:Int,val maxContextTokens:Int,val maxMemoryMb:Int)
object ResourceBudgets { fun forDevice(cpu:CpuProfile,ramMb:Int):ResourceBudget { val threads=cpu.recommendedThreads.coerceIn(1,8); val context=when { ramMb>=8192 -> 4096; ramMb>=6144 -> 3072; else -> 2048 }; return ResourceBudget(threads,context,(ramMb*0.65).toInt()) } }
