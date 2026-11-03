package com.ourspace.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.ourspace.app.data.sync.OfflineSyncWorker
import com.ourspace.app.ui.navigation.BottomNavTab
import com.ourspace.app.ui.screens.auth.AuthAndPairingScreen
import com.ourspace.app.ui.screens.home.HomeDashboardScreen
import com.ourspace.app.ui.screens.memories.MemoriesAndRelationshipScreen
import com.ourspace.app.ui.screens.memories.MemoriesSubTab
import com.ourspace.app.ui.screens.myspace.MySpaceScreen
import com.ourspace.app.ui.screens.ourspace.OurSpaceScreen
import com.ourspace.app.ui.screens.profile.ProfileAndSettingsScreen
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.theme.OurSpaceTheme
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OfflineSyncWorker.schedulePeriodicSync(applicationContext)

        setContent {
            val mainViewModel = com.ourspace.app.di.OurSpaceContainer.getViewModel(applicationContext)
            val prefs by mainViewModel.userPreferences.collectAsState()

            OurSpaceTheme(themeMode = prefs.themeMode) {
                OurSpaceNavigationHost(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun OurSpaceNavigationHost(viewModel: OurSpaceMainViewModel) {
    val session by viewModel.sessionState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.HOME) }
    var showAuthPairingOverlay by rememberSaveable { mutableStateOf(false) }
    var memoriesInitialTab by rememberSaveable { mutableStateOf(MemoriesSubTab.GALLERY) }
    val colors = OurSpaceDesign.colors

    Scaffold(
        containerColor = colors.backgroundPrimary,
        bottomBar = {
            if (!showAuthPairingOverlay) {
                NavigationBar(
                    containerColor = colors.cardSurface.copy(alpha = 0.96f),
                    tonalElevation = 8.dp
                ) {
                    BottomNavTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary)
                .padding(innerPadding)
        ) {
            if (showAuthPairingOverlay) {
                AuthAndPairingScreen(
                    session = session,
                    viewModel = viewModel,
                    onContinueToHome = {
                        showAuthPairingOverlay = false
                        selectedTab = BottomNavTab.HOME
                    }
                )
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "mainBottomNavTransition"
                ) { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> HomeDashboardScreen(
                            viewModel = viewModel,
                            onNavigateTab = { nextTab -> selectedTab = nextTab },
                            onOpenRelationshipHub = {
                                memoriesInitialTab = MemoriesSubTab.RELATIONSHIP
                                selectedTab = BottomNavTab.MEMORIES
                            },
                            onOpenSurpriseHub = {
                                memoriesInitialTab = MemoriesSubTab.SURPRISES
                                selectedTab = BottomNavTab.MEMORIES
                            }
                        )
                        BottomNavTab.MY_SPACE -> MySpaceScreen(viewModel = viewModel)
                        BottomNavTab.OUR_SPACE -> OurSpaceScreen(viewModel = viewModel)
                        BottomNavTab.MEMORIES -> MemoriesAndRelationshipScreen(
                            viewModel = viewModel,
                            initialSubTab = memoriesInitialTab
                        )
                        BottomNavTab.PROFILE -> ProfileAndSettingsScreen(
                            viewModel = viewModel,
                            onOpenAuthPairing = { showAuthPairingOverlay = true }
                        )
                    }
                }
            }
        }
    }
}
