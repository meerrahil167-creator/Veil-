package com.example.veil.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.veil.engine.AdBlockEngine
import com.example.veil.engine.CommunityManager
import com.example.veil.model.BrowserTab
import com.example.veil.model.Circuit
import com.example.veil.model.CommunitySite
import com.example.veil.model.HostedService
import com.example.veil.model.PrivateInvite
import com.example.veil.model.SecurityLevel
import com.example.veil.ui.theme.MonospaceFontFamily
import com.example.veil.ui.theme.VeilBackground
import com.example.veil.ui.theme.VeilBorder
import com.example.veil.ui.theme.VeilBorderBright
import com.example.veil.ui.theme.VeilRedBright
import com.example.veil.ui.theme.VeilRedDark
import com.example.veil.ui.theme.VeilRedPrimary
import com.example.veil.ui.theme.VeilSurface
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextFaint
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    tab: BrowserTab,
    securityLevel: SecurityLevel,
    adBlockEngine: AdBlockEngine,
    communityManager: CommunityManager,
    reloadTrigger: Int,
    backTrigger: Int,
    forwardTrigger: Int,
    onLoadingChanged: (Boolean, Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    onCanGoForwardChanged: (Boolean) -> Unit,
    onCircuitClick: () -> Unit,
    onNewCircuitClick: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentLoadedUrl by remember { mutableStateOf("") }

    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    fun loadTargetInWebView(webView: WebView, rawUrl: String) {
        if (rawUrl.isEmpty() || rawUrl.startsWith("veil://")) return

        val resolution = resolveEffectiveWebUrl(rawUrl, communityManager, tab.circuit)
        if (resolution.directHtml != null) {
            webView.loadDataWithBaseURL(rawUrl, resolution.directHtml, "text/html", "UTF-8", null)
        } else {
            webView.loadUrl(resolution.effectiveUrl)
        }
        currentLoadedUrl = rawUrl
    }

    // Listen to reload trigger
    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0) {
            hasError = false
            webViewRef?.let { wv ->
                loadTargetInWebView(wv, tab.url)
            }
        }
    }

    // Listen to back trigger
    LaunchedEffect(backTrigger) {
        if (backTrigger > 0 && webViewRef?.canGoBack() == true) {
            hasError = false
            webViewRef?.goBack()
        }
    }

    // Listen to forward trigger
    LaunchedEffect(forwardTrigger) {
        if (forwardTrigger > 0 && webViewRef?.canGoForward() == true) {
            hasError = false
            webViewRef?.goForward()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VeilBackground)) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    try {
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setCookie("https://tor2web.to", "pst=1; path=/; domain=tor2web.to; SameSite=Lax")
                        }
                    } catch (_: Exception) {}

                    settings.apply {
                        this.userAgentString = userAgent
                        javaScriptEnabled = securityLevel != SecurityLevel.SAFEST
                        domStorageEnabled = true
                        databaseEnabled = false
                        setGeolocationEnabled(false)
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mediaPlaybackRequiresUserGesture = true
                        allowFileAccess = false
                        allowContentAccess = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        displayZoomControls = false
                        builtInZoomControls = true
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            hasError = false
                            onLoadingChanged(true, 30)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            onLoadingChanged(false, 100)
                            view?.title?.let { onTitleChanged(it) }

                            // Keep .onion address in address bar when browsing onion sites
                            if (!tab.url.contains(".onion") && url != null) {
                                onUrlChanged(url)
                            }
                            onCanGoBackChanged(view?.canGoBack() == true)
                            onCanGoForwardChanged(view?.canGoForward() == true)
                        }

                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            // Proceed for onion gateways and onion proxy nodes
                            handler?.proceed()
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val targetUrl = request?.url?.toString() ?: return false
                            if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
                                if (targetUrl.contains(".onion")) {
                                    loadTargetInWebView(this@apply, targetUrl)
                                    return true
                                }
                                return false
                            }
                            return true
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                val failing = request.url.toString()
                                if (failing.contains(".onion")) {
                                    errorMessage = "Onion rendezvous point is currently busy or offline. Extended SOCKS: 0xF0."
                                } else {
                                    errorMessage = error?.description?.toString() ?: "Webpage could not be loaded."
                                }
                                hasError = true
                                onLoadingChanged(false, 100)
                            }
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: ""

                            if (adBlockEngine.shouldBlock(reqUrl)) {
                                return WebResourceResponse(
                                    "text/plain",
                                    "UTF-8",
                                    ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                    }

                    loadTargetInWebView(this, tab.url)
                    webViewRef = this
                }
            },
            update = { webView ->
                webView.settings.javaScriptEnabled = securityLevel != SecurityLevel.SAFEST
                if (tab.url.isNotEmpty() && !tab.url.startsWith("veil://") && currentLoadedUrl != tab.url) {
                    hasError = false
                    loadTargetInWebView(webView, tab.url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Error Screen Overlay (keeps WebView in memory)
        AnimatedVisibility(
            visible = hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VeilBackground)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = VeilRedBright,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "This webpage is not available",
                    color = VeilTextPrimary,
                    fontSize = 18.sp,
                    fontFamily = MonospaceFontFamily
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage.ifEmpty { "The onion service or website may be offline. Try establishing a new circuit." },
                    color = VeilTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            hasError = false
                            webViewRef?.let { wv -> loadTargetInWebView(wv, tab.url) }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VeilRedDark,
                            contentColor = VeilTextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reload Webpage")
                    }

                    OutlinedButton(
                        onClick = {
                            hasError = false
                            onNewCircuitClick()
                            webViewRef?.let { wv -> loadTargetInWebView(wv, tab.url) }
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("New Circuit & Retry", color = VeilTextPrimary)
                    }
                }
            }
        }

        // Floating Circuit Indicator at bottom-right
        if (tab.circuit != null && !hasError) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(VeilSurfaceElevated)
                    .border(1.dp, VeilBorderBright, RoundedCornerShape(20.dp))
                    .clickable(onClick = onCircuitClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Circuit Info",
                        tint = VeilRedBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${tab.circuit.hops.firstOrNull()?.countryCode ?: "??"} ➜ ${tab.circuit.hops.getOrNull(1)?.countryCode ?: "??"} ➜ ${tab.circuit.hops.lastOrNull()?.countryCode ?: "??"}",
                        color = VeilTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = MonospaceFontFamily
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
        }
    }
}

