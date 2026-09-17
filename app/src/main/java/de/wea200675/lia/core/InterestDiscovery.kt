package de.wea200675.lia.core

class InterestDiscovery {
 private val hints=mapOf("garten" to "Garten", "musik" to "Musik", "kochen" to "Kochen", "reise" to "Reisen", "bücher" to "Bücher", "tiere" to "Tiere", "geschichte" to "Geschichte")
 fun detect(text:String):List<String> = hints.filterKeys{text.contains(it,ignoreCase=true)}.values.toList()
 fun followUp(interests:List<String>)=if(interests.isEmpty()) "Was macht dir im Alltag besonders Freude?" else "Möchtest du mir mehr über ${interests.joinToString(" oder ")} erzählen?"
}
