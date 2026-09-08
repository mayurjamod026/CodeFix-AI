package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CodeFixBottomNavigation
import com.example.ui.components.CodeFixNavigationRail
import com.example.ui.components.CodeFixTopAppBar
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.theme.CodeFixTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CodeFixViewModel
import com.example.ui.viewmodel.UiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CodeFixViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.userNotification) {
                uiState.userNotification?.let { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.dismissNotification()
                }
            }

            CodeFixTheme(darkTheme = uiState.isDarkTheme) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 600.dp

                    if (isWideScreen) {
                        // Tablet / Desktop Layout: NavigationRail on left, TopBar + Content on right
                        Row(modifier = Modifier.fillMaxSize()) {
                            CodeFixNavigationRail(
                                currentScreen = uiState.currentScreen,
                                isDarkTheme = uiState.isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onSelectScreen = { screen -> viewModel.navigateTo(screen) }
                            )

                            Scaffold(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                topBar = {
                                    CodeFixTopAppBar(
                                        currentScreen = uiState.currentScreen,
                                        currentProjectTitle = uiState.currentProject?.title,
                                        currentLanguageName = uiState.currentLanguage.displayName,
                                        isDarkTheme = uiState.isDarkTheme,
                                        onToggleTheme = { viewModel.toggleTheme() }
                                    )
                                },
                                snackbarHost = { SnackbarHost(snackbarHostState) }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    ScreenContent(uiState = uiState, viewModel = viewModel)
                                }
                            }
                        }
                    } else {
                        // Mobile Layout: TopAppBar, Content, and BottomNavigation
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                CodeFixTopAppBar(
                                    currentScreen = uiState.currentScreen,
                                    currentProjectTitle = uiState.currentProject?.title,
                                    currentLanguageName = uiState.currentLanguage.displayName,
                                    isDarkTheme = uiState.isDarkTheme,
                                    onToggleTheme = { viewModel.toggleTheme() }
                                )
                            },
                            bottomBar = {
                                CodeFixBottomNavigation(
                                    currentScreen = uiState.currentScreen,
                                    onSelectScreen = { screen -> viewModel.navigateTo(screen) }
                                )
                            },
                            snackbarHost = { SnackbarHost(snackbarHostState) }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                ScreenContent(uiState = uiState, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    uiState: UiState,
    viewModel: CodeFixViewModel
) {
    when (uiState.currentScreen) {
        AppScreen.HOME -> HomeScreen(
            uiState = uiState,
            onNavigate = { screen -> viewModel.navigateTo(screen) },
            onSelectLanguage = { lang -> viewModel.selectLanguage(lang) },
            onOpenProject = { project ->
                viewModel.openProject(project)
                viewModel.navigateTo(AppScreen.EDITOR)
            }
        )
        AppScreen.EDITOR -> EditorScreen(
            viewModel = viewModel,
            uiState = uiState
        )
        AppScreen.CHAT -> AiChatScreen(
            viewModel = viewModel,
            uiState = uiState
        )
        AppScreen.PRACTICE -> PracticeScreen(
            viewModel = viewModel
        )
        AppScreen.PROJECTS -> ProjectsScreen(
            viewModel = viewModel,
            uiState = uiState
        )
    }
}


