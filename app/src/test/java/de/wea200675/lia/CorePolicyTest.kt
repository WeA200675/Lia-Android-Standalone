package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test

class CorePolicyTest {
 @Test fun anonymizerRemovesEmailAndPostalCode(){ val out=Anonymizer.redact("mail a@test.de, PLZ 12345"); assertFalse(out.contains("a@test.de")); assertFalse(out.contains("12345")) }
 @Test fun routerClassifiesKnowledge(){ assertEquals(ConversationStyle.KNOWLEDGE,ConversationRouter().classify("Was ist wissenswert?")) }
 @Test fun resourceBudgetUsesLowerTierForSmallRam(){ assertEquals(2048,ResourceBudgets.forDevice(CpuProfile(4,4,false),4096).maxContextTokens) }
}
