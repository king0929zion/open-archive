package com.king0929zion.openarchive.ai

import com.king0929zion.openarchive.data.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MultiProviderAiClient(
    private val http: OkHttpClient,
) : AiProviderClient {
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    override suspend fun listModels(provider: AiProvider, apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(provider.baseUrl.trimEnd('/') + "/models")
                .applyHeaders(provider.format, apiKey)
                .get()
                .build()
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val root = JSONObject(response.body.string())
                val array = root.optJSONArray("data") ?: root.optJSONArray("models") ?: JSONArray()
                buildList {
                    for (i in 0 until array.length()) {
                        val item = array.opt(i)
                        val id = when (item) {
                            is String -> item
                            is JSONObject -> item.optString("id")
                            else -> ""
                        }
                        if (id.isNotBlank()) add(id)
                    }
                }.distinct().sorted()
            }
        }

    override fun streamChat(
        provider: AiProvider,
        apiKey: String,
        modelId: String,
        messages: List<AiChatMessage>,
    ): Flow<String> = callbackFlow {
        val request = buildChatRequest(provider, apiKey, modelId, messages)
        val call = http.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val body = runCatching { response.body.string() }.getOrDefault("")
                    response.close()
                    close(IllegalStateException("HTTP ${response.code}${if (body.isNotBlank()) ": $body" else ""}"))
                    return
                }
                try {
                    response.use { res ->
                        val source = res.body.source()
                        var emitted = false
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val raw = line.removePrefix("data:").trim()
                            if (raw.isBlank() || raw == "[DONE]") continue
                            parseDelta(provider.format, raw)?.takeIf { it.isNotEmpty() }?.let {
                                emitted = true
                                trySend(it)
                            }
                        }
                        if (!emitted) {
                            // Some compatible endpoints ignore stream=true and return a single JSON payload.
                        }
                    }
                    close()
                } catch (t: Throwable) {
                    close(t)
                }
            }
        })
        awaitClose { call.cancel() }
    }

    private fun buildChatRequest(
        provider: AiProvider,
        apiKey: String,
        modelId: String,
        messages: List<AiChatMessage>,
    ): Request {
        val base = provider.baseUrl.trimEnd('/')
        val format = provider.format.lowercase()
        val body = when (format) {
            "anthropic" -> JSONObject().apply {
                put("model", modelId)
                put("max_tokens", 2048)
                put("stream", true)
                put("messages", JSONArray().apply {
                    messages.filter { it.role != "system" }.forEach { m ->
                        put(JSONObject().put("role", m.role).put("content", m.text))
                    }
                })
                messages.firstOrNull { it.role == "system" }?.let { put("system", it.text) }
            }
            "responses" -> JSONObject().apply {
                put("model", modelId)
                put("stream", true)
                put("input", JSONArray().apply {
                    messages.forEach { m ->
                        put(JSONObject().put("role", m.role).put("content", m.text))
                    }
                })
            }
            else -> JSONObject().apply {
                put("model", modelId)
                put("stream", true)
                put("messages", JSONArray().apply {
                    messages.forEach { m ->
                        put(JSONObject().put("role", m.role).put("content", m.text))
                    }
                })
            }
        }
        val path = when (format) {
            "anthropic" -> "/messages"
            "responses" -> "/responses"
            else -> "/chat/completions"
        }
        return Request.Builder()
            .url(base + path)
            .applyHeaders(format, apiKey)
            .header("Accept", "text/event-stream")
            .post(body.toString().toRequestBody(jsonType))
            .build()
    }

    private fun Request.Builder.applyHeaders(format: String, apiKey: String): Request.Builder = apply {
        if (format.lowercase() == "anthropic") {
            if (apiKey.isNotBlank()) header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
        } else if (apiKey.isNotBlank()) {
            header("Authorization", "Bearer $apiKey")
        }
        header("Content-Type", "application/json")
    }

    private fun parseDelta(format: String, raw: String): String? = runCatching {
        val json = JSONObject(raw)
        when (format.lowercase()) {
            "anthropic" -> {
                if (json.optString("type") == "content_block_delta") {
                    json.optJSONObject("delta")?.optString("text")
                } else null
            }
            "responses" -> when (json.optString("type")) {
                "response.output_text.delta" -> json.optString("delta")
                else -> json.optString("delta").takeIf { it.isNotBlank() }
            }
            else -> json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("delta")
                ?.optString("content")
        }
    }.getOrNull()
}
