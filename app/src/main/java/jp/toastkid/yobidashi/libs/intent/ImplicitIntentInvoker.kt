package jp.toastkid.yobidashi.libs.intent

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import timber.log.Timber

/**
 * For invoking implicit intent.
 * @author toastkidjp
 */
object ImplicitIntentInvoker {

    /**
     * Invoke intent with specified action name.
     *
     * @param context
     * @param actionName
     */
    fun invoke(context: Context, actionName: String) {
        try {
            context.startActivity(Intent(actionName))
            Toaster.tShort(context, context.getString(R.string.message_invoking_intent, actionName))
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            Toaster.tShort(
                    context,
                    context.getString(R.string.message_invoking_intent_failed, actionName)
            )
        }
    }

    /**
     * Show dialog.
     *
     * @param context
     */
    fun showDialog(context: Context) {Intent.ACTION_SEND
        val textInputLayout = TextInputs.make(context)
        TextInputs.setEmptyAlert(textInputLayout)
        Clipboard.getPrimary(context)?.let { textInputLayout.editText?.setText(it) }
        AlertDialog.Builder(context)
                .setTitle(R.string.title_invoker_dialog)
                .setMessage(R.string.message_invoker_dialog)
                .setView(textInputLayout)
                .setNegativeButton(R.string.cancel, { d, i -> d.cancel() })
                .setPositiveButton(R.string.invoke, { d, i ->
                    textInputLayout.editText?.text?.let { invoke(context, it.toString()) }
                    d.dismiss()
                })
                .show()
    }
}