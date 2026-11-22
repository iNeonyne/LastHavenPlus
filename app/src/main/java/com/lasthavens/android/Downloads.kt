package com.lasthavens.android

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import org.mozilla.geckoview.WebResponse
import java.io.File

/** Saves files GeckoView hands to the app (e.g. add-on / backup ZIP downloads). */
object Downloads {
    fun save(context: Context, response: WebResponse) {
        val app = context.applicationContext
        val body = response.body
        if (body == null) {
            toast(app, app.getString(R.string.download_failed))
            return
        }
        val headers = response.headers
        fun header(name: String) = headers.entries.firstOrNull { it.key.equals(name, true) }?.value
        val mime = header("content-type")?.substringBefore(';')?.trim()?.ifEmpty { null } ?: "application/octet-stream"
        val name = fileName(response.uri, header("content-disposition"), mime)

        Thread {
            try {
                body.use { input ->
                    if (Build.VERSION.SDK_INT >= 29) {
                        val resolver = app.contentResolver
                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                            put(MediaStore.MediaColumns.MIME_TYPE, mime)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Lasthavens")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                        val target = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            ?: error("Could not create download entry")
                        resolver.openOutputStream(target)!!.use { out -> input.copyTo(out) }
                        values.clear()
                        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(target, values, null, null)
                        toast(app, app.getString(R.string.download_saved, name))
                    } else {
                        val dir = app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: app.filesDir
                        val file = File(dir, name)
                        file.outputStream().use { out -> input.copyTo(out) }
                        toast(app, app.getString(R.string.download_saved_app, file.absolutePath))
                    }
                }
            } catch (e: Exception) {
                toast(app, app.getString(R.string.download_failed))
            }
        }.start()
    }

    private fun fileName(uri: String, disposition: String?, mime: String): String {
        var name: String? = null
        if (disposition != null) {
            val m = Regex("filename\\*?=(?:UTF-8'')?\"?([^\";]+)\"?", RegexOption.IGNORE_CASE).find(disposition)
            name = m?.groupValues?.get(1)?.let { runCatching { Uri.decode(it) }.getOrDefault(it) }
        }
        if (name.isNullOrBlank() && !uri.startsWith("blob:")) {
            name = Uri.parse(uri).lastPathSegment
        }
        if (name.isNullOrBlank()) {
            val ext = when {
                mime.contains("zip") -> "zip"
                mime.contains("json") -> "json"
                mime.contains("7z") -> "7z"
                else -> "bin"
            }
            name = "lasthavens-${System.currentTimeMillis()}.$ext"
        }
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun toast(context: Context, text: String) {
        Handler(Looper.getMainLooper()).post { Toast.makeText(context, text, Toast.LENGTH_LONG).show() }
    }
}
