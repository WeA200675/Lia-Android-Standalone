package de.wea200675.lia.core

data class ModelSpec(val id:String,val fileName:String,val sha256:String,val maxRamMb:Int)
interface ModelRuntime { suspend fun generate(prompt:String):Result<String>; fun isReady():Boolean }
class SafeOfflineRuntime : ModelRuntime {
 override suspend fun generate(prompt:String):Result<String> = Result.success("Ich bin im lokalen Grundmodus. Ich kann dir bei Alltag, Erinnerungen und Fragen helfen.")
 override fun isReady()=true
}
