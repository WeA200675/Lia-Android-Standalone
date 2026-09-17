package de.wea200675.lia.core

data class KioskConfig(val enabled:Boolean=false,val adminPinConfigured:Boolean=false,val allowWifiSettings:Boolean=false)
interface KioskAdmin { fun requestAdmin(pin:String):Boolean }
