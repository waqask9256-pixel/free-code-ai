package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ProjectFileEntity
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

enum class ViewportMode {
    MOBILE,
    TABLET,
    RESPONSIVE
}

data class ConsoleLogItem(
    val type: String, // "log", "warn", "error"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AndroidConsoleBridge(private val onLogReceived: (String, String) -> Unit) {
    @JavascriptInterface
    fun postLog(type: String, message: String) {
        onLogReceived(type, message)
    }
}

@Composable
fun LivePreviewView(
    files: List<ProjectFileEntity>,
    reloadTrigger: Int,
    consoleLogs: List<ConsoleLogItem>,
    onAddLog: (String, String) -> Unit,
    onOpenConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var viewportMode by remember { mutableStateOf(ViewportMode.RESPONSIVE) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val errorCount = consoleLogs.count { it.type == "error" }

    // Assemble unified HTML
    val bundledHtml = remember(files, reloadTrigger) {
        bundleProjectFiles(files)
    }

    LaunchedEffect(bundledHtml) {
        webViewRef?.loadDataWithBaseURL("https://freecodeai.local/", bundledHtml, "text/html", "UTF-8", null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Preview Control Bar
        Surface(
            color = Slate900,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Viewport selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewportMode = ViewportMode.MOBILE },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewportMode == ViewportMode.MOBILE) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = "Mobile Viewport",
                            tint = if (viewportMode == ViewportMode.MOBILE) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewportMode = ViewportMode.TABLET },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewportMode == ViewportMode.TABLET) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            Icons.Default.Tablet,
                            contentDescription = "Tablet Viewport",
                            tint = if (viewportMode == ViewportMode.TABLET) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewportMode = ViewportMode.RESPONSIVE },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewportMode == ViewportMode.RESPONSIVE) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            Icons.Default.Computer,
                            contentDescription = "Responsive Viewport",
                            tint = if (viewportMode == ViewportMode.RESPONSIVE) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Actions: Refresh, Console, External
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dev Console button with badge
                    FilledTonalButton(
                        onClick = onOpenConsole,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (errorCount > 0) MaterialTheme.colorScheme.errorContainer else Slate800,
                            contentColor = if (errorCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = "Console", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (errorCount > 0) "Console ($errorCount)" else "Console",
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            webViewRef?.loadDataWithBaseURL("https://freecodeai.local/", bundledHtml, "text/html", "UTF-8", null)
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Preview",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Preview Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Slate950)
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val viewportModifier = when (viewportMode) {
                ViewportMode.MOBILE -> Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxHeight()
                    .border(2.dp, Slate800, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                ViewportMode.TABLET -> Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxHeight()
                    .border(2.dp, Slate800, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                ViewportMode.RESPONSIVE -> Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            }

            AndroidView(
                modifier = viewportModifier,
                factory = { ctx ->
                    createConfiguredWebView(ctx, onAddLog).also { wv ->
                        webViewRef = wv
                        wv.loadDataWithBaseURL("https://freecodeai.local/", bundledHtml, "text/html", "UTF-8", null)
                    }
                },
                update = { wv ->
                    webViewRef = wv
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(context: Context, onAddLog: (String, String) -> Unit): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        val bridge = AndroidConsoleBridge { type, msg ->
            post {
                onAddLog(type, msg)
            }
        }
        addJavascriptInterface(bridge, "AndroidBridge")

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    val level = when (it.messageLevel()) {
                        android.webkit.ConsoleMessage.MessageLevel.ERROR -> "error"
                        android.webkit.ConsoleMessage.MessageLevel.WARNING -> "warn"
                        else -> "log"
                    }
                    onAddLog(level, "${it.message()} (line ${it.lineNumber()})")
                }
                return true
            }
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // If it's a real web link (http/https not local), open externally
                if (url != null && !url.startsWith("https://freecodeai.local") && (url.startsWith("http://") || url.startsWith("https://"))) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                return false
            }
        }
    }
}

