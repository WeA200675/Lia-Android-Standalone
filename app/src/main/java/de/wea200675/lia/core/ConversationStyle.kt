package de.wea200675.lia.core

enum class ConversationStyle { SMALLTALK, KNOWLEDGE, INTEREST_DISCOVERY, PRACTICAL_HELP }
object ConversationPrompts {
 fun starter(style:ConversationStyle)=when(style){
  ConversationStyle.SMALLTALK->"Erzähle mir gern, wie dein Tag bisher war – ganz ohne Druck."
  ConversationStyle.KNOWLEDGE->"Möchtest du heute eine kurze interessante Tatsache hören?"
  ConversationStyle.INTEREST_DISCOVERY->"Gibt es ein Thema, über das du gerade gern mehr erzählen möchtest?"
  ConversationStyle.PRACTICAL_HELP->"Wobei darf ich dich heute konkret unterstützen?" }
}
