package de.wea200675.lia.core

data class DailyQuestion(val id:String,val text:String)
object DailyQuestions { val defaults=listOf(DailyQuestion("mood","Wie geht es dir heute?"),DailyQuestion("plan","Was ist heute wichtig?"),DailyQuestion("help","Wobei soll ich dich unterstützen?"),DailyQuestion("joy","Was hat dir heute Freude gemacht?")) }
