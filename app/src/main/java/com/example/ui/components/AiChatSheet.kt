package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.theme.*

@Composable
fun AiChatSheet(
    messages: List<ChatMessageEntity>,
    isAiLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onApplyCodeToEditor: (String) -> Unit,
    onClearChat: () -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isAiLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Chat Header
        Surface(
            color = Slate900,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "AI",
                        tint = CyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Free Code AI Assistant",
                            color = Slate100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Powered by Gemini • Free Coding Help",
                            color = EmeraldNeon,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenApiKeyDialog) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "API Key / Model Settings",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (messages.isNotEmpty()) {
                        IconButton(onClick = onClearChat) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyChatGreeting(
                        onSelectPrompt = { samplePrompt ->
                            onSendMessage(samplePrompt)
                        }
                    )
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onApplyCode = onApplyCodeToEditor,
                        onCopyCode = { code ->
                            clipboardManager.setText(AnnotatedString(code))
                        }
                    )
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanNeon,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Free Code AI is thinking & coding...",
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Quick Action Prompt Chips
        val quickPrompts = listOf(
            "💡 Explain this code",
            "🐛 Debug & fix issues",
            "⚡ Optimize & modernize",
            "🎨 Add sleek neon dark styling",
            "📱 Make fully mobile responsive",
            "🚀 Generate full Web Calculator",
            "🎮 Generate Arcade Snake Game"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (qp in quickPrompts) {
                OutlinedButton(
                    onClick = { onSendMessage(qp) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                ) {
                    Text(qp, fontSize = 11.sp)
                }
            }
        }

        // Message Input Field
        Surface(
            color = Slate900,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            "Ask a coding question or request code...",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate200,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = promptInput.trim()
                        if (text.isNotEmpty()) {
                            onSendMessage(text)
                            promptInput = ""
                        }
                    },
                    enabled = promptInput.isNotBlank() && !isAiLoading,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (promptInput.isNotBlank() && !isAiLoading) CyanNeon else Slate800,
                        contentColor = if (promptInput.isNotBlank() && !isAiLoading) Slate950 else Slate600
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyChatGreeting(onSelectPrompt: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Welcome to Free Code AI!",
                color = Slate100,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "I can write, explain, debug, and optimize your HTML, CSS, and JavaScript. Generated code can be applied directly to your project with one tap!",
                color = Slate300,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Try asking:",
                color = CyanNeon,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))

            val suggestions = listOf(
                "How do I add local storage to remember user scores?",
                "Create a responsive hamburger navigation bar in CSS",
                "Fix JavaScript console errors in my current file",
                "Generate a digital clock widget with seconds"
            )

            for (s in suggestions) {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Text(
                        text = "• $s",
                        color = Slate200,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessageEntity,
    onApplyCode: (String) -> Unit,
    onCopyCode: (String) -> Unit
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) Slate800 else Slate900,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, Slate800) else null,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isUser) "You" else "Free Code AI",
                        color = if (isUser) Slate300 else CyanNeon,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Parse message text and potential code blocks
                val blocks = parseMessageContent(message.text)

                for (block in blocks) {
                    when (block) {
                        is ContentBlock.Text -> {
                            Text(
                                text = block.content,
                                color = Slate100,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                        is ContentBlock.Code -> {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                color = Slate950,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = block.language.uppercase(),
                                            color = EmeraldNeon,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { onCopyCode(block.code) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Code",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            Button(
                                                onClick = { onApplyCode(block.code) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CyanNeon,
                                                    contentColor = Slate950
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.Input,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Apply", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = block.code,
                                        color = Slate200,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Code(val language: String, val code: String) : ContentBlock()
}

private fun parseMessageContent(raw: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val codeBlockRegex = Regex("""```([a-zA-Z0-9]*)\r?\n([\s\S]*?)```""")

    var currentIndex = 0
    val matches = codeBlockRegex.findAll(raw)

    for (match in matches) {
        val matchStart = match.range.first
        if (matchStart > currentIndex) {
            val textPart = raw.substring(currentIndex, matchStart).trim()
            if (textPart.isNotEmpty()) {
                blocks.add(ContentBlock.Text(textPart))
            }
        }
        val lang = match.groupValues[1].ifEmpty { "code" }
        val code = match.groupValues[2].trim()
        blocks.add(ContentBlock.Code(lang, code))
        currentIndex = match.range.last + 1
    }

    if (currentIndex < raw.length) {
        val remaining = raw.substring(currentIndex).trim()
        if (remaining.isNotEmpty()) {
            blocks.add(ContentBlock.Text(remaining))
        }
    }

    if (blocks.isEmpty() && raw.isNotEmpty()) {
        blocks.add(ContentBlock.Text(raw))
    }

    return blocks
}
