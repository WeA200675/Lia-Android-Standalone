package de.wea200675.lia.core

class ConversationRouter {
 fun classify(text:String):ConversationStyle { val t=text.lowercase(); return when { Regex("wie geht|erzählen|tag").containsMatchIn(t)->ConversationStyle.SMALLTALK; Regex("warum|wie funktioniert|wissenswert|fakt").containsMatchIn(t)->ConversationStyle.KNOWLEDGE; Regex("interess|gern|liebe|mag").containsMatchIn(t)->ConversationStyle.INTEREST_DISCOVERY; else->ConversationStyle.PRACTICAL_HELP } }
 fun offlineReply(style:ConversationStyle)=when(style){ ConversationStyle.SMALLTALK->"Ich höre dir gern zu. Erzähl mir mehr, wenn du möchtest."; ConversationStyle.KNOWLEDGE->KnowledgeFacts.random(); ConversationStyle.INTEREST_DISCOVERY->"Das klingt interessant. Möchtest du mir davon erzählen?"; ConversationStyle.PRACTICAL_HELP->"Gern. Wir können das gemeinsam Schritt für Schritt angehen." }
 fun applySensitivity(reply:String, profile:SensitivityProfile):String {
  var out = reply
  if (profile.gentler) out = "Ganz in Ruhe: " + out
  if (profile.slower) out = out.replace(". ", ".\\n")
  return out
 }
}
