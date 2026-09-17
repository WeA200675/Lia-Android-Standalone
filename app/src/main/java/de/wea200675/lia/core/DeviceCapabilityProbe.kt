package de.wea200675.lia.core

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs

data class DeviceCapabilities(val logicalCores:Int,val ramMb:Int,val freeInternalMb:Long,val externalStorageAvailable:Boolean)
object DeviceCapabilityProbe {
 fun read(context:Context):DeviceCapabilities {
  val mem=ActivityManager.MemoryInfo().also{context.getSystemService(ActivityManager::class.java).getMemoryInfo(it)}
  val stat=StatFs(context.filesDir.absolutePath)
  val external=Environment.getExternalStorageState()==Environment.MEDIA_MOUNTED
  return DeviceCapabilities(Runtime.getRuntime().availableProcessors(),(mem.totalMem/1048576L).toInt(),stat.availableBytes/1048576L,external)
 }
 fun modelTier(c:DeviceCapabilities)=when{c.ramMb>=8192&&c.freeInternalMb>=8192L->"3B_Q4";c.ramMb>=6144&&c.freeInternalMb>=4096L->"2B_Q4";else->"RECOVERY_1_5B_Q4"}
}
