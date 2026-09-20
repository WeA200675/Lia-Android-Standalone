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
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
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
    private lateinit var localRuntime: SupervisedLocalRuntime
    private lateinit var answerOrchestrator: AnswerOrchestrator
    private lateinit var confirmedKnowledge: ConfirmedKnowledgeRepository
    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profile = EncryptedLearningProfile(this)
        DailyLearningWorker.schedule(this)
        speaker = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) speaker?.language = Locale.GERMAN }
        val router = ConversationRouter()
        val cpu = CpuProfiles.detect()
        val cap = DeviceCapabilityProbe.read(this)
        val perf = ResourceGovernor(this).level()
        val modelStorage = ModelStorageLocator.forContext(this)
        val modelStorageReport = ModelStorageReporter.forDirectory(modelStorage)
        localRuntime = ModelRuntimeBootstrap(this).createSupervised()
        val webStore = WebModeStore(this)
        var webMode = webStore.get()
        val cachePlan = KnowledgeCacheCapacity.recommend(
            ramMb = cap.ramMb.toLong(),
            sourceCount = KnowledgeSourceCatalog.sources.size
        )
        val secureStore = AndroidSecureStore(this)
        confirmedKnowledge = ConfirmedKnowledgeRepository(secureStore)
        answerOrchestrator = AnswerOrchestrator(
            localRuntime,
            SafeWikipediaGateway(
                enabled = { webMode != WebAccessMode.OFFLINE },
                cache = KnowledgeSessionRuntime.configureCache(cachePlan.maxEntries),
                sourceHealth = KnowledgeSessionRuntime.sourceHealth
            ),
            confirmedKnowledge = confirmedKnowledge
        )
        val today = LocalDate.now()
        val dailyPlan = DailyPlanRepository(TrainingCache(secureStore)).forDate(today)
        val promptProgress = DailyPromptProgress(secureStore)
        index = promptProgress.nextIndex(today, dailyPlan.prompts.size)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(255, 248, 240))
        }
        val title = TextView(this).apply { text = "🌼 Lia"; textSize = 42f; gravity = Gravity.CENTER; setTextColor(Color.rgb(230, 120, 70)) }
        val status = TextView(this).apply {
            text = "${if (localRuntime.isNativeReady()) "Lokale KI aktiv" else "Offline-Grundmodus aktiv"}\nCPU: ${cpu.logicalCores} logische Kerne\nRAM: ${(cap.ramMb / 1024)} GB · Speicher frei: ${(cap.freeInternalMb / 1024)} GB · Leistung: $perf\nWissenspuffer: ${cachePlan.profile} (${cachePlan.maxEntries} Einträge)\nModellspeicher: ${modelStorageReport.userSummary()}${if (modelStorageReport.usesExternalAppStorage) " · erweiterter App-Speicher" else ""}"
            textSize = 18f; gravity = Gravity.CENTER
        }
        val chat = EditText(this).apply { hint = "Schreib mir etwas …"; textSize = 21f; minLines = 2; setPadding(16, 12, 16, 12) }
        val send = Button(this).apply { text = "💬 Mit Lia sprechen"; textSize = 20f }
        val listen = Button(this).apply { text = "🎙️ Sprechen"; textSize = 20f }
        val reply = TextView(this).apply { textSize = 21f; setPadding(0, 16, 0, 16); gravity = Gravity.CENTER }
        val remember = Button(this).apply { text = "📚 Dieses Wissen merken"; textSize = 18f; isEnabled = false }
        var lastCandidate: ConfirmedKnowledgeCandidate? = null
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
            val provenance = result.knowledgeProvenance
            val sourceNote = if (provenance == null) "" else {
                val origin = when (provenance.origin) {
                    KnowledgeOrigin.LIVE -> "frisch abgerufen"
                    KnowledgeOrigin.SESSION_CACHE -> "aus dem flüchtigen Wissenspuffer"
                    KnowledgeOrigin.CONFIRMED_STORE -> "aus dem bestätigten lokalen Wissen"
                }
                "\n\nℹ Quellen: ${provenance.sourceLabels.joinToString(", ")} · $origin"
            }
            reply.text = result.text + sourceNote
            lastCandidate = ConfirmedKnowledgeCandidate.from(questionText, result)
            remember.isEnabled = lastCandidate != null
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
                    onlineAllowed = webMode != WebAccessMode.OFFLINE
                )
                val result = answerOrchestrator.answer(boundedText, prompt)
                presentAnswer(boundedText, result)
            }
        }
        remember.setOnClickListener {
            val candidate = lastCandidate
            if (candidate == null) {
                reply.text = "Diese Antwort hat keine überprüfbare Quelle und wird nicht dauerhaft gespeichert."
                return@setOnClickListener
            }
            val saved = confirmedKnowledge.saveConfirmed(candidate.summary, candidate.sourceLabels, candidate.fingerprint)
            reply.text = if (saved) "Dieses Wissen wurde lokal verschlüsselt gespeichert." else "Dieses Wissen konnte aus Sicherheitsgründen nicht gespeichert werden."
            if (saved) { lastCandidate = null; remember.isEnabled = false }
        }
        send.setOnClickListener {
            handleConversation(chat.text.toString())
            chat.text.clear()
        }
        listen.setOnClickListener {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) { reply.text = "Spracherkennung ist nicht verfügbar. Du kannst mich jederzeit schreiben."; return@setOnClickListener }
            if (checkSelfPermission("android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf("android.permission.RECORD_AUDIO"), 42); return@setOnClickListener }
            if (recognizer == null) recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply { setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: android.os.Bundle) {
                    val t = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    chat.setText(t)
                    if (t.isBlank()) {
                        presentReply("Ich habe nichts verstanden.")
                    } else {
                        handleConversation(t)
                    }
                }
                override fun onError(error: Int) { reply.text = "Ich konnte dich gerade nicht verstehen. Bitte versuche es noch einmal oder schreibe mir." }
                override fun onReadyForSpeech(p: android.os.Bundle?) { reply.text = "Ich höre zu …" }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(b: android.os.Bundle?) {}
                override fun onEvent(t: Int, b: android.os.Bundle?) {}
            }) }
            recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE"); putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_PROMPT, "Ich höre zu") })
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
        web.setOnClickListener { webMode = when (webMode) { WebAccessMode.OFFLINE -> WebAccessMode.AUTO_ANONYMIZED_GENERIC; WebAccessMode.AUTO_ANONYMIZED_GENERIC -> WebAccessMode.ASK_BEFORE_PERSONAL; else -> WebAccessMode.OFFLINE }; webStore.set(webMode); web.text = "Internet: $webMode" }
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
        root.addView(title); root.addView(status); root.addView(chat, LinearLayout.LayoutParams(-1, -2)); root.addView(listen); root.addView(send); root.addView(reply); root.addView(remember); root.addView(question); root.addView(answer, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(save); root.addView(skip); root.addView(web); root.addView(admin); setContentView(root)
    }

    override fun onDestroy() {
        uiScope.cancel()
        recognizer?.destroy()
        speaker?.shutdown()
        localRuntime.close()
        super.onDestroy()
    }
}
