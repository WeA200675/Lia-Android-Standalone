package de.wea200675.lia.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
import de.wea200675.lia.core.ModelStorageLocator
import de.wea200675.lia.core.ModelStorageReporter
import de.wea200675.lia.core.AdminDestructiveAction
import de.wea200675.lia.core.AdminDestructiveActionGuard

class AdminActivity : Activity() {
    private lateinit var kiosk: KioskController
    private lateinit var profile: EncryptedLearningProfile
    private val session = AdminContentSession(nowMillis = { SystemClock.elapsedRealtime() })
    private val sessionHandler = Handler(Looper.getMainLooper())
    private val expireSession = Runnable { concealAdminContent?.invoke() }
    private var concealAdminContent: (() -> Unit)? = null
    override fun onCreate(state:Bundle?) {
        super.onCreate(state); kiosk=KioskController(this); profile=EncryptedLearningProfile(this)
        val secureStore = AndroidSecureStore(this)
        val budgetStore = RestartBudgetStore(secureStore)
        val retainedKnowledge = ConfirmedKnowledgeRepository(secureStore)
        val modelStorage = ModelStorageLocator.forContext(this)
        val modelStorageReport = ModelStorageReporter.forDirectory(modelStorage)
        val destructiveGuard = AdminDestructiveActionGuard()
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(32,32,32,32) }
        val title=TextView(this).apply { text="Lia Admin"; textSize=32f; gravity=Gravity.CENTER }
        val pin=EditText(this).apply { hint="Admin-PIN"; inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD; textSize=22f }
        val setupPin=EditText(this).apply { hint="Neue Admin-PIN (6–12 Ziffern)"; inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD; textSize=20f }
        val setupConfirm=EditText(this).apply { hint="Neue Admin-PIN wiederholen"; inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD; textSize=20f }
        val setup=Button(this).apply { text="Admin-PIN erstmalig einrichten"; textSize=18f }
        val unlock=Button(this).apply { text="Kiosk verlassen"; textSize=20f }
        val lockNow=Button(this).apply { text="Jetzt sperren"; textSize=20f }
        val wifi=Button(this).apply { text="WLAN-Einstellungen öffnen"; textSize=20f }
        val background=Button(this).apply { text="Hintergrundbild auswählen"; textSize=18f }
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
        val voiceRate = SeekBar(this).apply { max = 15; progress = (((getSharedPreferences("lia_voice", MODE_PRIVATE).getFloat("speech_rate", 1.0f) - 0.5f) / 0.1f).toInt()).coerceIn(0, 15) }
        val voiceRateStatus = TextView(this).apply { textSize = 16f; text = "Sprechtempo: %.1fx".format(0.5f + voiceRate.progress * 0.1f) }
        fun refresh(){
            review.text=session.read { profile.confirmed().joinToString("\n"){"✓ ${it.questionId}: ${it.answer}"}.ifBlank{"Keine bestätigten Lernpunkte."} }
                ?: "Lernprofil: Inhalte erst nach PIN-Freigabe sichtbar."
            budgetStatus.text="KI-Selbstheilung: maximal ${budgetStore.load()} Neustarts pro Lauf"
            val knowledge=KnowledgeSessionRuntime.snapshot()
            val retained=try {
                session.read { retainedKnowledge.all() } ?: emptyList()
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
            retainedStatus.text=integrityText + if(session.isUnlocked) {
                "Bestätigtes Wissen: ${retained.size} Einträge\n" +
                    retained.take(10).joinToString("\n") { "• ${it.summary.take(140)} — ${it.sourceLabels.joinToString(", ")}" }
            } else {
                "Bestätigtes Wissen: Inhalte erst nach PIN-Freigabe sichtbar."
            }
            knowledgeStatus.text="Modellspeicher: ${modelStorageReport.userSummary()}\\n" +
                "Speicherpfad: ${if (modelStorageReport.usesExternalAppStorage) "erweiterter privater App-Speicher" else "privater interner App-Speicher"}\\n\\n" +
                KnowledgeCoverageReport.adminText() +
                "\n\nLaufzeitstatus: ${knowledge.cachedEntries}/${knowledge.cacheCapacity} Wissenseinträge" +
                "\nQuellen mit Fehlern: ${knowledge.sourcesWithFailures}" +
                "\nVorübergehend pausiert: ${knowledge.suspendedSources}"
        }
        fun requireAdmin():Boolean { if (session.isUnlocked) return true; concealAdminContent?.invoke(); status.text="Bitte zuerst mit der Admin-PIN freigeben."; return false }
        setup.setOnClickListener {
            if (kiosk.hasAdminPin()) { status.text="Eine Admin-PIN ist bereits eingerichtet."; return@setOnClickListener }
            val first=setupPin.text.toString(); val second=setupConfirm.text.toString()
            if (first != second) { status.text="Die beiden PIN-Eingaben stimmen nicht überein."; return@setOnClickListener }
            try { kiosk.setAdminPin(first); setupPin.text.clear(); setupConfirm.text.clear(); session.unlock(); sessionHandler.postDelayed(expireSession, 5 * 60_000L); setup.isEnabled=false; setupPin.isEnabled=false; setupConfirm.isEnabled=false; status.text="Admin-PIN eingerichtet und Admin-Modus geöffnet. WLAN-Einstellungen können jetzt geöffnet werden."; refresh() }
            catch (_: IllegalArgumentException) { status.text="Die PIN muss aus 6 bis 12 Ziffern bestehen."; }
        }
        unlock.setOnClickListener {
            sessionHandler.removeCallbacks(expireSession)
            session.lock()
            destructiveGuard.cancel()
            refresh()
            val verified = try { kiosk.disableWithPin(pin.text.toString()) } finally { pin.text.clear() }
            if (verified) {
                session.unlock()
                sessionHandler.postDelayed(expireSession, 5 * 60_000L)
            }
            val remaining=kiosk.lockoutRemainingSeconds()
            status.text=when {
                session.isUnlocked -> "Admin-Modus für höchstens fünf Minuten geöffnet."
                remaining>0 -> "Zu viele Fehlversuche. Bitte in ${remaining} Sekunden erneut versuchen."
                else -> "PIN nicht korrekt."
            }
            refresh()
        }
        lockNow.setOnClickListener { if (!requireAdmin()) return@setOnClickListener; kiosk.enable(); status.text="Kiosk-Modus wieder aktiviert."; concealAdminContent?.invoke() }
        wifi.setOnClickListener { if(!requireAdmin()) return@setOnClickListener; startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
        background.setOnClickListener {
            if (!requireAdmin()) return@setOnClickListener
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) }, 77)
        }
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
        voiceRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) { val rate = 0.5f + value * 0.1f; voiceRateStatus.text = "Sprechtempo: %.1fx".format(rate); getSharedPreferences("lia_voice", MODE_PRIVATE).edit().putFloat("speech_rate", rate).apply() }; override fun onStartTrackingTouch(seekBar: SeekBar?) {}; override fun onStopTrackingTouch(seekBar: SeekBar?) {} })
        box.addView(title); box.addView(pin); box.addView(unlock); box.addView(setupPin); box.addView(setupConfirm); box.addView(setup); box.addView(lockNow); box.addView(voiceRateStatus); box.addView(voiceRate); box.addView(wifi); box.addView(background); box.addView(review); box.addView(confirm); box.addView(clear); box.addView(budgetStatus); box.addView(budgetInput); box.addView(budgetApply); box.addView(budgetReset); box.addView(knowledgeStatus); box.addView(knowledgeReset); box.addView(retainedStatus); box.addView(retainedClear); box.addView(status)
        concealAdminContent = {
            sessionHandler.removeCallbacks(expireSession)
            session.lock()
            destructiveGuard.cancel()
            pin.text.clear()
            budgetInput.text.clear()
            status.text = "Admin-Bereich gesperrt. Bitte erneut mit PIN freigeben."
            refresh()
        }
        val scroll=ScrollView(this).apply { addView(box) }
        setContentView(scroll); if (intent.getBooleanExtra("admin_authorized", false)) { session.unlock(); setup.isEnabled=false; setupPin.isEnabled=false; setupConfirm.isEnabled=false; pin.isEnabled=false }; refresh()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) {}
            getSharedPreferences("lia_appearance", MODE_PRIVATE).edit().putString("background_uri", uri.toString()).apply()
            status.text = "Hintergrund gespeichert. Beim nächsten Start wird er dezent angezeigt."
        }
    }
    override fun onDestroy() {
        sessionHandler.removeCallbacks(expireSession)
        concealAdminContent = null
        super.onDestroy()
    }
    override fun onPause() {
        concealAdminContent?.invoke()
        super.onPause()
    }
}
