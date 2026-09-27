package com.example.data.api

import com.example.BuildConfig
import com.example.data.local.DefaultProjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService(
    private val customApiKeyProvider: () -> String? = { null }
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        val customKey = customApiKeyProvider()?.trim()
        if (!customKey.isNullOrEmpty()) return customKey

        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun generateCodingResponse(
        userPrompt: String,
        contextCode: String? = null,
        activeFileName: String? = null,
        chatHistory: List<Pair<String, String>> = emptyList(), // Pair(role, text)
        modelName: String = "gemini-3.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            // Provide offline intelligent coding assistant fallback
            return@withContext Result.success(getSmartOfflineResponse(userPrompt, contextCode, activeFileName))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Include system-like instruction
            val systemContext = buildString {
                append("You are Free Code AI, a world-class coding assistant and web development expert. ")
                append("Provide clean, modern, well-formatted HTML, CSS, JavaScript, or any requested language. ")
                append("When providing code snippets, format them in markdown code blocks like ```html ... ``` or ```javascript ... ```. ")
                append("Keep explanations concise, accurate, beginner-friendly yet technically rigorous. ")
                if (!activeFileName.isNullOrEmpty() && !contextCode.isNullOrEmpty()) {
                    append("\n\nCurrently active file: $activeFileName\nCurrent file contents:\n```\n")
                    append(contextCode.take(4000))
                    append("\n```\n")
                }
            }

            // Add previous chat history (up to last 6 messages)
            val recentHistory = chatHistory.takeLast(6)
            for ((role, text) in recentHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role.equals("USER", ignoreCase = true)) "user" else "model")
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", text))
                turnObj.put("parts", partsArray)
                contentsArray.put(turnObj)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", "$systemContext\n\nUser Question/Request:\n$userPrompt"))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.95)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                // If API returned error (e.g. invalid key or quota), return fallback with helpful message
                val errMessage = parseErrorMessage(responseBody)
                return@withContext Result.success(
                    "⚠️ Gemini API note: $errMessage\n\n" +
                    getSmartOfflineResponse(userPrompt, contextCode, activeFileName)
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrEmpty()) {
                Result.success(text)
            } else {
                Result.success("Received empty response from AI.")
            }
        } catch (e: Exception) {
            // Graceful fallback
            Result.success(
                "⚠️ Network notice: (${e.message}). Showing built-in solution:\n\n" +
                getSmartOfflineResponse(userPrompt, contextCode, activeFileName)
            )
        }
    }

    suspend fun generateFullProject(
        projectPrompt: String,
        modelName: String = "gemini-3.5-flash"
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getSmartProjectTemplate(projectPrompt))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val prompt = """
                Generate a complete, fully functional, modern multi-file web project based on this prompt:
                "$projectPrompt"

                Requirements:
                - Return files: "index.html", "style.css", "script.js", and optional "README.md".
                - Use modern HTML5 semantic tags, responsive CSS flexbox/grid with a beautiful modern design, and clean interactive JavaScript with event listeners.
                - Format your response EXACTLY with file delimiter blocks so they can be parsed programmatically:
                
                FILE: index.html
                ```html
                (full html code here with <link rel="stylesheet" href="style.css"> and <script src="script.js"></script>)
                ```

                FILE: style.css
                ```css
                (full css styling here)
                ```

                FILE: script.js
                ```javascript
                (full interactive javascript code here)
                ```
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartProjectTemplate(projectPrompt))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrEmpty()) {
                val parsedFiles = parseDelimitedFiles(text)
                if (parsedFiles.isNotEmpty()) {
                    Result.success(parsedFiles)
                } else {
                    Result.success(getSmartProjectTemplate(projectPrompt))
                }
            } else {
                Result.success(getSmartProjectTemplate(projectPrompt))
            }
        } catch (e: Exception) {
            Result.success(getSmartProjectTemplate(projectPrompt))
        }
    }

    private fun parseDelimitedFiles(rawText: String): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val fileRegex = Regex("""FILE:\s*([a-zA-Z0-9_.-]+)[\r\n]+```(?:[a-zA-Z0-9]+)?[\r\n]+([\s\S]*?)```""", RegexOption.MULTILINE)
        val matches = fileRegex.findAll(rawText)

        for (match in matches) {
            val fileName = match.groupValues[1].trim()
            val content = match.groupValues[2].trim()
            if (fileName.isNotEmpty() && content.isNotEmpty()) {
                files[fileName] = content
            }
        }

        // If regex didn't catch standard FILE: markers, attempt fallback by checking code blocks
        if (files.isEmpty()) {
            val htmlMatch = Regex("""```html[\r\n]+([\s\S]*?)```""").find(rawText)
            val cssMatch = Regex("""```css[\r\n]+([\s\S]*?)```""").find(rawText)
            val jsMatch = Regex("""```(?:javascript|js)[\r\n]+([\s\S]*?)```""").find(rawText)

            if (htmlMatch != null) files["index.html"] = htmlMatch.groupValues[1].trim()
            if (cssMatch != null) files["style.css"] = cssMatch.groupValues[1].trim()
            if (jsMatch != null) files["script.js"] = jsMatch.groupValues[1].trim()
        }

        return files
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val obj = JSONObject(errorBody)
            obj.optJSONObject("error")?.optString("message") ?: "API call was not successful"
        } catch (e: Exception) {
            "API call error"
        }
    }

    private fun getSmartOfflineResponse(prompt: String, code: String?, fileName: String?): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("explain") || lower.contains("how does") -> {
                buildString {
                    append("### 💡 Code Explanation for `${fileName ?: "current file"}`\n\n")
                    if (code.isNullOrBlank()) {
                        append("Your current file is empty. To get an explanation, write or generate code in the editor, or choose one of the starter templates like **Neon Calculator** or **Cyber Snake**!")
                    } else {
                        val lines = code.lines()
                        append("This file contains **${lines.size} lines** of code.\n\n")
                        append("**Key Components:**\n")
                        if (code.contains("<html", ignoreCase = true) || code.contains("<!doctype", ignoreCase = true)) {
                            append("- **HTML Structure**: Sets up the semantic DOM tree and viewport meta tags for responsive layout.\n")
                            append("- **External Links**: Connects stylesheet (`style.css`) and script (`script.js`).\n")
                        }
                        if (code.contains("{") && code.contains(":")) {
                            append("- **CSS Rules & Selectors**: Custom styling targeting layout, flexbox/grid containers, and color schemes.\n")
                        }
                        if (code.contains("function") || code.contains("const") || code.contains("addEventListener")) {
                            append("- **JavaScript Logic**: Handles state management, DOM event listeners, and user interaction logic.\n")
                        }
                        append("\n**Tip:** You can ask specific questions like *'How do I add sound effects?'* or *'How can I make this layout responsive?'*")
                    }
                }
            }
            lower.contains("debug") || lower.contains("error") || lower.contains("fix") -> {
                buildString {
                    append("### 🐛 Code Diagnostics & Debugging\n\n")
                    var issuesFound = 0
                    if (!code.isNullOrBlank()) {
                        val openBraces = code.count { it == '{' }
                        val closeBraces = code.count { it == '}' }
                        if (openBraces != closeBraces) {
                            append("- ⚠️ **Mismatched curly braces**: Found $openBraces `{` and $closeBraces `}`.\n")
                            issuesFound++
                        }
                        val openParens = code.count { it == '(' }
                        val closeParens = code.count { it == ')' }
                        if (openParens != closeParens) {
                            append("- ⚠️ **Mismatched parentheses**: Found $openParens `(` and $closeParens `)`.\n")
                            issuesFound++
                        }
                        if (code.contains("<script") && !code.contains("</script>")) {
                            append("- ⚠️ **Unclosed `<script>` tag**.\n")
                            issuesFound++
                        }
                    }
                    if (issuesFound == 0) {
                        append("✅ **No obvious syntax bracket mismatches detected in `${fileName ?: "active file"}`!**\n\n")
                        append("**Common Debugging Checklist:**\n")
                        append("1. **Check Live Preview Console**: Tap the 'Dev Console' button below the preview to see real-time JavaScript runtime errors and console logs.\n")
                        append("2. **Element IDs**: Verify `document.getElementById('...')` matches exact IDs in your HTML.\n")
                        append("3. **Network/CORS**: Ensure external CDN assets (e.g. Google Fonts) have correct URLs.\n")
                    }
                }
            }
            lower.contains("improve") || lower.contains("optimize") -> {
                buildString {
                    append("### ⚡ Optimization & Improvement Recommendations\n\n")
                    append("1. **Modern CSS Flex/Grid**: Ensure layout uses `display: flex` or `grid` with `gap` properties instead of floats or margins.\n")
                    append("2. **Touch Targets**: Use minimum 48px padding/size on interactive buttons for smooth mobile usability.\n")
                    append("3. **Event Delegation**: Use centralized event listeners when dealing with dynamic list items.\n")
                    append("4. **Smooth Transitions**: Add `transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1)` for polished micro-interactions.\n")
                }
            }
            else -> {
                buildString {
                    append("### 💻 Free Code AI Assistant\n\n")
                    append("Here is how you can achieve that in web development:\n\n")
                    append("```javascript\n")
                    append("// Example implementation\n")
                    append("function handleAction() {\n")
                    append("    console.log('Action performed successfully');\n")
                    append("}\n")
                    append("```\n\n")
                    append("You can copy this snippet or click **'Apply to Editor'** to test it directly in your live preview!")
                }
            }
        }
    }

    private fun getSmartProjectTemplate(prompt: String): Map<String, String> {
        val lower = prompt.lowercase()
        return when {
            lower.contains("calc") -> DefaultProjects.starterProjects[0].files
            lower.contains("game") || lower.contains("snake") -> DefaultProjects.starterProjects[1].files
            lower.contains("todo") || lower.contains("task") -> DefaultProjects.starterProjects[2].files
            lower.contains("portfolio") || lower.contains("resume") -> DefaultProjects.starterProjects[3].files
            lower.contains("weather") -> DefaultProjects.starterProjects[4].files
            else -> DefaultProjects.starterProjects[5].files
        }
    }
}
