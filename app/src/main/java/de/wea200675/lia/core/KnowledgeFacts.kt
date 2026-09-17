package de.wea200675.lia.core

object KnowledgeFacts {
 private val facts=listOf("Bienen können Gesichter anhand von Mustern unterscheiden.","Das menschliche Herz schlägt im Laufe eines Lebens viele Milliarden Mal.","Der Geruch von frisch gemähtem Gras entsteht durch Pflanzenstoffe als Stresssignal.")
 fun random(seed:Long=System.currentTimeMillis())=facts[(seed and Long.MAX_VALUE).toInt()%facts.size]
}
