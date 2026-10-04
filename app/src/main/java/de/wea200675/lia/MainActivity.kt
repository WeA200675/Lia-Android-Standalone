package de.wea200675.lia

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.widget.*
import android.view.ViewGroup
import android.app.AlertDialog
import de.wea200675.lia.admin.AdminActivity
import de.wea200675.lia.admin.KioskController
import de.wea200675.lia.core.*
import de.wea200675.lia.background.DailyLearningWorker

class MainActivity : Activity() {
    private var index = 0
    private lateinit var profile: EncryptedLearningProfile
    private var recognizer: SpeechRecognizer? = null
    private var speaker: TextToSpeech? = null
    private lateinit var localRuntime: LocalModelRuntime
    private var modelStatusView: TextView? = null
    private lateinit var answerOrchestrator: AnswerOrchestrator
    private lateinit var confirmedKnowledge: ConfirmedKnowledgeRepository
    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingDailySpeech = false
    private var permissionFeedback: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profile = EncryptedLearningProfile(this)
        DailyLearningWorker.schedule(this)
        speaker = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) { speaker?.language = Locale.GERMAN; speaker?.setSpeechRate(getSharedPreferences("lia_voice", MODE_PRIVATE).getFloat("speech_rate", 1.0f)) } }
        val router = ConversationRouter()
        val cpu = CpuProfiles.detect()
        val cap = DeviceCapabilityProbe.read(this)
        val perf = ResourceGovernor(this).level()
        val modelStorage = ModelStorageLocator.forContext(this)
        val modelStorageReport = ModelStorageReporter.forDirectory(modelStorage)
        val runtimeBootstrap = ModelRuntimeBootstrap(this)
        localRuntime = runtimeBootstrap.createSupervised()
        val webStore = WebModeStore(this)
        var webMode = webStore.get()
        val consentStore = ConsentStore(this)
        var webConsent = consentStore.webEnabled()
        val cachePlan = KnowledgeCacheCapacity.recommend(
            ramMb = cap.ramMb.toLong(),
            sourceCount = KnowledgeSourceCatalog.sources.size
        )
        val secureStore = AndroidSecureStore(this)
        confirmedKnowledge = ConfirmedKnowledgeRepository(secureStore)
        answerOrchestrator = AnswerOrchestrator(
            localRuntime,
            SafeWikipediaGateway(
                enabled = { WebNetworkAdmission.allowed(webMode, webConsent) },
                cache = KnowledgeSessionRuntime.configureCache(cachePlan.maxEntries),
                sourceHealth = KnowledgeSessionRuntime.sourceHealth
            ),
            confirmedKnowledge = confirmedKnowledge
        )
        val today = LocalDate.now()
        val dailyPlan = DailyPlanRepository(TrainingCache(secureStore)).forDate(today)
        val promptProgress = DailyPromptProgress(secureStore)
        index = promptProgress.nextIndex(today, dailyPlan.prompts.size)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
        }
        val root = FrameLayout(this)
        val backgroundUri = getSharedPreferences("lia_appearance", MODE_PRIVATE).getString("background_uri", null)
        if (backgroundUri != null) try {
            content.background = ColorDrawable(Color.rgb(255, 248, 240))
            val image = ImageView(this).apply { setImageURI(Uri.parse(backgroundUri)); alpha = 0.16f; scaleType = ImageView.ScaleType.CENTER_CROP; contentDescription = null }
            root.addView(image, FrameLayout.LayoutParams(-1, -1))
        } catch (_: SecurityException) { getSharedPreferences("lia_appearance", MODE_PRIVATE).edit().remove("background_uri").apply() }
        root.addView(content, FrameLayout.LayoutParams(-1, -1))
        val title = TextView(this).apply { text = "🌼 Lia"; textSize = 42f; gravity = Gravity.CENTER; setTextColor(Color.rgb(230, 120, 70)) }
        val status = TextView(this).apply {
            text = "${if (localRuntime.isNativeReady()) "Lokale KI aktiv" else "Offline-Grundmodus aktiv"}\nCPU: ${cpu.logicalCores} logische Kerne\nRAM: ${(cap.ramMb / 1024)} GB · Speicher frei: ${(cap.freeInternalMb / 1024)} GB · Leistung: $perf\nWissenspuffer: ${cachePlan.profile} (${cachePlan.maxEntries} Einträge)\nModellspeicher: ${modelStorageReport.userSummary()}${if (modelStorageReport.usesExternalAppStorage) " · erweiterter App-Speicher" else ""}"
            textSize = 18f; gravity = Gravity.CENTER
        }
        modelStatusView = status
        val installModel = Button(this).apply { text = "🧠 Lokales Modell installieren"; textSize = 18f }
        val offlineStatus = status.text.toString()
        val chat = EditText(this).apply { hint = "Schreib mir etwas …"; textSize = 21f; minLines = 2; setPadding(16, 12, 16, 12) }
        val send = Button(this).apply { text = "💬 Mit Lia sprechen"; textSize = 20f }
        val listen = Button(this).apply { text = "🎙️ Mit Lia sprechen"; textSize = 20f }
        val dailyListen = Button(this).apply { text = "🎙️ Tagesantwort sprechen"; textSize = 18f }
        val reply = TextView(this).apply { textSize = 21f; setPadding(0, 16, 0, 16); gravity = Gravity.CENTER }
        permissionFeedback = reply
        val remember = Button(this).apply { text = "📚 Dieses Wissen merken"; textSize = 18f; isEnabled = false }
        var lastCandidate: ConfirmedKnowledgeCandidate? = null
        var lastAnswerText = ""
        val question = TextView(this).apply { textSize = 23f; gravity = Gravity.CENTER; setPadding(0, 16, 0, 12) }
        val answer = EditText(this).apply { hint = "Tagesantwort (freiwillig)"; textSize = 20f; minLines = 2 }
        val save = Button(this).apply { text = "💾 Antwort speichern"; textSize = 18f }
        val skip = Button(this).apply { text = "➡️ Später beantworten"; textSize = 18f }
        val web = Button(this).apply { text = "Internet: $webMode"; textSize = 16f }
        val admin = Button(this).apply { text = "Wartung / WLAN"; textSize = 16f }
        fun showCurrent() {
            val complete = index >= dailyPlan.prompts.size
            question.text = if (complete) "🌷 Für heute sind alle freiwilligen Impulse geschafft." else dailyPlan.prompts[index].prompt
            answer.isEnabled = !complete
            save.isEnabled = !complete
            skip.isEnabled = !complete
        }
        fun next() {
            index = promptProgress.markHandled(today, index, dailyPlan.prompts.size)
            answer.text.clear()
            showCurrent()
        }
        showCurrent()
        fun presentReply(text: String) {
            reply.text = text
            speaker?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lia-reply")
        }
        fun presentAnswer(questionText: String, result: OrchestratedAnswer) {
            val sourceNote = KnowledgePresentation.sourceLine(result.knowledgeProvenance)
            reply.text = result.text + sourceNote
            lastAnswerText = result.text
            lastCandidate = ConfirmedKnowledgeCandidate.from(questionText, result)
            remember.isEnabled = true
            speaker?.speak(result.text, TextToSpeech.QUEUE_FLUSH, null, "lia-reply")
        }
        fun handleConversation(text: String) {
            val boundedText = text.trim()
            if (boundedText.isEmpty()) {
                presentReply("Ich höre dir gern zu.")
                return
            }
            reply.text = "Ich denke kurz nach …"
            uiScope.launch {
                val prompt = PromptContext.build(
                    userText = boundedText,
                    profile = profile.confirmed(),
                    onlineAllowed = WebNetworkAdmission.allowed(webMode, webConsent)
                )
                val result = answerOrchestrator.answer(boundedText, prompt)
                presentAnswer(boundedText, result)
            }
        }
        remember.setOnClickListener {
            val candidate = lastCandidate
            if (candidate == null) {
                profile.add(LearningItem("personal-" + System.currentTimeMillis(), lastAnswerText, confirmed = true))
                reply.text = "Als persönliche Erinnerung lokal verschlüsselt gespeichert."
                remember.isEnabled = false
                return@setOnClickListener
            }
            val saved = confirmedKnowledge.saveConfirmed(candidate.summary, candidate.sourceLabels, candidate.fingerprint)
            reply.text = if (saved) "Dieses Wissen wurde lokal verschlüsselt gespeichert." else "Dieses Wissen konnte aus Sicherheitsgründen nicht gespeichert werden."
            if (saved) { lastCandidate = null; remember.isEnabled = false }
        }
        installModel.setOnClickListener {
            val entry = ModelCatalog.entries.first()
            val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 8, 32, 0) }
            val details = TextView(this).apply {
                text = "Quelle: " + entry.sourceUrl + "\nLizenz: " + entry.license + "\nDie Datei wird automatisch gegen diesen SHA-256 geprüft: " + entry.sha256 + "\nLade genau diese GGUF-Datei manuell herunter. Lia lädt kein Modell automatisch."
                textSize = 15f
            }
            layout.addView(details)
            AlertDialog.Builder(this).setTitle(entry.displayName).setView(layout)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Datei wählen") { _, _ ->
                    startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/octet-stream"
                    }, 73)
                }.show()
        }
        send.setOnClickListener {
            handleConversation(chat.text.toString())
            chat.text.clear()
        }
        dailyListen.setOnClickListener { pendingDailySpeech = true; listen.performClick() }
        listen.setOnClickListener {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) { pendingDailySpeech = false; reply.text = "Spracherkennung ist nicht verfügbar. Du kannst mich jederzeit schreiben."; return@setOnClickListener }
            if (checkSelfPermission("android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf("android.permission.RECORD_AUDIO"), 42); return@setOnClickListener }
            if (recognizer == null) recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply { setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: android.os.Bundle) {
                    val t = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    if (pendingDailySpeech) { answer.setText(t); pendingDailySpeech = false; if (t.isNotBlank()) reply.text = "Tagesantwort übernommen." ; return }
                    chat.setText(t)
                    if (t.isBlank()) {
                        presentReply("Ich habe nichts verstanden.")
                    } else {
                        handleConversation(t)
                    }
                }
                override fun onError(error: Int) { pendingDailySpeech = false; reply.text = "Ich konnte dich gerade nicht verstehen. Bitte versuche es noch einmal oder schreibe mir." }
                override fun onReadyForSpeech(p: android.os.Bundle?) { reply.text = "Ich höre zu …" }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(b: android.os.Bundle?) {}
                override fun onEvent(t: Int, b: android.os.Bundle?) {}
            }) }
            try {
                recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE"); putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_PROMPT, "Ich höre zu") })
            } catch (_: SecurityException) {
                pendingDailySpeech = false
                reply.text = "Mikrofonzugriff fehlt. Du kannst Lia jederzeit schreiben."
            }
        }
        save.setOnClickListener {
            if (index >= dailyPlan.prompts.size) return@setOnClickListener
            if (answer.text.isNullOrBlank()) reply.text = "Keine Antwort gespeichert."
            else {
                profile.add(LearningItem(dailyPlan.prompts[index].id, answer.text.toString()))
                reply.text = "Danke. Lokal verschlüsselt gespeichert."
            }
            next()
        }
        skip.setOnClickListener { reply.text = "Übersprungen – das ist jederzeit in Ordnung."; next() }
        fun refreshWebButton() {
            web.text = if (WebNetworkAdmission.allowed(webMode, webConsent)) "Internet: anonymisierte Suche EIN" else "Internet: AUS"
        }
        refreshWebButton()
        web.setOnClickListener {
            if (WebNetworkAdmission.allowed(webMode, webConsent)) {
                webConsent = false
                consentStore.setWebEnabled(false)
                webMode = WebAccessMode.OFFLINE
                webStore.set(webMode)
                refreshWebButton()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Anonymisierte Websuche")
                    .setMessage("Wenn du zustimmst, darf Lia allgemeine Wissensfragen anonymisiert an geprüfte öffentliche Quellen senden. Persönliche Fragen bleiben auf diesem Gerät. Du kannst die Freigabe jederzeit hier ausschalten.")
                    .setNegativeButton("Offline bleiben", null)
                    .setPositiveButton("Anonymisierte Suche erlauben") { _, _ ->
                        webConsent = true
                        consentStore.setWebEnabled(true)
                        webMode = WebAccessMode.AUTO_ANONYMIZED_GENERIC
                        webStore.set(webMode)
                        refreshWebButton()
                    }
                    .show()
            }
        }
        admin.setOnClickListener {
            val kiosk = KioskController(this)
            if (!kiosk.hasAdminPin()) {
                val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 8, 48, 0) }
                val first = EditText(this).apply { hint = "Neue Admin-PIN (6–12 Ziffern)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
                val second = EditText(this).apply { hint = "PIN wiederholen"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
                layout.addView(first); layout.addView(second)
                AlertDialog.Builder(this).setTitle("Geschützten Bereich einrichten")
                    .setMessage("Für WLAN, Kiosk und Wartung wird eine Admin-PIN benötigt.")
                    .setView(layout).setNegativeButton("Abbrechen", null)
                    .setPositiveButton("Einrichten") { _, _ ->
                        try {
                            if (first.text.toString() != second.text.toString()) throw IllegalArgumentException()
                            kiosk.setAdminPin(first.text.toString())
                            startActivity(Intent(this, AdminActivity::class.java).putExtra("admin_authorized", true))
                        } catch (_: IllegalArgumentException) {
                            Toast.makeText(this, "Bitte zweimal dieselbe PIN mit 6–12 Ziffern eingeben.", Toast.LENGTH_LONG).show()
                        }
                    }.show()
            } else {
                val pinInput = EditText(this).apply { hint = "Admin-PIN"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
                AlertDialog.Builder(this).setTitle("Geschützter Bereich")
                    .setMessage("Bitte Admin-PIN eingeben, um Wartung und WLAN zu öffnen.")
                    .setView(pinInput).setNegativeButton("Abbrechen", null)
                    .setPositiveButton("Öffnen") { _, _ ->
                        if (kiosk.disableWithPin(pinInput.text.toString())) {
                            startActivity(Intent(this, AdminActivity::class.java).putExtra("admin_authorized", true))
                        } else Toast.makeText(this, "PIN nicht korrekt.", Toast.LENGTH_SHORT).show()
                    }.show()
            }
        }
        content.addView(title); content.addView(status); content.addView(installModel); content.addView(chat, LinearLayout.LayoutParams(-1, -2)); content.addView(listen); content.addView(send); content.addView(reply); content.addView(remember); content.addView(question); content.addView(answer, LinearLayout.LayoutParams(-1, 0, 1f)); content.addView(dailyListen); content.addView(save); content.addView(skip); content.addView(web); content.addView(admin); setContentView(root)
        uiScope.launch {
            val modelLoaded = runtimeBootstrap.loadInstalled(localRuntime)
            if (modelLoaded) status.text = "Lokale KI aktiv\n" + offlineStatus.substringAfter('\n')
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 73) return
        val uri = data?.data
        if (resultCode != RESULT_OK || uri == null) return
        val entry = ModelCatalog.entries.first()
        val directory = ModelStorageLocator.forContext(this).directory
        modelStatusView?.text = "Modell wird geprüft …"
        uiScope.launch {
            val imported = withContext(Dispatchers.IO) {
                ModelInstaller(this@MainActivity, directory).install(uri, entry)
            }
            if (!imported.succeeded || imported.file == null || imported.sha256 == null) {
                modelStatusView?.text = "Offline-Grundmodus aktiv\n" + offlineStatus.removePrefix("Offline-Grundmodus aktiv\n")
                Toast.makeText(this@MainActivity, imported.error ?: "Modellimport fehlgeschlagen.", Toast.LENGTH_LONG).show()
                return@launch
            }
            val loaded = withContext(Dispatchers.Default) {
                localRuntime.load(entry.spec(), imported.file!!)
            }
            modelStatusView?.text = if (loaded) {
                "Lokale KI aktiv · " + entry.displayName + " · SHA-256 geprüft\n" + offlineStatus.removePrefix("Offline-Grundmodus aktiv\n")
            } else {
                "Modell geprüft, aber Runtime konnte es nicht laden. Offline-Grundmodus aktiv.\n" + offlineStatus.removePrefix("Offline-Grundmodus aktiv\n")
            }
            Toast.makeText(this@MainActivity,
                if (loaded) "Lokales Modell geprüft und geladen." else "Modell ist geprüft, konnte aber nicht gestartet werden.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 42) return
        pendingDailySpeech = false
        permissionFeedback?.text = if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            "Mikrofon freigegeben. Tippe zum Sprechen erneut; Texteingabe bleibt verfügbar."
        } else {
            "Mikrofonzugriff nicht freigegeben. Du kannst Lia jederzeit schreiben."
        }
    }

    override fun onDestroy() {
        permissionFeedback = null
        uiScope.cancel()
        recognizer?.destroy()
        speaker?.shutdown()
        localRuntime.close()
        super.onDestroy()
    }
}
