package de.wea200675.lia

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import de.wea200675.lia.admin.AdminActivity
import de.wea200675.lia.core.CpuProfiles
import de.wea200675.lia.core.DailyQuestions

class MainActivity : Activity() {
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cpu = CpuProfiles.detect()
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(32,32,32,32); setBackgroundColor(Color.rgb(250,248,255)) }
        val title=TextView(this).apply { text="Lia"; textSize=42f; gravity=Gravity.CENTER; setTextColor(Color.rgb(55,40,80)) }
        val status=TextView(this).apply { text="Offline-Grundmodus aktiv\nCPU: ${cpu.logicalCores} logische Kerne"; textSize=19f; gravity=Gravity.CENTER; setPadding(0,24,0,24) }
        val question=TextView(this).apply { text=DailyQuestions.defaults[index].text; textSize=25f; gravity=Gravity.CENTER; setPadding(0,24,0,16) }
        val answer=EditText(this).apply { hint="Deine Antwort (freiwillig)"; textSize=21f; minLines=3; gravity=Gravity.TOP }
        val save=Button(this).apply { text="Antwort lokal speichern"; textSize=20f }
        val skip=Button(this).apply { text="Überspringen"; textSize=20f }
        val admin=Button(this).apply { text="Wartung / WLAN"; textSize=16f }
        val result=TextView(this).apply { textSize=18f; gravity=Gravity.CENTER }
        fun next(){ index=(index+1)%DailyQuestions.defaults.size; question.text=DailyQuestions.defaults[index].text; answer.text.clear(); result.text="" }
        save.setOnClickListener { result.text=if(answer.text.isNullOrBlank()) "Keine Antwort gespeichert." else "Danke. Sie bleibt lokal und wird erst nach Bestätigung gelernt."; next() }
        skip.setOnClickListener { result.text="Übersprungen – das ist jederzeit in Ordnung."; next() }
        admin.setOnClickListener { startActivity(Intent(this, AdminActivity::class.java)) }
        root.addView(title); root.addView(status); root.addView(question); root.addView(answer,LinearLayout.LayoutParams(-1,0,1f)); root.addView(save); root.addView(skip); root.addView(admin); root.addView(result); setContentView(root)
    }
}
