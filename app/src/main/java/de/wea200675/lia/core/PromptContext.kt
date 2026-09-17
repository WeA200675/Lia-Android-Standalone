package de.wea200675.lia.core

object PromptContext {
    fun build(userText:String, profile:List<LearningItem>, onlineAllowed:Boolean):String {
        val memories=profile.filter{it.confirmed}.takeLast(20).joinToString("\\n"){ "- "+it.answer.take(240) }
        val mode=if(onlineAllowed) "Internet nur anonymisiert und nach Einwilligung" else "vollständig offline"
        return "Du bist Lia, geduldig und klar. Modus: $mode.\\n"+
            "Sicherheitsregeln: keine Diagnosen, keine erfundenen Notfälle, bei Gefahr an Vertrauensperson/Notruf verweisen.\\n"+
            "Bestätigte persönliche Hinweise (nur verwenden, wenn relevant):\\n$memories\\n\\nNachricht: ${userText.take(2000)}"
    }
}
