package de.wea200675.lia.core

enum class LiaMode { OFFLINE, ONLINE_ANONYMIZED }
object OfflinePolicy { fun fallback(message:String)="Ich kann gerade nur lokal helfen. $message" }
