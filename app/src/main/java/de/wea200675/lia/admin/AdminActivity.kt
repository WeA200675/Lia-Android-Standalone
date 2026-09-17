package de.wea200675.lia.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class AdminActivity : Activity() {
    private lateinit var kiosk: KioskController
    override fun onCreate(state:Bundle?) {
        super.onCreate(state); kiosk=KioskController(this)
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(32,32,32,32) }
        val title=TextView(this).apply { text="Lia Admin"; textSize=32f; gravity=Gravity.CENTER }
        val pin=EditText(this).apply { hint="Admin-PIN"; inputType=2 or 16; textSize=22f }
        val unlock=Button(this).apply { text="Kiosk verlassen"; textSize=20f }
        val wifi=Button(this).apply { text="WLAN-Einstellungen öffnen"; textSize=20f }
        val status=TextView(this).apply { textSize=18f; gravity=Gravity.CENTER }
        unlock.setOnClickListener { status.text=if(kiosk.disableWithPin(pin.text.toString())) "Admin-Modus geöffnet." else "PIN nicht korrekt." }
        wifi.setOnClickListener { if(!kiosk.disableWithPin(pin.text.toString())) status.text="Bitte zuerst mit der PIN freigeben." else startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
        box.addView(title); box.addView(pin); box.addView(unlock); box.addView(wifi); box.addView(status); setContentView(box)
    }
}
