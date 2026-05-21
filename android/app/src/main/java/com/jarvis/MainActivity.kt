package com.jarvis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var adapter: MessageAdapter
    private lateinit var jarvisClient: JarvisClient
    private lateinit var statusText: TextView
    private lateinit var textInput: EditText

    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        textInput = findViewById(R.id.textInput)
        val sendButton: ImageButton = findViewById(R.id.sendButton)
        val micButton: FloatingActionButton = findViewById(R.id.micButton)
        val recyclerView: RecyclerView = findViewById(R.id.chatRecyclerView)
        val title: TextView = findViewById(R.id.title)

        adapter = MessageAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recyclerView.adapter = adapter

        // Init Client from SharedPrefs
        val prefs = getSharedPreferences("JarvisPrefs", Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "ws://100.64.0.1:3000/jarvis/stream")!!
        jarvisClient = JarvisClient(serverUrl, this)

        setupSTT()
        setupTTS()

        sendButton.setOnClickListener {
            val text = textInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                textInput.text.clear()
            }
        }

        micButton.setOnClickListener { checkPermissionAndListen() }
        title.setOnClickListener { showSettingsDialog() }
        title.setOnLongClickListener {
            Toast.makeText(this, "Server: $serverUrl", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun sendMessage(text: String) {
        adapter.addMessage(Message(text, true))
        jarvisClient.sendMessage(text)
    }

    private fun checkPermissionAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        } else {
            startListening()
        }
    }

    private fun setupSTT() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { onStatusUpdate("Jarvis ascultă...") }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) sendMessage(matches[0])
            }
            override fun onError(e: Int) { onStatusUpdate("Eroare voce") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(r: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() { onStatusUpdate("Procesez...") }
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(ev: Int, p: Bundle?) {}
        })
    }

    private fun setupTTS() {
        tts = TextToSpeech(this) { s -> if (s != TextToSpeech.ERROR) tts.language = Locale("ro", "RO") }
    }

    private fun startListening() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO")
        }
        speechRecognizer.startListening(i)
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val urlInput = dialogView.findViewById<EditText>(R.id.urlInput)
        val prefs = getSharedPreferences("JarvisPrefs", Context.MODE_PRIVATE)
        urlInput.setText(prefs.getString("server_url", "ws://100.64.0.1:3000/jarvis/stream"))

        AlertDialog.Builder(this)
            .setTitle("Configurare Jarvis")
            .setView(dialogView)
            .setPositiveButton("Salvează") { _, _ ->
                val newUrl = urlInput.text.toString()
                prefs.edit().putString("server_url", newUrl).apply()
                jarvisClient.reconnect(newUrl)
                onStatusUpdate("Reconectare...")
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    fun onStreamingStart() {
        runOnUiThread { adapter.addMessage(Message("", false)) }
    }

    fun onStreamingChunk(chunk: String) {
        runOnUiThread { adapter.updateLastMessage(chunk) }
    }

    fun onJarvisResponse(response: JSONObject) {
        runOnUiThread {
            val text = response.optString("text")
            if (response.optString("type") == "full") {
                adapter.addMessage(Message(text, false))
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

            val actions = response.optJSONArray("actions")
            for (i in 0 until (actions?.length() ?: 0)) {
                handleAction(actions!!.getJSONObject(i))
            }
            onStatusUpdate("Conectat")
        }
    }

    fun onStatusUpdate(msg: String) {
        runOnUiThread {
            statusText.text = msg
            when(msg) {
                "Conectat" -> statusText.setTextColor(0xFF00E5FF.toInt())
                "Deconectat", "Eroare Conexiune" -> statusText.setTextColor(0xFFFF5252.toInt())
                else -> statusText.setTextColor(0xFFFFFFFF.toInt())
            }
        }
    }

    private fun handleAction(action: JSONObject) {
        try {
            when (action.getString("type")) {
                "open_url" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.getString("url"))))
                "open_app" -> {
                    val intent = packageManager.getLaunchIntentForPackage(action.getString("package"))
                    if (intent != null) startActivity(intent)
                }
                "open_settings" -> startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
