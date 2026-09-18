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
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import de.wea200675.lia.admin.AdminActivity
import de.wea200675.lia.core.*
import de.wea200675.lia.background.DailyLearningWorker

class MainActivity : Activity() {
    private var index = 0
    private lateinit var profile: EncryptedLearningProfile
    private var recognizer: SpeechRecognizer? = null
    private var speaker: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profile = EncryptedLearningProfile(this)
        DailyLearningWorker.schedule(this)
        speaker = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) speaker?.language = Locale.GERMAN }
        val router = ConversationRouter()
        val cpu = CpuProfiles.detect()
        val cap = DeviceCapabilityProbe.read(this)
        val perf = ResourceGovernor(this).level()
        val localRuntime = ModelRuntimeBootstrap(this).create()
        val webStore = WebModeStore(this)
        var webMode = webStore.get()
        val today = LocalDate.now()
        val secureStore = AndroidSecureStore(this)
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
            text = "${if (localRuntime.isNativeReady()) "Lokale KI aktiv" else "Offline-Grundmodus aktiv"}\nCPU: ${cpu.logicalCores} logische Kerne\nRAM: ${(cap.ramMb / 1024)} GB · Speicher frei: ${(cap.freeInternalMb / 1024)} GB · Leistung: $perf"
            textSize = 18f; gravity = Gravity.CENTER
        }
        val chat = EditText(this).apply { hint = "Schreib mir etwas …"; textSize = 21f; minLines = 2; setPadding(16, 12, 16, 12) }
        val send = Button(this).apply { text = "💬 Mit Lia sprechen"; textSize = 20f }
        val listen = Button(this).apply { text = "🎙️ Sprechen"; textSize = 20f }
        val reply = TextView(this).apply { textSize = 21f; setPadding(0, 16, 0, 16); gravity = Gravity.CENTER }
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
        send.setOnClickListener { val t = chat.text.toString(); reply.text = if (t.isBlank()) "Ich höre dir gern zu." else router.offlineReply(router.classify(t)); speaker?.speak(reply.text, TextToSpeech.QUEUE_FLUSH, null, "lia-reply"); chat.text.clear() }
        listen.setOnClickListener {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) { reply.text = "Spracherkennung ist nicht verfügbar. Du kannst mich jederzeit schreiben."; return@setOnClickListener }
            if (checkSelfPermission("android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf("android.permission.RECORD_AUDIO"), 42); return@setOnClickListener }
            if (recognizer == null) recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply { setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: android.os.Bundle) { val t = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); chat.setText(t); reply.text = if (t.isBlank()) "Ich habe nichts verstanden." else router.offlineReply(router.classify(t)); speaker?.speak(reply.text, TextToSpeech.QUEUE_FLUSH, null, "lia-reply") }
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
        admin.setOnClickListener { startActivity(Intent(this, AdminActivity::class.java)) }
        root.addView(title); root.addView(status); root.addView(chat, LinearLayout.LayoutParams(-1, -2)); root.addView(listen); root.addView(send); root.addView(reply); root.addView(question); root.addView(answer, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(save); root.addView(skip); root.addView(web); root.addView(admin); setContentView(root)
    }
}
