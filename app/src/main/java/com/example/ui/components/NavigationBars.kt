package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DevSky
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeFixTopAppBar(
    currentScreen: AppScreen,
    currentProjectTitle: String?,
    currentLanguageName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Glow badge logo
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DevSky.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "</>",
                        color = DevSky,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "CodeFix AI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (currentScreen == AppScreen.EDITOR) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = currentLanguageName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark/Light Mode",
                    tint = if (isDarkTheme) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun CodeFixBottomNavigation(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(AppScreen.HOME, "Home", Icons.Default.Home),
            Triple(AppScreen.EDITOR, "Editor", Icons.Default.Code),
            Triple(AppScreen.CHAT, "AI Tutor", Icons.Default.Psychology),
            Triple(AppScreen.PRACTICE, "Practice", Icons.Default.School),
            Triple(AppScreen.PROJECTS, "Projects", Icons.Default.Folder)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DevSky,
                    selectedTextColor = DevSky,
                    indicatorColor = DevSky.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
            )
        }
    }
}

@Composable
fun CodeFixNavigationRail(
    currentScreen: AppScreen,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSelectScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DevSky.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "</>",
                        color = DevSky,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "CodeFix",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DevSky
                )
            }
        }
    ) {
        val items = listOf(
            Triple(AppScreen.HOME, "Home", Icons.Default.Home),
            Triple(AppScreen.EDITOR, "Editor", Icons.Default.Code),
            Triple(AppScreen.CHAT, "AI Tutor", Icons.Default.Psychology),
            Triple(AppScreen.PRACTICE, "Practice", Icons.Default.School),
            Triple(AppScreen.PROJECTS, "Projects", Icons.Default.Folder)
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            items.forEach { (screen, label, icon) ->
                val isSelected = currentScreen == screen
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onSelectScreen(screen) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = DevSky,
                        selectedTextColor = DevSky,
                        indicatorColor = DevSky.copy(alpha = 0.15f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_rail_${screen.name.lowercase()}")
                )
            }
        }

        // Bottom Theme Toggle
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .testTag("toggle_theme_button_rail")
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Dark/Light Mode",
                tint = if (isDarkTheme) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

