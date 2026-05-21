package com.jarvis

import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class JarvisClient(private val serverUrl: String, private val activity: MainActivity) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentAiText = StringBuilder()

    init {
        connect()
    }

    private fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val data = JSONObject(text)
                when (data.optString("type")) {
                    "full" -> activity.onJarvisResponse(data)
                    "start" -> {
                        currentAiText = StringBuilder()
                        activity.onStreamingStart()
                    }
                    "chunk" -> {
                        val chunk = data.optString("text")
                        currentAiText.append(chunk)
                        activity.onStreamingChunk(chunk)
                    }
                    "end" -> {
                        val finalResponse = JSONObject()
                        finalResponse.put("text", currentAiText.toString())
                        activity.onJarvisResponse(finalResponse)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                activity.runOnUiThread {
                    activity.onStatusUpdate("Conexiune eșuată. Reîncearcă.")
                }
            }
        })
    }

    fun sendMessage(text: String) {
        val message = JSONObject()
        message.put("text", text)
        message.put("user_id", "android_device_01")
        webSocket?.send(message.toString())
    }
}
