package de.wea200675.lia.core

import android.content.Context

class WebModeStore(context:Context) {
 private val p=context.getSharedPreferences("lia_web_mode",Context.MODE_PRIVATE)
 fun get()=runCatching{WebAccessMode.valueOf(p.getString("mode",WebAccessMode.OFFLINE.name)!!)}.getOrDefault(WebAccessMode.OFFLINE)
 fun set(mode:WebAccessMode){p.edit().putString("mode",mode.name).apply()}
 fun clear(){p.edit().clear().apply()}
}
