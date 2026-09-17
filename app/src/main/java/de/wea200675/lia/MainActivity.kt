package de.wea200675.lia

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(32,32,32,32); setBackgroundColor(Color.rgb(250,248,255)) }
        val title = TextView(this).apply { text = "Lia"; textSize = 42f; setTextColor(Color.rgb(55,40,80)); gravity = Gravity.CENTER }
        val welcome = TextView(this).apply { text = "Ich bin da. Wie kann ich heute helfen?\n\nOffline-Grundmodus aktiv"; textSize = 24f; gravity = Gravity.CENTER; setPadding(0,32,0,32) }
        root.addView(title, LinearLayout.LayoutParams(-1, -2)); root.addView(welcome, LinearLayout.LayoutParams(-1, -2)); setContentView(root)
    }
}
