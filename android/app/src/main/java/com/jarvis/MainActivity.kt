package com.jarvis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var chatContent: TextView
    private lateinit var jarvisClient: JarvisClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        chatContent = findViewById(R.id.chatContent)
        val micButton: FloatingActionButton = findViewById(R.id.micButton)

        jarvisClient = JarvisClient("ws://YOUR_SERVER_IP:3000", this)

        setupSTT()
        setupTTS()

        micButton.setOnClickListener {
            startListening()
        }
    }

    private fun setupSTT() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { statusText.text = "Ascult..." }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { statusText.text = "Mă gândesc..." }
            override fun onError(error: Int) { statusText.text = "Eroare voce. Încearcă din nou." }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    chatContent.append("\nTu: $text")
                    jarvisClient.sendMessage(text)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupTTS() {
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale("ro", "RO")
            }
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO")
        speechRecognizer.startListening(intent)
    }

    fun onStreamingStart() {
        runOnUiThread {
            statusText.text = "Jarvis scrie..."
            chatContent.append("\nJarvis: ")
        }
    }

    fun onStreamingChunk(chunk: String) {
        runOnUiThread {
            chatContent.append(chunk)
        }
    }

    fun onJarvisResponse(response: JSONObject) {
        runOnUiThread {
            statusText.text = "Gata."
            val text = response.optString("text")
            if (response.has("type") && response.getString("type") == "full") {
                chatContent.append("\nJarvis: $text")
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

            if (response.has("actions")) {
                val actions = response.getJSONArray("actions")
                for (i in 0 until actions.length()) {
                    handleAction(actions.getJSONObject(i))
                }
            }
        }
    }

    fun onStatusUpdate(msg: String) {
        runOnUiThread { statusText.text = msg }
    }

    private fun handleAction(action: JSONObject) {
        when (action.getString("type")) {
            "open_url" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.getString("url")))
                startActivity(intent)
            }
        }
    }
}
