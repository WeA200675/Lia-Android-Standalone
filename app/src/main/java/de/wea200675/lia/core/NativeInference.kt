package de.wea200675.lia.core

/** Adapter boundary for a future native Android GGUF engine (LiteRT/llama.cpp JNI). */
interface NativeInference {
 fun load(verifiedModel:ModelSpec):Boolean
 fun generate(prompt:String,maxTokens:Int=256):Result<String>
 fun close()
}
class UnavailableNativeInference:NativeInference {
 override fun load(verifiedModel:ModelSpec)=false
 override fun generate(prompt:String,maxTokens:Int)=Result.failure(IllegalStateException("Native Android inference adapter not installed"))
 override fun close(){}
}
