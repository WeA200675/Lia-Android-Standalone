package de.wea200675.lia.core

import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager

enum class PerformanceLevel { MAXIMUM, BALANCED, RECOVERY }

class ResourceGovernor(context:Context) {
 private val battery=context.getSystemService(BatteryManager::class.java)
 private val power=context.getSystemService(PowerManager::class.java)
 fun level():PerformanceLevel {
  val pct=battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
  val thermal=if(android.os.Build.VERSION.SDK_INT>=29) power.currentThermalStatus else PowerManager.THERMAL_STATUS_NONE
  return when { thermal>=PowerManager.THERMAL_STATUS_SEVERE || pct<10 -> PerformanceLevel.RECOVERY
   thermal>=PowerManager.THERMAL_STATUS_MODERATE || pct<20 -> PerformanceLevel.BALANCED
   else -> PerformanceLevel.MAXIMUM }
 }
}
