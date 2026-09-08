package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.components.AiAnalysisSheet
import com.example.ui.components.AiExplainDialog
import com.example.ui.components.AiFixDialog
import com.example.ui.components.ConsoleOutputView
import com.example.ui.components.SaveProjectDialog
import com.example.ui.editor.SyntaxHighlighter
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevSky
import com.example.ui.viewmodel.CodeFixViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun EditorScreen(
    viewModel: CodeFixViewModel,
    uiState: UiState
) {
    val clipboardManager = LocalClipboardManager.current
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Synchronized scroll states
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val lineCount = remember(uiState.currentCode) {
        uiState.currentCode.lines().size.coerceAtLeast(1)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isWideScreen = maxWidth >= 760.dp
        val showSideBySide = isWideScreen && uiState.consoleVisible

        Column(modifier = Modifier.fillMaxSize()) {

            // Top Action Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Selector Button
                    Box {
                        OutlinedButton(
                            onClick = { languageDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("language_selector_dropdown")
                        ) {
                            Text(
                                text = uiState.currentLanguage.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevSky
                            )
                        }

                        DropdownMenu(
                            expanded = languageDropdownExpanded,
                            onDismissRequest = { languageDropdownExpanded = false }
                        ) {
                            Language.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName, fontWeight = if (lang == uiState.currentLanguage) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.selectLanguage(lang)
                                        languageDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Run Code Button
                    Button(
                        onClick = { viewModel.runCode() },
                        enabled = !uiState.isExecuting,
                        colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("run_code_button")
                    ) {
                        if (uiState.isExecuting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isExecuting) "Running..." else "Run",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Analyze Code Button
                    FilledTonalButton(
                        onClick = { viewModel.analyzeCode() },
                        enabled = !uiState.isAnalyzing,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("analyze_code_button")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = DevSky, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analyze", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Fix Code Button
                    FilledTonalButton(
                        onClick = { viewModel.requestFix() },
                        enabled = !uiState.isFixing,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("fix_code_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = DevEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Explain Code Button
                    FilledTonalButton(
                        onClick = { viewModel.requestExplanation() },
                        enabled = !uiState.isExplaining,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("explain_code_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = DevPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Explain", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Save Button
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.size(32.dp).testTag("save_project_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Project", tint = DevSky, modifier = Modifier.size(18.dp))
                    }

                    // Copy Code Button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(uiState.currentCode))
                            viewModel.setNotification("Code copied to clipboard!")
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_code_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }

                    // Clear Code Button
                    IconButton(
                        onClick = { viewModel.clearCode() },
                        modifier = Modifier.size(32.dp).testTag("clear_code_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }

                    // Toggle Console Drawer
                    IconButton(
                        onClick = { viewModel.setConsoleVisible(!uiState.consoleVisible) },
                        modifier = Modifier.size(32.dp).testTag("toggle_console_button")
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = "Toggle Console",
                            tint = if (uiState.consoleVisible) DevSky else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Mobile Quick Coding Symbol Bar
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val symbols = listOf("Tab", "{", "}", "(", ")", "[", "]", ";", "\"", "'", "=", "==", "<", ">", "+", "-", "*", "/", ":", ".", "!", "&", "|")
                    symbols.forEach { sym ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = sym,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        val insert = if (sym == "Tab") "    " else sym
                                        viewModel.updateCode(uiState.currentCode + insert)
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Editor Canvas & Console: Adaptive Layout
            if (showSideBySide) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF080C16))
                    ) {
                        EditorTextCanvas(
                            lineCount = lineCount,
                            verticalScrollState = verticalScrollState,
                            horizontalScrollState = horizontalScrollState,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    ConsoleOutputView(
                        result = uiState.executionResult,
                        isLoading = uiState.isExecuting,
                        onClose = { viewModel.setConsoleVisible(false) },
                        modifier = Modifier
                            .weight(0.85f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF080C16))
                ) {
                    EditorTextCanvas(
                        lineCount = lineCount,
                        verticalScrollState = verticalScrollState,
                        horizontalScrollState = horizontalScrollState,
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }

                // Bottom Sandboxed Console Drawer
                if (uiState.consoleVisible) {
                    ConsoleOutputView(
                        result = uiState.executionResult,
                        isLoading = uiState.isExecuting,
                        onClose = { viewModel.setConsoleVisible(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 290.dp)
                    )
                }
            }
        }

        // Modals & Dialogs
        if (uiState.showAnalysisSheet) {
            AiAnalysisSheet(
                result = uiState.analysisResult,
                isLoading = uiState.isAnalyzing,
                onDismiss = { viewModel.dismissAnalysisSheet() }
            )
        }

        if (uiState.showFixDialog) {
            AiFixDialog(
                fixResult = uiState.fixResult,
                originalCode = uiState.currentCode,
                language = uiState.currentLanguage,
                isLoading = uiState.isFixing,
                onApplyFix = { newCode -> viewModel.applyFix(newCode) },
                onDismiss = { viewModel.dismissFixDialog() }
            )
        }

        if (uiState.showExplainDialog) {
            AiExplainDialog(
                explainResult = uiState.explainResult,
                selectedMode = uiState.selectedExplainMode,
                selectedLanguage = uiState.selectedExplainLanguage,
                isLoading = uiState.isExplaining,
                onChangeMode = { mode -> viewModel.requestExplanation(mode = mode) },
                onChangeLanguage = { lang -> viewModel.requestExplanation(language = lang) },
                onDismiss = { viewModel.dismissExplainDialog() }
            )
        }

        if (showSaveDialog) {
            SaveProjectDialog(
                initialTitle = uiState.currentProject?.title ?: "My ${uiState.currentLanguage.displayName} Code",
                currentLanguage = uiState.currentLanguage,
                onSave = { title ->
                    viewModel.saveProject(title)
                    showSaveDialog = false
                },
                onDismiss = { showSaveDialog = false }
            )
        }
    }
}

@Composable
private fun EditorTextCanvas(
    lineCount: Int,
    verticalScrollState: androidx.compose.foundation.ScrollState,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    uiState: UiState,
    viewModel: CodeFixViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
    ) {
        // Line Number column
        Column(
            modifier = Modifier
                .background(Color(0xFF060911))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = "$i",
                    color = Color(0xFF475569),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Code text field with syntax highlighting
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp, horizontal = 4.dp)
                .horizontalScroll(horizontalScrollState)
        ) {
            BasicTextField(
                value = uiState.currentCode,
                onValueChange = { viewModel.updateCode(it) },
                textStyle = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(DevSky),
                visualTransformation = {
                    androidx.compose.ui.text.input.TransformedText(
                        SyntaxHighlighter.highlight(it.text, uiState.currentLanguage),
                        androidx.compose.ui.text.input.OffsetMapping.Identity
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("code_editor_input")
            )
        }
    }
}

