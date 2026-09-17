package de.wea200675.lia

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import de.wea200675.lia.admin.AdminActivity
import de.wea200675.lia.core.*

class MainActivity : Activity() {
    private var index = 0
    private lateinit var profile: EncryptedLearningProfile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profile = EncryptedLearningProfile(this)
        val router = ConversationRouter()
        val cpu = CpuProfiles.detect()
        val cap = DeviceCapabilityProbe.read(this)
        val perf = ResourceGovernor(this).level()
        val webStore = WebModeStore(this)
        var webMode = webStore.get()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(250, 248, 255))
        }
        val title = TextView(this).apply { text = "Lia"; textSize = 42f; gravity = Gravity.CENTER; setTextColor(Color.rgb(55, 40, 80)) }
        val status = TextView(this).apply {
            text = "Offline-Grundmodus aktiv\nCPU: ${cpu.logicalCores} logische Kerne\nRAM: ${(cap.ramMb / 1024)} GB · Speicher frei: ${(cap.freeInternalMb / 1024)} GB · Leistung: $perf"
            textSize = 18f; gravity = Gravity.CENTER
        }
        val chat = EditText(this).apply { hint = "Schreib mir etwas …"; textSize = 21f; minLines = 2 }
        val send = Button(this).apply { text = "Mit Lia sprechen"; textSize = 20f }
        val reply = TextView(this).apply { textSize = 21f; setPadding(0, 16, 0, 16); gravity = Gravity.CENTER }
        val question = TextView(this).apply { text = DailyQuestions.defaults[index].text; textSize = 23f; gravity = Gravity.CENTER; setPadding(0, 16, 0, 12) }
        val answer = EditText(this).apply { hint = "Tagesantwort (freiwillig)"; textSize = 20f; minLines = 2 }
        val save = Button(this).apply { text = "Antwort lokal speichern"; textSize = 18f }
        val skip = Button(this).apply { text = "Frage überspringen"; textSize = 18f }
        val web = Button(this).apply { text = "Internet: $webMode"; textSize = 16f }
        val admin = Button(this).apply { text = "Wartung / WLAN"; textSize = 16f }
        fun next() { index = (index + 1) % DailyQuestions.defaults.size; question.text = DailyQuestions.defaults[index].text; answer.text.clear() }
        send.setOnClickListener { val t = chat.text.toString(); reply.text = if (t.isBlank()) "Ich höre dir gern zu." else router.offlineReply(router.classify(t)); chat.text.clear() }
        save.setOnClickListener { if (answer.text.isNullOrBlank()) reply.text = "Keine Antwort gespeichert." else { profile.add(LearningItem(DailyQuestions.defaults[index].id, answer.text.toString())); reply.text = "Danke. Lokal verschlüsselt gespeichert." }; next() }
        skip.setOnClickListener { reply.text = "Übersprungen – das ist jederzeit in Ordnung."; next() }
        web.setOnClickListener { webMode = when (webMode) { WebAccessMode.OFFLINE -> WebAccessMode.AUTO_ANONYMIZED_GENERIC; WebAccessMode.AUTO_ANONYMIZED_GENERIC -> WebAccessMode.ASK_BEFORE_PERSONAL; else -> WebAccessMode.OFFLINE }; webStore.set(webMode); web.text = "Internet: $webMode" }
        admin.setOnClickListener { startActivity(Intent(this, AdminActivity::class.java)) }
        root.addView(title); root.addView(status); root.addView(chat, LinearLayout.LayoutParams(-1, -2)); root.addView(send); root.addView(reply); root.addView(question); root.addView(answer, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(save); root.addView(skip); root.addView(web); root.addView(admin); setContentView(root)
    }
}
