package de.wea200675.lia.core

interface SpeechToText { suspend fun listen():Result<String> }
interface TextToSpeech { fun speak(text:String) }
object VoiceFallback { fun textOnly()="Die Texteingabe bleibt verfügbar." }
