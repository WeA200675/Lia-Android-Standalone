package de.wea200675.lia.core

/** Consent-aware local memory; it is not neural-weight retraining. */
data class LearningItem(val questionId:String,val answer:String,val confirmed:Boolean=false,val createdAt:Long=System.currentTimeMillis())
data class LearningProfile(val items:List<LearningItem>=emptyList()) {
 fun add(item:LearningItem)=copy(items=items+item)
 fun confirmedItems()=items.filter{it.confirmed}
}
