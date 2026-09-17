package de.wea200675.lia.core

/** Gateway contract: only explicitly approved, redacted queries may leave the device. */
interface WebGateway { suspend fun query(anonymizedQuery:String):Result<String> }
object Anonymizer { private val email=Regex("[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}"); fun redact(input:String)=input.replace(email,"[E-MAIL]").replace(Regex("\\b\\d{5}\\b"),"[PLZ]") }
