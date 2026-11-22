package com.lasthavens.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession.PromptDelegate.FilePrompt
import org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse

/**
 * GeckoView has no built-in file chooser. This bridges <input type="file"> to the
 * Android Storage Access Framework (needed for add-on ZIP/7z import and backup restore).
 * Must be created while the activity is being constructed (before STARTED).
 */
class FilePicker(private val activity: ComponentActivity) {
    private var pending: Pair<FilePrompt, GeckoResult<PromptResponse>>? = null

    private val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val (prompt, response) = pending ?: return@registerForActivityResult
        pending = null
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) {
            response.complete(prompt.dismiss())
            return@registerForActivityResult
        }
        val uris = mutableListOf<Uri>()
        data.clipData?.let { clip -> for (i in 0 until clip.itemCount) uris += clip.getItemAt(i).uri }
        if (uris.isEmpty()) data.data?.let { uris += it }
        when {
            uris.isEmpty() -> response.complete(prompt.dismiss())
            prompt.type == FilePrompt.Type.MULTIPLE -> response.complete(prompt.confirm(activity, uris.toTypedArray()))
            else -> response.complete(prompt.confirm(activity, uris.first()))
        }
    }

    fun request(prompt: FilePrompt): GeckoResult<PromptResponse> {
        val response = GeckoResult<PromptResponse>()
        pending?.let { (old, oldResponse) -> oldResponse.complete(old.dismiss()) }
        pending = prompt to response

        // No MIME filter on purpose: providers label ZIP/7z files inconsistently
        // (application/zip, x-zip-compressed, octet-stream) and a filter would hide them.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, prompt.type == FilePrompt.Type.MULTIPLE)
        }
        launcher.launch(intent)
        return response
    }
}
