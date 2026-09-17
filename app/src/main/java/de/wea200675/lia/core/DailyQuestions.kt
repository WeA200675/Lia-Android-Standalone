package de.wea200675.lia.core

data class DailyQuestion(val id:String,val text:String,val style:ConversationStyle=ConversationStyle.PRACTICAL_HELP)
object DailyQuestions { val defaults=listOf(
 DailyQuestion("mood","Wie geht es dir heute?"), DailyQuestion("plan","Was ist heute wichtig?"),
 DailyQuestion("smalltalk","Wie war dein Tag bisher?",ConversationStyle.SMALLTALK),
 DailyQuestion("knowledge","Möchtest du etwas Wissenswertes hören?",ConversationStyle.KNOWLEDGE),
 DailyQuestion("interest","Gibt es ein Thema, das dich gerade interessiert?",ConversationStyle.INTEREST_DISCOVERY),
 DailyQuestion("help","Wobei soll ich dich unterstützen?") ) }
