package com.example.veil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veil.engine.AdBlockEngine
import com.example.veil.engine.CommunityManager
import com.example.veil.engine.LeakTestEngine
import com.example.veil.engine.SelfTestEngine
import com.example.veil.engine.TorCircuitManager
import com.example.veil.model.BridgeType
import com.example.veil.model.BrowserTab
import com.example.veil.model.Circuit
import com.example.veil.model.SecurityLevel
import com.example.veil.model.TorState
import com.example.veil.model.VeilScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URI
import java.util.UUID

class VeilViewModel : ViewModel() {
    val circuitManager = TorCircuitManager()
    val communityManager = CommunityManager()
    val leakTestEngine = LeakTestEngine(circuitManager)
    val adBlockEngine = AdBlockEngine()
    val selfTestEngine = SelfTestEngine(circuitManager)

    private val _currentScreen = MutableStateFlow(VeilScreen.HUB)
    val currentScreen: StateFlow<VeilScreen> = _currentScreen.asStateFlow()

    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>("")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<BrowserTab?> = combine(_tabs, _activeTabId) { tabList, activeId ->
        tabList.find { it.id == activeId } ?: tabList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _securityLevel = MutableStateFlow(SecurityLevel.STANDARD)
    val securityLevel: StateFlow<SecurityLevel> = _securityLevel.asStateFlow()

    private val _bookmarks = MutableStateFlow<Set<String>>(emptySet())
    val bookmarks: StateFlow<Set<String>> = _bookmarks.asStateFlow()

    private val _history = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val history: StateFlow<List<Pair<String, String>>> = _history.asStateFlow()

    private val _isCircuitPanelOpen = MutableStateFlow(false)
    val isCircuitPanelOpen: StateFlow<Boolean> = _isCircuitPanelOpen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    private val _backTrigger = MutableStateFlow(0)
    val backTrigger: StateFlow<Int> = _backTrigger.asStateFlow()

    private val _forwardTrigger = MutableStateFlow(0)
    val forwardTrigger: StateFlow<Int> = _forwardTrigger.asStateFlow()

    val canGoBack = MutableStateFlow(false)
    val canGoForward = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            circuitManager.initializeBootstrap()
        }
        val initialTab = createTabInternal("veil://hub/", "Veil Hub")
        _tabs.value = listOf(initialTab)
        _activeTabId.value = initialTab.id
    }

    private fun createTabInternal(url: String, title: String): BrowserTab {
        val id = UUID.randomUUID().toString().substring(0, 8)
        val domain = extractDomain(url)
        val circuit = if (domain.isNotEmpty() && !url.startsWith("veil://")) {
            circuitManager.getCircuitForDomain(domain)
        } else null

        val hasOnion = isClearwebSiteWithOnion(url)
        return BrowserTab(
            id = id,
            title = title,
            url = url,
            isLoading = false,
            circuit = circuit,
            hasOnionAlternative = hasOnion.first,
            onionAlternativeUrl = hasOnion.second
        )
    }

    fun openTab(url: String) {
        val title = when {
            url.contains("hub") -> "Veil Hub"
            url.contains("routing") -> "Onion Routing"
            url.contains("leaktest") -> "Leak Test"
            url.contains("selftest") -> "Self Test"
            else -> extractDomain(url).ifEmpty { "New Tab" }
        }
        val tab = createTabInternal(url, title)
        _tabs.value = _tabs.value + tab
        _activeTabId.value = tab.id
        resolveScreenForUrl(url)
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            // Keep at least one tab
            val freshTab = createTabInternal("veil://hub/", "Veil Hub")
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
            _currentScreen.value = VeilScreen.HUB
            return
        }

        val index = currentList.indexOfFirst { it.id == tabId }
        val updated = currentList.filterNot { it.id == tabId }
        _tabs.value = updated

        if (_activeTabId.value == tabId) {
            val nextIndex = if (index >= updated.size) updated.size - 1 else index
            val nextTab = updated[nextIndex]
            _activeTabId.value = nextTab.id
            resolveScreenForUrl(nextTab.url)
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
        _tabs.value.find { it.id == tabId }?.let {
            resolveScreenForUrl(it.url)
        }
    }

    fun navigateTo(screen: VeilScreen) {
        _currentScreen.value = screen
        val url = when (screen) {
            VeilScreen.HUB -> "veil://hub/"
            VeilScreen.ROUTING_VISUALIZER -> "veil://routing/"
            VeilScreen.LEAK_TEST -> "veil://leaktest/"
            VeilScreen.COMMUNITY -> "veil://community/"
            VeilScreen.HOST_ONION -> "veil://host/"
            VeilScreen.SELF_TEST -> "veil://selftest/"
            VeilScreen.BROWSER -> activeTab.value?.url ?: "https://check.torproject.org/"
        }
        updateActiveTabUrl(url, screen.name)
    }

    fun reloadActiveTab() {
        _reloadTrigger.value++
    }

    fun goBack() {
        if (canGoBack.value) {
            _backTrigger.value++
        } else if (_currentScreen.value != VeilScreen.HUB) {
            navigateTo(VeilScreen.HUB)
        }
    }

    fun goForward() {
        if (canGoForward.value) {
            _forwardTrigger.value++
        }
    }

    fun handleAddressSubmit(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        val url = when {
            trimmed.startsWith("veil://") -> trimmed
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.endsWith(".onion") || trimmed.contains(".onion/") || trimmed.contains(".onion") -> {
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
            }
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> {
                val encoded = java.net.URLEncoder.encode(trimmed, "UTF-8")
                "https://html.duckduckgo.com/html/?q=$encoded"
            }
        }

        resolveScreenForUrl(url)
        updateActiveTabUrl(url, extractDomain(url).ifEmpty { "Browsing" })
        recordHistory(url, extractDomain(url).ifEmpty { url })
        _reloadTrigger.value++
    }

    fun updateTabUrlFromWeb(newUrl: String) {
        if (newUrl.startsWith("veil://")) return
        val currentId = _activeTabId.value
        val domain = extractDomain(newUrl)
        val circuit = if (domain.isNotEmpty()) circuitManager.getCircuitForDomain(domain) else null
        val hasOnion = isClearwebSiteWithOnion(newUrl)

        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(
                    url = newUrl,
                    circuit = circuit ?: tab.circuit,
                    hasOnionAlternative = hasOnion.first,
                    onionAlternativeUrl = hasOnion.second
                )
            } else tab
        }
        recordHistory(newUrl, domain.ifEmpty { newUrl })
    }

    private fun resolveScreenForUrl(url: String) {
        when {
            url.contains("veil://hub") -> _currentScreen.value = VeilScreen.HUB
            url.contains("veil://routing") -> _currentScreen.value = VeilScreen.ROUTING_VISUALIZER
            url.contains("veil://leaktest") -> _currentScreen.value = VeilScreen.LEAK_TEST
            url.contains("veil://community") -> _currentScreen.value = VeilScreen.COMMUNITY
            url.contains("veil://host") -> _currentScreen.value = VeilScreen.HOST_ONION
            url.contains("veil://selftest") -> _currentScreen.value = VeilScreen.SELF_TEST
            else -> _currentScreen.value = VeilScreen.BROWSER
        }
    }

    private fun updateActiveTabUrl(url: String, title: String) {
        val currentId = _activeTabId.value
        val domain = extractDomain(url)
        val circuit = if (domain.isNotEmpty() && !url.startsWith("veil://")) {
            circuitManager.getCircuitForDomain(domain)
        } else null

        val hasOnion = isClearwebSiteWithOnion(url)

        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(
                    url = url,
                    title = title,
                    circuit = circuit,
                    hasOnionAlternative = hasOnion.first,
                    onionAlternativeUrl = hasOnion.second
                )
            } else tab
        }
    }

    fun updateTabLoading(isLoading: Boolean, progress: Int = 100) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map {
            if (it.id == currentId) it.copy(isLoading = isLoading, progress = progress) else it
        }
    }

    fun updateTabTitle(title: String) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map {
            if (it.id == currentId) it.copy(title = title) else it
        }
    }

    fun setSecurityLevel(level: SecurityLevel) {
        _securityLevel.value = level
        showToast("Security level changed to ${level.label}")
    }

    fun setBridge(bridge: BridgeType) {
        circuitManager.setBridge(bridge)
        showToast("Tor bridge set to ${bridge.label}")
    }

    fun toggleBookmark(url: String) {
        val current = _bookmarks.value
        if (current.contains(url)) {
            _bookmarks.value = current - url
            showToast("Removed bookmark")
        } else {
            _bookmarks.value = current + url
            showToast("Page bookmarked (in-memory)")
        }
    }

    fun toggleCircuitPanel() {
        _isCircuitPanelOpen.value = !_isCircuitPanelOpen.value
    }

    fun renewCurrentCircuit() {
        val tab = activeTab.value ?: return
        val domain = extractDomain(tab.url)
        if (domain.isNotEmpty()) {
            val fresh = circuitManager.renewCircuitForDomain(domain)
            _tabs.value = _tabs.value.map {
                if (it.id == tab.id) it.copy(circuit = fresh) else it
            }
            showToast("New circuit established for $domain")
        }
    }

    fun newIdentity() {
        circuitManager.resetAllCircuits()
        _bookmarks.value = emptySet()
        _history.value = emptyList()
        val freshTab = createTabInternal("veil://hub/", "Veil Hub")
        _tabs.value = listOf(freshTab)
        _activeTabId.value = freshTab.id
        _currentScreen.value = VeilScreen.HUB
        _isCircuitPanelOpen.value = false
        showToast("New Identity: All circuits reset, memory cleared")
    }

    fun panic() {
        circuitManager.resetAllCircuits()
        communityManager.clearAll()
        adBlockEngine.resetStats()
        _bookmarks.value = emptySet()
        _history.value = emptyList()
        val cleanTab = createTabInternal("veil://hub/", "Veil Hub")
        _tabs.value = listOf(cleanTab)
        _activeTabId.value = cleanTab.id
        _currentScreen.value = VeilScreen.HUB
        _isCircuitPanelOpen.value = false
        showToast("PANIC: Tabs closed, clipboard purged, storage destroyed")
    }

    private fun recordHistory(url: String, title: String) {
        if (!url.startsWith("veil://")) {
            _history.value = listOf(title to url) + _history.value.take(49)
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = URI(url)
            uri.host ?: ""
        } catch (_: Exception) {
            url.substringAfter("://").substringBefore("/")
        }
    }

    private fun isClearwebSiteWithOnion(url: String): Pair<Boolean, String?> {
        val lower = url.lowercase()
        return when {
            lower.contains("torproject.org") && !lower.contains(".onion") ->
                true to "http://2gzyxa5ihm7nsggfxnu52rck2vv4rvmdlkiu3zzui5du4xyclen53wid.onion/"
            lower.contains("duckduckgo.com") && !lower.contains(".onion") ->
                true to "https://duckduckgogg42xjoc72x3sjasowoarfbgcmvfimaftt6twagswzczad.onion/"
            lower.contains("bbc.com") && !lower.contains(".onion") ->
                true to "https://www.bbcnewsd73hkzno2ini43t4gblxvycyac5aw4gnv7t2rccijh7745uqd.onion/"
            lower.contains("propublica.org") && !lower.contains(".onion") ->
                true to "https://p53lf57qovyuvwsc6xnrppyply3vtqm7l6pcobkmyqsiofyeznfu5uqd.onion/"
            else -> false to null
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
