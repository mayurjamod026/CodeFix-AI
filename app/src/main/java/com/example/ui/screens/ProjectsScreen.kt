package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HistoryEntryEntity
import com.example.data.local.ProjectEntity
import com.example.data.model.Language
import com.example.ui.components.CreateProjectDialog
import com.example.ui.components.DeleteProjectDialog
import com.example.ui.components.RenameProjectDialog
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose
import com.example.ui.theme.DevSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CodeFixViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    viewModel: CodeFixViewModel,
    uiState: UiState
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1000.dp)
                .fillMaxSize()
        ) {
            // Tab row: Projects vs History
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = DevSky
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Saved Projects (${uiState.projects.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("projects_tab_button")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Coding History (${uiState.history.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("history_tab_button")
            )
        }

        if (selectedTab == 0) {
            // Projects Tab Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Workspace Projects",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DevSky),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("create_new_project_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Project", color = Color(0xFF0B0F19), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (uiState.projects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Projects Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create a project or save your code in the editor.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.projects) { project ->
                            ProjectCard(
                                project = project,
                                onOpen = {
                                    viewModel.openProject(project)
                                    viewModel.navigateTo(AppScreen.EDITOR)
                                },
                                onRename = { projectToRename = project },
                                onDelete = { projectToDelete = project }
                            )
                        }
                    }
                }
            }
        } else {
            // History Tab Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sandbox & AI Log",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.history.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.clearAllHistory() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DevRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", color = DevRose, fontSize = 12.sp)
                        }
                    }
                }

                if (uiState.history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Execution History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Run code or run AI analysis to see execution logs here.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.history) { entry ->
                            HistoryItemCard(
                                entry = entry,
                                onLoad = {
                                    val lang = Language.fromId(entry.languageId)
                                    viewModel.selectLanguage(lang)
                                    viewModel.updateCode(entry.codeSnapshot)
                                    viewModel.navigateTo(AppScreen.EDITOR)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showCreateDialog) {
            CreateProjectDialog(
                onCreate = { title, lang ->
                    viewModel.createNewProject(title, lang)
                    showCreateDialog = false
                },
                onDismiss = { showCreateDialog = false }
            )
        }

        projectToRename?.let { project ->
            RenameProjectDialog(
                currentTitle = project.title,
                onRename = { newTitle ->
                    viewModel.renameProject(project.id, newTitle)
                    projectToRename = null
                },
                onDismiss = { projectToRename = null }
            )
        }

        projectToDelete?.let { project ->
            DeleteProjectDialog(
                projectTitle = project.title,
                onConfirmDelete = {
                    viewModel.deleteProject(project.id)
                    projectToDelete = null
                },
                onDismiss = { projectToDelete = null }
            )
        }
    }
}
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val lang = Language.fromId(project.languageId)
    val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(project.updatedAt))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .clickable { onOpen() }
            .testTag("project_item_${project.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DevSky.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = lang.displayName,
                            color = DevSky,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = project.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DevRose, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preview code snippet
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF070B13),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = project.code.lines().take(3).joinToString("\n"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(8.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dateStr • ${project.code.lines().size} lines",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Open in Editor →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DevSky
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    entry: HistoryEntryEntity,
    onLoad: () -> Unit
) {
    val lang = Language.fromId(entry.languageId)
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(entry.timestamp))

    val (actionColor, actionLabel) = when (entry.actionType) {
        "RUN" -> Pair(DevEmerald, "SANDBOX RUN")
        "ANALYZE" -> Pair(DevSky, "AI ANALYSIS")
        "FIX" -> Pair(Color(0xFFFBBF24), "AI CODE FIX")
        "EXPLAIN" -> Pair(DevPurple, "AI EXPLAIN")
        else -> Pair(DevSky, entry.actionType)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .testTag("history_item_${entry.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = actionColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = actionLabel,
                            color = actionColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = lang.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code snapshot preview
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF070B13),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = entry.codeSnapshot.lines().take(2).joinToString("\n"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(8.dp),
                    maxLines = 2
                )
            }

            if (!entry.output.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Output: " + entry.output.trim().lines().firstOrNull().orEmpty(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = DevEmerald
                )
            } else if (!entry.error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: " + entry.error.trim().lines().firstOrNull().orEmpty(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = DevRose
                )
            } else if (!entry.aiAnalysisSummary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Analysis: " + entry.aiAnalysisSummary,
                    fontSize = 11.sp,
                    color = DevSky
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Restore into Editor →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DevSky,
                    modifier = Modifier.clickable { onLoad() }
                )
            }
        }
    }
}
