package com.example.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ExecutionResult
import com.example.data.model.ExecutionStatus
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import com.example.ui.theme.DevSky

@Composable
fun ConsoleOutputView(
    result: ExecutionResult?,
    isLoading: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val hasHtmlPreview = result?.htmlPreviewContent != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = shape
            ),
        color = Color(0xFF070B13),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Terminal",
                        tint = DevSky,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sandbox Console",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (result != null && !isLoading) {
                        val (statusBg, statusFg) = when (result.status) {
                            ExecutionStatus.SUCCESS -> Pair(DevEmerald.copy(alpha = 0.2f), DevEmerald)
                            ExecutionStatus.COMPILATION_ERROR, ExecutionStatus.RUNTIME_ERROR -> Pair(DevRose.copy(alpha = 0.2f), DevRose)
                            else -> Pair(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFF59E0B))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusBg,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (result.status == ExecutionStatus.SUCCESS) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = statusFg,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = result.status.label,
                                    color = statusFg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "${result.executionTimeMs} ms",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (result?.stdout != null || result?.stderr != null) {
                        IconButton(
                            onClick = {
                                val text = (result.stdout ?: "") + "\n" + (result.stderr ?: "")
                                clipboardManager.setText(AnnotatedString(text))
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("copy_console_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Console Output",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("close_console_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Console",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (hasHtmlPreview) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0A0F1D),
                    contentColor = DevSky,
                    modifier = Modifier.height(36.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Terminal Output", fontSize = 12.sp, color = if (selectedTab == 0) DevSky else Color(0xFF94A3B8)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Rendered Preview", fontSize = 12.sp, color = if (selectedTab == 1) DevSky else Color(0xFF94A3B8))
                            }
                        }
                    )
                }
            }

            // Body content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 240.dp)
                    .padding(12.dp)
            ) {
                if (isLoading) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = DevSky,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Executing in secure sandbox container...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (hasHtmlPreview && selectedTab == 1) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                loadDataWithBaseURL(null, result.htmlPreviewContent ?: "", "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(verticalScroll)
                            .horizontalScroll(horizontalScroll)
                    ) {
                        if (result == null) {
                            Text(
                                text = "$ Ready. Press 'Run Code' to execute in the secure sandbox.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            // Compilation errors
                            if (!result.compilationOutput.isNullOrBlank()) {
                                Text(
                                    text = "[Compilation Output]:\n" + result.compilationOutput,
                                    color = DevRose,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Standard error
                            if (!result.stderr.isNullOrBlank()) {
                                Text(
                                    text = "[Runtime Error]:\n" + result.stderr,
                                    color = DevRose,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Standard output
                            if (!result.stdout.isNullOrBlank()) {
                                Text(
                                    text = result.stdout,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (result.stdout.isNullOrBlank() && result.stderr.isNullOrBlank() && result.compilationOutput.isNullOrBlank()) {
                                Text(
                                    text = "[Process completed with exit code ${result.exitCode}. No output generated.]",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
