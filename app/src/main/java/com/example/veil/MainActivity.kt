package com.example.veil

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.model.VeilScreen
import com.example.veil.ui.components.CircuitPanelBottomSheet
import com.example.veil.ui.components.VeilTopBar
import com.example.veil.ui.screens.BrowserScreen
import com.example.veil.ui.screens.CommunityScreen
import com.example.veil.ui.screens.HostOnionScreen
import com.example.veil.ui.screens.HubScreen
import com.example.veil.ui.screens.LeakTestScreen
import com.example.veil.ui.screens.RoutingVisualizerScreen
import com.example.veil.ui.screens.SelfTestScreen
import com.example.veil.ui.theme.MonospaceFontFamily
import com.example.veil.ui.theme.VeilBackground
import com.example.veil.ui.theme.VeilBorder
import com.example.veil.ui.theme.VeilRedBright
import com.example.veil.ui.theme.VeilRedDark
import com.example.veil.ui.theme.VeilSurface
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary
import com.example.veil.ui.theme.VeilTheme
import com.example.veil.viewmodel.VeilViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VeilViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VeilTheme {
                val context = LocalContext.current
                val currentScreen by viewModel.currentScreen.collectAsState()
                val tabs by viewModel.tabs.collectAsState()
                val activeTab by viewModel.activeTab.collectAsState()
                val torState by viewModel.circuitManager.torState.collectAsState()
                val activeBridge by viewModel.circuitManager.activeBridge.collectAsState()
                val securityLevel by viewModel.securityLevel.collectAsState()
                val bookmarks by viewModel.bookmarks.collectAsState()
                val isCircuitPanelOpen by viewModel.isCircuitPanelOpen.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()

                val communitySites by viewModel.communityManager.directorySites.collectAsState()
                val joinedInvites by viewModel.communityManager.joinedInvites.collectAsState()
                val hostedServices by viewModel.communityManager.hostedServices.collectAsState()

                val networkTests by viewModel.leakTestEngine.networkTests.collectAsState()
                val fingerprintTests by viewModel.leakTestEngine.fingerprintTests.collectAsState()
                val isLeakTesting by viewModel.leakTestEngine.isRunning.collectAsState()

                val selfTestResults by viewModel.selfTestEngine.results.collectAsState()
                val isSelfTesting by viewModel.selfTestEngine.isTesting.collectAsState()
                val selfTestProgress by viewModel.selfTestEngine.currentProgress.collectAsState()

                val reloadTrigger by viewModel.reloadTrigger.collectAsState()
                val backTrigger by viewModel.backTrigger.collectAsState()
                val forwardTrigger by viewModel.forwardTrigger.collectAsState()
                val canGoBack by viewModel.canGoBack.collectAsState()
                val canGoForward by viewModel.canGoForward.collectAsState()

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                BackHandler(enabled = canGoBack || currentScreen != VeilScreen.HUB) {
                    viewModel.goBack()
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    containerColor = VeilBackground,
                    topBar = {
                        VeilTopBar(
                            tabs = tabs,
                            activeTab = activeTab,
                            torState = torState,
                            securityLevel = securityLevel,
                            isBookmarked = bookmarks.contains(activeTab?.url ?: ""),
                            onTabSelect = { viewModel.selectTab(it) },
                            onTabClose = { viewModel.closeTab(it) },
                            onNewTab = { viewModel.openTab("veil://hub/") },
                            onAddressSubmit = { viewModel.handleAddressSubmit(it) },
                            onSecurityLevelChange = { viewModel.setSecurityLevel(it) },
                            onToggleBookmark = {
                                activeTab?.url?.let { viewModel.toggleBookmark(it) }
                            },
                            onCircuitClick = { viewModel.toggleCircuitPanel() },
                            onNewIdentityClick = { viewModel.newIdentity() },
                            onPanicClick = { viewModel.panic() },
                            onReload = { viewModel.reloadActiveTab() },
                            onBack = { viewModel.goBack() },
                            onForward = { viewModel.goForward() },
                            canGoBack = canGoBack || currentScreen != VeilScreen.HUB,
                            canGoForward = canGoForward
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, VeilBorder)
                                .navigationBarsPadding(),
                            containerColor = VeilSurface,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == VeilScreen.HUB,
                                onClick = { viewModel.navigateTo(VeilScreen.HUB) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Hub") },
                                label = { Text("Hub", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VeilRedBright,
                                    selectedTextColor = VeilRedBright,
                                    indicatorColor = VeilRedDark,
                                    unselectedIconColor = VeilTextSecondary,
                                    unselectedTextColor = VeilTextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == VeilScreen.BROWSER,
                                onClick = { viewModel.navigateTo(VeilScreen.BROWSER) },
                                icon = { Icon(Icons.Default.Language, contentDescription = "Browser") },
                                label = { Text("Browser", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VeilRedBright,
                                    selectedTextColor = VeilRedBright,
                                    indicatorColor = VeilRedDark,
                                    unselectedIconColor = VeilTextSecondary,
                                    unselectedTextColor = VeilTextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == VeilScreen.ROUTING_VISUALIZER,
                                onClick = { viewModel.navigateTo(VeilScreen.ROUTING_VISUALIZER) },
                                icon = { Icon(Icons.Default.Timeline, contentDescription = "Routing") },
                                label = { Text("Routing", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VeilRedBright,
                                    selectedTextColor = VeilRedBright,
                                    indicatorColor = VeilRedDark,
                                    unselectedIconColor = VeilTextSecondary,
                                    unselectedTextColor = VeilTextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == VeilScreen.LEAK_TEST,
                                onClick = { viewModel.navigateTo(VeilScreen.LEAK_TEST) },
                                icon = { Icon(Icons.Default.Shield, contentDescription = "Leak Test") },
                                label = { Text("Leak Test", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VeilRedBright,
                                    selectedTextColor = VeilRedBright,
                                    indicatorColor = VeilRedDark,
                                    unselectedIconColor = VeilTextSecondary,
                                    unselectedTextColor = VeilTextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == VeilScreen.COMMUNITY,
                                onClick = { viewModel.navigateTo(VeilScreen.COMMUNITY) },
                                icon = { Icon(Icons.Default.People, contentDescription = "Community") },
                                label = { Text("Community", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VeilRedBright,
                                    selectedTextColor = VeilRedBright,
                                    indicatorColor = VeilRedDark,
                                    unselectedIconColor = VeilTextSecondary,
                                    unselectedTextColor = VeilTextSecondary
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            VeilScreen.HUB -> HubScreen(
                                torState = torState,
                                activeBridge = activeBridge,
                                communitySites = communitySites,
                                onSearch = { viewModel.handleAddressSubmit(it) },
                                onNavigate = { viewModel.navigateTo(it) },
                                onSelectSite = { viewModel.handleAddressSubmit(it) },
                                onSetBridge = { viewModel.setBridge(it) },
                                onJoinInvite = { viewModel.communityManager.joinInvite(it) }
                            )
                            VeilScreen.BROWSER -> {
                                activeTab?.let { tab ->
                                    BrowserScreen(
                                        tab = tab,
                                        securityLevel = securityLevel,
                                        adBlockEngine = viewModel.adBlockEngine,
                                        communityManager = viewModel.communityManager,
                                        reloadTrigger = reloadTrigger,
                                        backTrigger = backTrigger,
                                        forwardTrigger = forwardTrigger,
                                        onLoadingChanged = { loading, progress ->
                                            viewModel.updateTabLoading(loading, progress)
                                        },
                                        onTitleChanged = { title ->
                                            viewModel.updateTabTitle(title)
                                        },
                                        onUrlChanged = { newUrl ->
                                            viewModel.updateTabUrlFromWeb(newUrl)
                                        },
                                        onCanGoBackChanged = { canBack ->
                                            viewModel.canGoBack.value = canBack
                                        },
                                        onCanGoForwardChanged = { canFwd ->
                                            viewModel.canGoForward.value = canFwd
                                        },
                                        onCircuitClick = { viewModel.toggleCircuitPanel() },
                                        onNewCircuitClick = { viewModel.renewCurrentCircuit() }
                                    )
                                }
                            }
                            VeilScreen.ROUTING_VISUALIZER -> RoutingVisualizerScreen(
                                circuit = activeTab?.circuit,
                                onRenewCircuit = { viewModel.renewCurrentCircuit() }
                            )
                            VeilScreen.LEAK_TEST -> LeakTestScreen(
                                leakTestEngine = viewModel.leakTestEngine,
                                networkTests = networkTests,
                                fingerprintTests = fingerprintTests,
                                isRunning = isLeakTesting
                            )
                            VeilScreen.COMMUNITY -> CommunityScreen(
                                sites = communitySites,
                                joinedInvites = joinedInvites,
                                directoryFingerprint = viewModel.communityManager.directoryFingerprint,
                                onSelectSite = { viewModel.handleAddressSubmit(it) },
                                onJoinInvite = {
                                    val res = viewModel.communityManager.joinInvite(it)
                                    if (res.isSuccess) {
                                        viewModel.showToast("Joined invite-only service successfully!")
                                    } else {
                                        viewModel.showToast(res.exceptionOrNull()?.message ?: "Invalid invite")
                                    }
                                },
                                onRemoveInvite = { viewModel.communityManager.removeInvite(it) }
                            )
                            VeilScreen.HOST_ONION -> HostOnionScreen(
                                communityManager = viewModel.communityManager,
                                hostedServices = hostedServices,
                                onPreviewOnion = { viewModel.handleAddressSubmit(it) }
                            )
                            VeilScreen.SELF_TEST -> SelfTestScreen(
                                selfTestEngine = viewModel.selfTestEngine,
                                results = selfTestResults,
                                isTesting = isSelfTesting,
                                progress = selfTestProgress
                            )
                        }

                        if (isCircuitPanelOpen) {
                            CircuitPanelBottomSheet(
                                circuit = activeTab?.circuit,
                                onDismiss = { viewModel.toggleCircuitPanel() },
                                onSeeRouting = {
                                    viewModel.toggleCircuitPanel()
                                    viewModel.navigateTo(VeilScreen.ROUTING_VISUALIZER)
                                },
                                onNewCircuit = {
                                    viewModel.renewCurrentCircuit()
                                },
                                sheetState = sheetState
                            )
                        }
                    }
                }
            }
        }
    }
}
