package de.wea200675.lia.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale
import de.wea200675.lia.core.SpeechToText

class AndroidSpeech(private val context: Context) : SpeechToText {
    private var recognizer: SpeechRecognizer? = null
    override suspend fun listen(): Result<String> = Result.failure(UnsupportedOperationException("Use listenOnce callback on Android main thread"))
    fun listenOnce(onResult:(Result<String>)->Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) { onResult(Result.failure(IllegalStateException("Spracherkennung nicht verfügbar"))); return }
        recognizer?.destroy()
        recognizer=SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object: RecognitionListener {
                override fun onResults(results: android.os.Bundle) { onResult(Result.success(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty())) }
                override fun onError(error:Int) { onResult(Result.failure(IllegalStateException("Spracherkennung Fehler $error"))) }
                override fun onReadyForSpeech(p0:android.os.Bundle?){}; override fun onBeginningOfSpeech(){}; override fun onRmsChanged(p0:Float){}; override fun onBufferReceived(p0:ByteArray?){}; override fun onEndOfSpeech(){}; override fun onPartialResults(p0:android.os.Bundle?){}; override fun onEvent(p0:Int,p1:android.os.Bundle?){}
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN.toLanguageTag()); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false) })
        }
    }
    fun close(){ recognizer?.destroy(); recognizer=null }
}

class AndroidVoiceOutput(context: Context) : TextToSpeech.OnInitListener, de.wea200675.lia.core.TextToSpeech {
    private val tts=TextToSpeech(context,this)
    private var ready=false
    override fun onInit(status:Int){ ready=status==TextToSpeech.SUCCESS; if(ready) tts.language=Locale.GERMAN }
    override fun speak(text:String){ if(ready) tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"lia") }
    fun close(){ tts.shutdown() }
}