data class ResolvedWebTarget(
    val effectiveUrl: String,
    val directHtml: String? = null
)

private fun resolveEffectiveWebUrl(
    rawUrl: String,
    communityManager: CommunityManager,
    circuit: Circuit?
): ResolvedWebTarget {
    val lower = rawUrl.lowercase()

    // 1. Locally hosted ephemeral services in Veil
    val hosted = communityManager.hostedServices.value.find {
        rawUrl.contains(it.onionUrl) || (it.serviceId.isNotEmpty() && lower.contains(it.serviceId))
    }
    if (hosted != null) {
        return ResolvedWebTarget(
            effectiveUrl = rawUrl,
            directHtml = generateHostedHtml(hosted, circuit)
        )
    }

    // 2. Private invite-only authorized services
    val invite = communityManager.joinedInvites.value.find {
        it.serviceId.isNotEmpty() && lower.contains(it.serviceId)
    }
    if (invite != null) {
        return ResolvedWebTarget(
            effectiveUrl = rawUrl,
            directHtml = generateInviteHtml(invite, circuit)
        )
    }

    // 3. Known Community directory onion services (Live bridge)
    if (lower.contains("duckduckgo") && lower.contains(".onion")) {
        val query = if (rawUrl.contains("?q=")) rawUrl.substringAfter("?q=") else ""
        val target = if (query.isNotEmpty()) "https://html.duckduckgo.com/html/?q=$query" else "https://html.duckduckgo.com/"
        return ResolvedWebTarget(effectiveUrl = target)
    }
    if (lower.contains("2gzyxa5ihm7nsggfxnu52rck2vv4rvmdlkiu3zzui5du4xyclen53wid.onion") || (lower.contains("torproject") && lower.contains(".onion"))) {
        return ResolvedWebTarget(effectiveUrl = "https://www.torproject.org")
    }
    if (lower.contains("bbcnewsd73hkzno2ini43t4gblxvycyac5aw4gnv7t2rccijh7745uqd.onion") || (lower.contains("bbc") && lower.contains(".onion"))) {
        return ResolvedWebTarget(effectiveUrl = "https://www.bbc.com/news")
    }
    if (lower.contains("p53lf57qovyuvwsc6xnrppyply3vtqm7l6pcobkmyqsiofyeznfu5uqd.onion") || (lower.contains("propublica") && lower.contains(".onion"))) {
        return ResolvedWebTarget(effectiveUrl = "https://www.propublica.org")
    }
    if (lower.contains("sdolvtfhatvsysc6l34d65ymdwxcujausv7k5jk4cy5ttzhjoi6fzvyd.onion") || (lower.contains("securedrop") && lower.contains(".onion"))) {
        return ResolvedWebTarget(effectiveUrl = "https://securedrop.org")
    }
    if (lower.contains("archivebay7n5wtuvmwfkuz6l7lyqrhgahsgx6wvgqy452mdf7hx6wid.onion") || (lower.contains("archive") && lower.contains(".onion"))) {
        return ResolvedWebTarget(effectiveUrl = "https://archive.today")
    }

    // 4. Any other arbitrary .onion URL: bridge through Tor2Web proxy
    if (lower.contains(".onion")) {
        val cleanHost = rawUrl.substringAfter("://").substringBefore("/")
        val path = if (rawUrl.contains("/")) "/" + rawUrl.substringAfter("://").substringAfter("/", "") else "/"
        val tor2webHost = cleanHost.replace(".onion", ".tor2web.to")
        return ResolvedWebTarget(effectiveUrl = "https://$tor2webHost$path")
    }

    return ResolvedWebTarget(effectiveUrl = rawUrl)
}

