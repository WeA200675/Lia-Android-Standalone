package de.wea200675.lia.core

import android.content.Context

class ContentHistory(context:Context) {
 private val p=context.getSharedPreferences("lia_content_history",Context.MODE_PRIVATE)
 private fun get(key:String)=p.getStringSet(key,emptySet())!!.toMutableSet()
 fun nextQuestion(available:List<DailyQuestion>):DailyQuestion {
  val used=get("questions"); val candidate=available.firstOrNull{it.id !in used} ?: available.first()
  val next=if(candidate.id in used) emptySet() else used+candidate.id
  p.edit().putStringSet("questions",next).apply(); return candidate
 }
 fun nextFact():String {
  val used=get("facts"); val all=KnowledgeFacts.all(); val candidate=all.firstOrNull{it !in used} ?: all.first()
  val next=if(candidate in used) emptySet() else used+candidate
  p.edit().putStringSet("facts",next).apply(); return candidate
 }
 fun clear(){p.edit().clear().apply()}
}
