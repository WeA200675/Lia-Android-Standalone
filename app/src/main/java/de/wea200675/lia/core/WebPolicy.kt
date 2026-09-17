package de.wea200675.lia.core

enum class WebAccessMode { OFFLINE, AUTO_ANONYMIZED_GENERIC, ASK_BEFORE_PERSONAL }

class WebPolicy(private var mode:WebAccessMode=WebAccessMode.OFFLINE) {
 fun mode()=mode
 fun setMode(value:WebAccessMode){ mode=value }
 fun requiresConfirmation(raw:String):Boolean {
  if(mode==WebAccessMode.OFFLINE) return true
  val t=raw.lowercase()
  return Regex("adresse|telefon|familie|name|gesund|arzt|medikament|konto|geld|termin|passwort|heimat").containsMatchIn(t)
 }
 fun prepare(raw:String):Result<String> {
  if(mode==WebAccessMode.OFFLINE) return Result.failure(IllegalStateException("Internet ist deaktiviert"))
  if(requiresConfirmation(raw) && mode!=WebAccessMode.ASK_BEFORE_PERSONAL) return Result.failure(IllegalStateException("Bestätigung für persönliche Anfrage erforderlich"))
  val clean=Anonymizer.redact(raw).trim()
  if(clean.length<3) return Result.failure(IllegalArgumentException("Anfrage ist zu kurz"))
  return Result.success(clean.take(2000))
 }
}
