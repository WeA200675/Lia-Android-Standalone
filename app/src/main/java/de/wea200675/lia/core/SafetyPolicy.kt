package de.wea200675.lia.core

object SafetyPolicy {
    fun requiresHumanHelp(text:String):Boolean = Regex("(?i)suizid|selbstmord|atemnot|schlaganfall|starke brustschmerzen|notruf").containsMatchIn(text)
    fun responseForRisk()="Bitte wende dich sofort an eine Vertrauensperson oder den Notruf. Ich kann keinen Notfall ersetzen."
}
