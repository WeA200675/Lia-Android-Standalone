package de.wea200675.lia.core

class WebPolicy(private var enabled:Boolean=false) {
    fun isEnabled()=enabled
    fun setEnabled(value:Boolean){ enabled=value }
    fun prepare(raw:String):Result<String> {
        if(!enabled) return Result.failure(IllegalStateException("Internet ist deaktiviert"))
        val clean=Anonymizer.redact(raw).trim()
        if(clean.length<3) return Result.failure(IllegalArgumentException("Anfrage ist zu kurz"))
        return Result.success(clean.take(2000))
    }
}