private fun bundleProjectFiles(files: List<ProjectFileEntity>): String {
    val htmlFile = files.find { it.name.equals("index.html", ignoreCase = true) }
        ?: files.find { it.fileType == "HTML" }

    var htmlContent = htmlFile?.content ?: """
        <!DOCTYPE html>
        <html>
        <head><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Free Code AI</title></head>
        <body style="font-family:sans-serif;background:#0f172a;color:#f8fafc;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;">
            <div style="text-align:center;">
                <h2>Welcome to Free Code AI</h2>
                <p>Create or select an <code>index.html</code> file to preview your live project.</p>
            </div>
        </body>
        </html>
    """.trimIndent()

    val cssFiles = files.filter { it.fileType == "CSS" }
    val jsFiles = files.filter { it.fileType == "JAVASCRIPT" }

    // JavaScript Console bridge injection
    val consoleBridgeScript = """
        <script>
        (function() {
            var oldLog = console.log;
            var oldErr = console.error;
            var oldWarn = console.warn;
            console.log = function() {
                var msg = Array.from(arguments).map(function(a) { return typeof a === 'object' ? JSON.stringify(a) : String(a); }).join(' ');
                window.AndroidBridge && window.AndroidBridge.postLog('log', msg);
                oldLog.apply(console, arguments);
            };
            console.error = function() {
                var msg = Array.from(arguments).map(function(a) { return typeof a === 'object' ? JSON.stringify(a) : String(a); }).join(' ');
                window.AndroidBridge && window.AndroidBridge.postLog('error', msg);
                oldErr.apply(console, arguments);
            };
            console.warn = function() {
                var msg = Array.from(arguments).map(function(a) { return typeof a === 'object' ? JSON.stringify(a) : String(a); }).join(' ');
                window.AndroidBridge && window.AndroidBridge.postLog('warn', msg);
                oldWarn.apply(console, arguments);
            };
            window.onerror = function(message, source, lineno, colno, error) {
                window.AndroidBridge && window.AndroidBridge.postLog('error', message + ' (Line ' + lineno + ')');
            };
        })();
        </script>
    """.trimIndent()

    // Replace linked stylesheet tags with inline styles, or append
    for (cssFile in cssFiles) {
        val linkTagRegex = Regex("""<link\s+[^>]*href=["']${Regex.escape(cssFile.name)}["'][^>]*>""", RegexOption.IGNORE_CASE)
        val inlineStyle = "<style>\n/* ${cssFile.name} */\n${cssFile.content}\n</style>"
        if (linkTagRegex.containsMatchIn(htmlContent)) {
            htmlContent = linkTagRegex.replace(htmlContent, inlineStyle)
        } else {
            // Append before </head> or to top
            htmlContent = if (htmlContent.contains("</head>", ignoreCase = true)) {
                htmlContent.replace(Regex("</head>", RegexOption.IGNORE_CASE), "$inlineStyle\n</head>")
            } else {
                "$inlineStyle\n$htmlContent"
            }
        }
    }

    // Replace linked script tags with inline scripts
    for (jsFile in jsFiles) {
        val scriptTagRegex = Regex("""<script\s+[^>]*src=["']${Regex.escape(jsFile.name)}["'][^>]*>\s*</script>""", RegexOption.IGNORE_CASE)
        val inlineScript = "<script>\n/* ${jsFile.name} */\n${jsFile.content}\n</script>"
        if (scriptTagRegex.containsMatchIn(htmlContent)) {
            htmlContent = scriptTagRegex.replace(htmlContent, inlineScript)
        } else {
            // Append before </body> or bottom
            htmlContent = if (htmlContent.contains("</body>", ignoreCase = true)) {
                htmlContent.replace(Regex("</body>", RegexOption.IGNORE_CASE), "$inlineScript\n</body>")
            } else {
                "$htmlContent\n$inlineScript"
            }
        }
    }

    // Insert console bridge at top of <head> or body
    htmlContent = if (htmlContent.contains("<head>", ignoreCase = true)) {
        htmlContent.replace(Regex("<head>", RegexOption.IGNORE_CASE), "<head>\n$consoleBridgeScript")
    } else {
        "$consoleBridgeScript\n$htmlContent"
    }

    return htmlContent
}
