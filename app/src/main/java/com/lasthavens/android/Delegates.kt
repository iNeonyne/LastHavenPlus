package com.lasthavens.android

import android.content.Context
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

/** Routes <input type="file"> to the Android document picker. */
class AppPromptDelegate(private val picker: FilePicker) : GeckoSession.PromptDelegate {
    override fun onFilePrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.FilePrompt,
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? = picker.request(prompt)
}