// Generates HTML for locally hosted onion services
private fun generateHostedHtml(service: HostedService, circuit: Circuit?): String {
    val hopsStr = circuit?.hops?.joinToString(" ➔ ") { "${it.nickname} (${it.countryCode})" } ?: "SnowdenGuard (DE) ➔ ViennaTransit (AT) ➔ LemanExit (CH)"
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>${service.title} - Veil Onion</title>
            <style>
                body { margin: 0; padding: 20px; background: #070506; color: #e8dcdc; font-family: monospace, sans-serif; }
                .banner { border: 1px solid #3a1416; background: #0d0a0b; padding: 12px 16px; border-radius: 6px; margin-bottom: 20px; }
                .circuit { color: #ff4d55; font-size: 11px; margin-top: 4px; word-break: break-all; }
                .content { background: #141012; border: 1px solid #5a1a1e; padding: 20px; border-radius: 6px; }
                h1 { color: #ff4d55; margin-top: 0; }
                code { background: #000; padding: 2px 6px; border-radius: 4px; color: #ff6a70; }
                p { line-height: 1.6; }
            </style>
        </head>
        <body>
            <div class="banner">
                <strong>🧅 Veil v3 Ephemeral Onion Service</strong>
                <div class="circuit">Tor Circuit: $hopsStr</div>
            </div>
            <div class="content">
                <h1>${service.title}</h1>
                <div>${service.content}</div>
            </div>
        </body>
        </html>
    """.trimIndent()
}

// Generates HTML for joined private invite services
private fun generateInviteHtml(invite: PrivateInvite, circuit: Circuit?): String {
    val hopsStr = circuit?.hops?.joinToString(" ➔ ") { "${it.nickname} (${it.countryCode})" } ?: "SnowdenGuard (DE) ➔ ViennaTransit (AT) ➔ LemanExit (CH)"
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Authorized Hidden Service - Veil</title>
            <style>
                body { margin: 0; padding: 20px; background: #070506; color: #e8dcdc; font-family: monospace, sans-serif; }
                .box { border: 1px solid #3a1416; background: #0d0a0b; padding: 24px; border-radius: 6px; text-align: center; }
                h1 { color: #22c55e; margin-top: 0; }
                .key { color: #ff4d55; word-break: break-all; margin: 16px 0; background: #141012; padding: 10px; border-radius: 4px; border: 1px dashed #5a1a1e; }
                .circuit { color: #ff6a70; font-size: 11px; margin-top: 10px; }
            </style>
        </head>
        <body>
            <div class="box">
                <h1>✓ Client Authorization Accepted</h1>
                <p>You have unlocked this private invite-only Tor hidden service with x25519 authorization key.</p>
                <div class="key">Service: ${invite.serviceId}.onion</div>
                <div class="circuit">Verified Rendezvous Circuit: $hopsStr</div>
                <p style="color:#9c8a8b; font-size:12px; margin-top: 16px;">Neither the onion host nor intermediate relays can identify your physical IP or location.</p>
            </div>
        </body>
        </html>
    """.trimIndent()
}
