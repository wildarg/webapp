package com.uob.jsbridgedemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val IMAGE_CAPTURE = 1

class MainActivity : AppCompatActivity() {

    private val jsBridge by lazy { JSBridge(this, web_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView(web_view)
        web_view.loadUrl("file:///android_asset/demo_page.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(view: WebView) {
        view.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }
        view.addJavascriptInterface(jsBridge, "JSBridge")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IMAGE_CAPTURE   -> {
                web_view.evaluateJavascript("javascript: ${jsBridge.loadPhotoCallback}(\"${jsBridge.currentPhotoPath}\")", null)
            }
        }
    }
}


class JSBridge(private val context: AppCompatActivity, private val webView: WebView) {

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun takeShot(callback: String) {
        loadPhotoCallback = callback
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        val photoUri = FileProvider.getUriForFile(context, "com.uob.jsbridgedemo.FileProvider", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        context.startActivityForResult(intent, IMAGE_CAPTURE)
    }

    @JavascriptInterface
    fun makeCall(phone: String) {
        Intent(Intent.ACTION_DIAL)
            .apply { setData(Uri.parse("tel:$phone")) }
            .let(context::startActivity)
    }

    var currentPhotoPath: String = ""
    var loadPhotoCallback: String = "";

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


}
