package de.wea200675.lia.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.*
import de.wea200675.lia.core.AndroidSecureStore
import de.wea200675.lia.core.EncryptedLearningProfile
import de.wea200675.lia.core.RestartBudgetStore
import de.wea200675.lia.core.KnowledgeCoverageReport
import de.wea200675.lia.core.KnowledgeSessionRuntime
import de.wea200675.lia.core.ConfirmedKnowledgeRepository
import de.wea200675.lia.core.ConfirmedKnowledgeIntegrityRuntime
import de.wea200675.lia.core.KnowledgeIntegrityState
import de.wea200675.lia.core.AdminDestructiveAction
import de.wea200675.lia.core.AdminDestructiveActionGuard

class AdminActivity : Activity() {
    private lateinit var kiosk: KioskController
    private lateinit var profile: EncryptedLearningProfile
    override fun onCreate(state:Bundle?) {
        super.onCreate(state); kiosk=KioskController(this); profile=EncryptedLearningProfile(this)
        val secureStore = AndroidSecureStore(this)
        val budgetStore = RestartBudgetStore(secureStore)
        val retainedKnowledge = ConfirmedKnowledgeRepository(secureStore)
        val destructiveGuard = AdminDestructiveActionGuard()
        var adminUnlocked = false
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(32,32,32,32) }
        val title=TextView(this).apply { text="Lia Admin"; textSize=32f; gravity=Gravity.CENTER }
        val pin=EditText(this).apply { hint="Admin-PIN"; inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD; textSize=22f }
        val unlock=Button(this).apply { text="Kiosk verlassen"; textSize=20f }
        val wifi=Button(this).apply { text="WLAN-Einstellungen öffnen"; textSize=20f }
        val review=TextView(this).apply { textSize=18f; setPadding(0,24,0,12) }
        val confirm=Button(this).apply { text="Alle gespeicherten Punkte bestätigen"; textSize=18f }
        val clear=Button(this).apply { text="Lernprofil vollständig löschen"; textSize=18f }
        val budgetStatus=TextView(this).apply { textSize=18f; setPadding(0,18,0,6) }
        val budgetInput=EditText(this).apply { hint="Neues Restart-Budget (1–5)"; inputType=InputType.TYPE_CLASS_NUMBER; textSize=18f }
        val budgetApply=Button(this).apply { text="Restart-Budget speichern"; textSize=18f }
        val budgetReset=Button(this).apply { text="Restart-Budget zurücksetzen"; textSize=18f }
        val knowledgeStatus=TextView(this).apply {
            textSize=16f
            setPadding(0,24,0,16)
        }
        val knowledgeReset=Button(this).apply {
            text="Wissenspuffer und Quellenfehler zurücksetzen"
            textSize=16f
        }
        val retainedStatus=TextView(this).apply { textSize=16f; setPadding(0,20,0,8) }
        val retainedClear=Button(this).apply { text="Dauerhaftes bestätigtes Wissen löschen"; textSize=16f }
        val status=TextView(this).apply { textSize=18f; gravity=Gravity.CENTER }
        fun refresh(){
            review.text=profile.confirmed().joinToString("\n"){"✓ ${it.questionId}: ${it.answer}"}.ifBlank{"Keine bestätigten Lernpunkte."}
            budgetStatus.text="KI-Selbstheilung: maximal ${budgetStore.load()} Neustarts pro Lauf"
            val knowledge=KnowledgeSessionRuntime.snapshot()
            val retained=try {
                retainedKnowledge.all()
            } catch (_: SecurityException) {
                ConfirmedKnowledgeIntegrityRuntime.recordSecurityFailure()
                emptyList()
            }
            val integrity=ConfirmedKnowledgeIntegrityRuntime.snapshot()
            val integrityText=if(integrity.state==KnowledgeIntegrityState.SECURITY_FAILURE) {
                "⚠ Integritätsfehler: Bestätigtes Wissen ist gesperrt. Nach PIN-Freigabe vollständig löschen.\n"
            } else {
                "✓ Verschlüsselte Wissensablage ohne erkannten Integritätsfehler.\n"
            }
            retainedStatus.text=integrityText + if(adminUnlocked) {
                "Bestätigtes Wissen: ${retained.size} Einträge\n" +
                    retained.take(10).joinToString("\n") { "• ${it.summary.take(140)} — ${it.sourceLabels.joinToString(", ")}" }
            } else {
                "Bestätigtes Wissen: ${retained.size} verschlüsselte Einträge. Inhalte erst nach PIN-Freigabe sichtbar."
            }
            knowledgeStatus.text=KnowledgeCoverageReport.adminText() +
                "\n\nLaufzeitstatus: ${knowledge.cachedEntries}/${knowledge.cacheCapacity} Wissenseinträge" +
                "\nQuellen mit Fehlern: ${knowledge.sourcesWithFailures}" +
                "\nVorübergehend pausiert: ${knowledge.suspendedSources}"
        }
        fun requireAdmin():Boolean { if (adminUnlocked) return true; status.text="Bitte zuerst mit der Admin-PIN freigeben."; return false }
        unlock.setOnClickListener { adminUnlocked=kiosk.disableWithPin(pin.text.toString()); if(!adminUnlocked) destructiveGuard.cancel(); status.text=if(adminUnlocked) "Admin-Modus geöffnet." else "PIN nicht korrekt."; refresh() }
        wifi.setOnClickListener { if(!requireAdmin()) return@setOnClickListener; startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
        confirm.setOnClickListener { if(!requireAdmin()) return@setOnClickListener; profile.confirmAll(); status.text="Alle Lernpunkte bestätigt."; refresh() }
        clear.setOnClickListener {
            if(!requireAdmin()) return@setOnClickListener
            if(!destructiveGuard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE)) {
                status.text="Lernprofil wirklich löschen? Bitte dieselbe Taste innerhalb von 30 Sekunden erneut drücken."
                return@setOnClickListener
            }
            profile.deleteAll()
            status.text="Lernprofil gelöscht."
            refresh()
        }
        budgetApply.setOnClickListener {
            if(!requireAdmin()) return@setOnClickListener
            val requested=budgetInput.text.toString().toIntOrNull()
            if(requested==null){ status.text="Bitte eine Zahl von 1 bis 5 eingeben."; return@setOnClickListener }
            val saved=budgetStore.save(requested)
            status.text="Restart-Budget auf $saved gesetzt. Wirksam beim nächsten KI-Start."; refresh()
        }
        budgetReset.setOnClickListener {
            if(!requireAdmin()) return@setOnClickListener
            budgetStore.save(RestartBudgetStore.DEFAULT)
            status.text="Restart-Budget auf den sicheren Standard 3 zurückgesetzt."; refresh()
        }
        retainedClear.setOnClickListener {
            if(!requireAdmin()) return@setOnClickListener
            if(!destructiveGuard.confirm(AdminDestructiveAction.DELETE_CONFIRMED_KNOWLEDGE)) {
                status.text="Bestätigtes Wissen wirklich löschen? Bitte dieselbe Taste innerhalb von 30 Sekunden erneut drücken."
                return@setOnClickListener
            }
            retainedKnowledge.clear()
            ConfirmedKnowledgeIntegrityRuntime.resetAfterDeletion()
            status.text="Dauerhaftes bestätigtes Wissen vollständig gelöscht; Integritätsalarm zurückgesetzt."
            refresh()
        }
        knowledgeReset.setOnClickListener {
            if(!requireAdmin()) return@setOnClickListener
            KnowledgeSessionRuntime.reset()
            status.text="Wissenspuffer geleert und Quellenfehler zurückgesetzt."
            refresh()
        }
        box.addView(title); box.addView(pin); box.addView(unlock); box.addView(wifi); box.addView(review); box.addView(confirm); box.addView(clear); box.addView(budgetStatus); box.addView(budgetInput); box.addView(budgetApply); box.addView(budgetReset); box.addView(knowledgeStatus); box.addView(knowledgeReset); box.addView(retainedStatus); box.addView(retainedClear); box.addView(status)
        val scroll=ScrollView(this).apply { addView(box) }
        setContentView(scroll); refresh()
    }
}