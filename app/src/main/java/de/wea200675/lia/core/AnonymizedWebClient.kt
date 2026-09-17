package de.wea200675.lia.core

import java.net.HttpURLConnection
import java.net.URL

class AnonymizedWebClient(private val endpoint:String, private val policy:WebPolicy) : WebGateway {
 override suspend fun query(anonymizedQuery:String):Result<String> = runCatching {
  val safe=policy.prepare(anonymizedQuery).getOrThrow()
  require(endpoint.startsWith("https://")) { "Nur HTTPS-Endpunkte sind erlaubt" }
  val c=(URL(endpoint).openConnection() as HttpURLConnection).apply { requestMethod="POST"; connectTimeout=8000; readTimeout=12000; doOutput=true; setRequestProperty("Content-Type","text/plain; charset=utf-8"); setRequestProperty("Cache-Control","no-store"); useCaches=false }
  c.outputStream.use{it.write(safe.toByteArray(Charsets.UTF_8))}
  if(c.responseCode !in 200..299) error("Webgateway antwortete mit HTTP ${c.responseCode}")
  c.inputStream.bufferedReader().use{it.readText().take(12000)}
 }.recoverCatching{throw it}
}
