package de.spritradar.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val executor = Executors.newSingleThreadExecutor()
    private val locationRequest = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?, callback: GeolocationPermissions.Callback?
            ) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    callback?.invoke(origin, true, false)
                } else {
                    pendingGeoCallback = callback
                    pendingGeoOrigin = origin
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        locationRequest
                    )
                }
            }
        }
        web.addJavascriptInterface(AndroidBridge(), "Android")
        web.loadUrl("file:///android_asset/index.html")
    }

    private var pendingGeoCallback: GeolocationPermissions.Callback? = null
    private var pendingGeoOrigin: String? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequest) {
            val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            pendingGeoCallback?.invoke(pendingGeoOrigin, granted, false)
            pendingGeoCallback = null
            pendingGeoOrigin = null
        }
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun searchStations(lat: Double, lng: Double, radius: Int, fuel: String) {
            executor.execute {
                var connection: HttpURLConnection? = null
                try {
                    val safeFuel = if (fuel in listOf("e5", "e10", "diesel")) fuel else "e10"
                    val query = "lat=$lat&lng=$lng&rad=${radius.coerceIn(1, 25)}&fuel=$safeFuel&limit=250"
                    val url = URL("https://api.tankpuls.de/api/stations/search?$query")
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 15000
                        readTimeout = 15000
                        setRequestProperty("Accept", "application/json")
                    }
                    val code = connection.responseCode
                    val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                    val body = stream.bufferedReader().use { it.readText() }
                    if (code !in 200..299) throw IllegalStateException("API-Fehler ($code)")
                    val json = JSONObject(body)
                    runOnUiThread {
                        web.evaluateJavascript("window.onStationsResponse(${JSONObject.quote(json.toString())})", null)
                    }
                } catch (e: Exception) {
                    val msg = e.message ?: "Verbindung fehlgeschlagen"
                    runOnUiThread {
                        web.evaluateJavascript("window.onStationsError(${JSONObject.quote(msg)})", null)
                    }
                } finally {
                    connection?.disconnect()
                }
            }
        }

        @JavascriptInterface
        fun openNavigation(lat: Double, lng: Double, label: String) {
            runOnUiThread {
                val uri = android.net.Uri.parse("google.navigation:q=$lat,$lng")
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                try { startActivity(intent) } catch (_: Exception) {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")))
                }
            }
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        web.destroy()
        super.onDestroy()
    }
}
