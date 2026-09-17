package de.wea200675.lia

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import de.wea200675.lia.core.CpuProfiles
import de.wea200675.lia.core.DailyQuestions

class MainActivity : Activity() {
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cpu = CpuProfiles.detect()
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(32,32,32,32); setBackgroundColor(Color.rgb(250,248,255)) }
        val title=TextView(this).apply { text="Lia"; textSize=42f; gravity=Gravity.CENTER; setTextColor(Color.rgb(55,40,80)) }
        val status=TextView(this).apply { text="Offline-Grundmodus aktiv\nCPU: ${cpu.logicalCores} logische Kerne · Profil: ${if(cpu.smtActive) "SMT/Hyperthreading erkannt" else "alle verfügbaren Kerne"}"; textSize=19f; gravity=Gravity.CENTER; setPadding(0,24,0,24) }
        val question=TextView(this).apply { text=DailyQuestions.defaults[index].text; textSize=25f; gravity=Gravity.CENTER; setPadding(0,24,0,16) }
        val answer=EditText(this).apply { hint="Deine Antwort (freiwillig)"; textSize=21f; minLines=3; gravity=Gravity.TOP; setPadding(20,20,20,20) }
        val save=Button(this).apply { text="Antwort lokal speichern"; textSize=20f }
        val skip=Button(this).apply { text="Überspringen"; textSize=20f }
        val result=TextView(this).apply { textSize=18f; gravity=Gravity.CENTER }
        fun next(){ index=(index+1)%DailyQuestions.defaults.size; question.text=DailyQuestions.defaults[index].text; answer.text.clear(); result.text=""; }
        save.setOnClickListener { result.text=if(answer.text.isNullOrBlank()) "Keine Antwort gespeichert." else "Danke. Die Antwort bleibt lokal und kann später bestätigt werden."; next() }
        skip.setOnClickListener { result.text="Übersprungen – das ist jederzeit in Ordnung."; next() }
        root.addView(title,LinearLayout.LayoutParams(-1,-2)); root.addView(status,LinearLayout.LayoutParams(-1,-2)); root.addView(question,LinearLayout.LayoutParams(-1,-2)); root.addView(answer,LinearLayout.LayoutParams(-1,0,1f)); root.addView(save,LinearLayout.LayoutParams(-1,-2)); root.addView(skip,LinearLayout.LayoutParams(-1,-2)); root.addView(result,LinearLayout.LayoutParams(-1,-2)); setContentView(root)
    }
}
