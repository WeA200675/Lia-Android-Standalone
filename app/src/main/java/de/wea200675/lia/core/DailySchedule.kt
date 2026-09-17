package de.wea200675.lia.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailySchedule {
 private val day=SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(Date())
 fun shouldAsk(lastAskedDay:String?):Boolean = lastAskedDay != day
 fun todayKey()=day
}
