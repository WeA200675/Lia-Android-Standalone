package de.wea200675.lia.admin

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.WindowManager
import java.security.MessageDigest

class KioskController(private val activity: Activity) {
    private val prefs: SharedPreferences = activity.getSharedPreferences("lia_admin", Context.MODE_PRIVATE)
    fun isEnabled()=prefs.getBoolean("kiosk_enabled",false)
    fun enable(){ prefs.edit().putBoolean("kiosk_enabled",true).apply(); activity.startLockTask(); activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    fun disableWithPin(pin:String):Boolean {
        val stored=prefs.getString("admin_pin_hash",null) ?: return false
        if(hash(pin)!=stored) return false
        activity.stopLockTask(); prefs.edit().putBoolean("kiosk_enabled",false).apply(); return true
    }
    fun setAdminPin(pin:String){ require(pin.length>=6); prefs.edit().putString("admin_pin_hash",hash(pin)).apply() }
    private fun hash(value:String)=MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString(""){"%02x".format(it)}
}
