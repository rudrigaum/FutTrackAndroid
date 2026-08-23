package com.rodrigo.androidapp.futtrack.presentation.video.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) {
        context.findActivity()
    }

    var isFullscreen by remember {
        mutableStateOf(false)
    }

    val webChromeClient = remember(activity) {
        FullscreenWebChromeClient(
            activity = activity,
            onFullscreenChanged = { fullscreen ->
                isFullscreen = fullscreen
            }
        )
    }

    BackHandler(enabled = isFullscreen) {
        webChromeClient.exitFullscreen()
    }

    AndroidView(
        modifier = modifier,
        factory = { factoryContext ->
            FrameLayout(factoryContext).apply {
                addView(
                    WebView(factoryContext).apply {
                        configureForYouTube(
                            webChromeClient = webChromeClient
                        )
                    },
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        },
        update = { container ->
            val webView = container.webView

            if (webView?.tag != videoId) {
                webView?.tag = videoId
                webView?.loadYouTubeVideo(videoId)
            }
        },
        onRelease = { container ->
            webChromeClient.exitFullscreen()
            container.webView?.release()
            container.removeAllViews()
        }
    )
}

private class FullscreenWebChromeClient(
    private val activity: Activity?,
    private val onFullscreenChanged: (Boolean) -> Unit
) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    override fun onShowCustomView(
        view: View?,
        callback: CustomViewCallback?
    ) {
        if (view == null || activity == null) {
            callback?.onCustomViewHidden()
            return
        }

        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        val decorView = activity.window.decorView as? ViewGroup

        if (decorView == null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback

        decorView.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        hideSystemBars(activity)
        onFullscreenChanged(true)
    }

    override fun onHideCustomView() {
        exitFullscreen()
    }

    fun exitFullscreen() {
        val fullscreenView = customView ?: return

        val decorView =
            activity?.window?.decorView as? ViewGroup

        val callback = customViewCallback

        customView = null
        customViewCallback = null

        decorView?.removeView(fullscreenView)

        activity?.let(::showSystemBars)

        onFullscreenChanged(false)

        callback?.onCustomViewHidden()
    }

    private fun hideSystemBars(activity: Activity) {
        val controller = WindowCompat.getInsetsController(
            activity.window,
            activity.window.decorView
        )

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun showSystemBars(activity: Activity) {
        WindowCompat.getInsetsController(
            activity.window,
            activity.window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )
    }
}

private val FrameLayout.webView: WebView?
    get() = getChildAt(0) as? WebView

private fun WebView.configureForYouTube(
    webChromeClient: WebChromeClient
) {
    webViewClient = WebViewClient()
    this.webChromeClient = webChromeClient

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
    }
}

private fun WebView.loadYouTubeVideo(
    videoId: String
) {
    val encodedVideoId = Uri.encode(videoId)

    val embedUrl =
        "https://www.youtube.com/embed/" +
                "$encodedVideoId?playsinline=1"

    val headers = mapOf(
        "Referer" to "https://${context.packageName}"
    )

    loadUrl(embedUrl, headers)
}

private fun WebView.release() {
    stopLoading()
    loadUrl("about:blank")
    clearHistory()
    removeAllViews()
    destroy()
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this

        is ContextWrapper -> {
            baseContext.findActivity()
        }

        else -> null
    }
}