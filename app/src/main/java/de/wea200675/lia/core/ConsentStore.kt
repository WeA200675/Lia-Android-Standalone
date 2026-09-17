package de.wea200675.lia.core

import android.content.Context

class ConsentStore(context:Context) {
 private val p=context.getSharedPreferences("lia_consent",Context.MODE_PRIVATE)
 fun webEnabled()=p.getBoolean("web_enabled",false)
 fun setWebEnabled(enabled:Boolean){p.edit().putBoolean("web_enabled",enabled).apply()}
 fun clear(){p.edit().clear().apply()}
}
