package de.wea200675.lia.core

enum class KnowledgeCategory {
    PERSONAL_PREFERENCE, DAILY_ROUTINE, BIOGRAPHY, INTEREST, RELATIONSHIP,
    CONVERSATION_STYLE, PRACTICAL_HELP, HEALTH_HINT, GENERAL_KNOWLEDGE,
    INTERNET_KNOWLEDGE, SAFETY, REVIEW_QUEUE
}

enum class KnowledgeState { NEW, CONFIRMED, FREQUENTLY_USED, UNCERTAIN, STALE, ARCHIVED, REVIEW }

data class ClassifiedKnowledge(
    val entry: KnowledgeEntry,
    val category: KnowledgeCategory,
    val state: KnowledgeState = KnowledgeState.NEW
)
