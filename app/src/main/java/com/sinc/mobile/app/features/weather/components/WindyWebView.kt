package com.sinc.mobile.app.features.weather.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Color

@Composable
fun WindyWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Forzamos fondo negro para evitar el bloque blanco inicial
                setBackgroundColor(Color.BLACK)
                
                webViewClient = WebViewClient()
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    
                    // Mejoras de renderizado
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    
                    // Permitir que el mapa sea fluido
                    setSupportZoom(true)
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                
                // Evitar que el contenedor Android intente hacer scroll
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}
