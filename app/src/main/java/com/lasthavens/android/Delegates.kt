package com.lasthavens.android

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebResponse

/** Content events shared by the game and Addon Manager screens. */
open class AppContentDelegate(
    private val context: Context,
    private val onClose: () -> Unit = {},
    private val onProcessGone: () -> Unit = {},
    private val onFullScreenChanged: (Boolean) -> Unit = {},
) : GeckoSession.ContentDelegate {

    // Downloads (including blob: downloads such as the backup ZIP) arrive here.
    override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
        Downloads.save(context, response)
    }

    override fun onCloseRequest(session: GeckoSession) = onClose()
    override fun onCrash(session: GeckoSession) = onProcessGone()
    override fun onKill(session: GeckoSession) = onProcessGone()
    override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) = onFullScreenChanged(fullScreen)
}

/** Bridges web prompts and <input type="file"> to native Android UI. */
class AppPromptDelegate(
    private val activity: AppCompatActivity,
    private val picker: FilePicker,
) : GeckoSession.PromptDelegate {

    override fun onFilePrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.FilePrompt,
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? = picker.request(prompt)

    override fun onAlertPrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.AlertPrompt,
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
        val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
        val dialog = AlertDialog.Builder(activity)
            .setTitle(prompt.title ?: activity.getString(R.string.app_name))
            .setMessage(prompt.message.orEmpty())
            .setPositiveButton(android.R.string.ok) { _, _ ->
                result.complete(prompt.dismiss())
            }
            .create()
        dialog.setOnCancelListener { result.complete(prompt.dismiss()) }
        dialog.show()
        return result
    }

    override fun onButtonPrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.ButtonPrompt,
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
        val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
        val dialog = AlertDialog.Builder(activity)
            .setTitle(prompt.title ?: activity.getString(R.string.app_name))
            .setMessage(prompt.message.orEmpty())
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                result.complete(
                    prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE),
                )
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                result.complete(
                    prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE),
                )
            }
            .create()
        dialog.setOnCancelListener {
            result.complete(
                prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE),
            )
        }
        dialog.show()
        return result
    }

    override fun onTextPrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.TextPrompt,
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
        val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
        val input = EditText(activity).apply {
            setSingleLine()
            setText(prompt.defaultValue.orEmpty())
            setSelection(text.length)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(prompt.title ?: activity.getString(R.string.app_name))
            .setMessage(prompt.message.orEmpty())
            .setView(input, 24, 0, 24, 0)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                result.complete(prompt.dismiss())
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                result.complete(prompt.confirm(input.text.toString()))
            }
            .create()
        dialog.setOnCancelListener { result.complete(prompt.dismiss()) }
        dialog.show()
        return result
    }
}
