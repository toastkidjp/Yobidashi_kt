package jp.toastkid.yobidashi.libs.intent

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import timber.log.Timber

/**
 * For invoking implicit intent.
 *
 * @author toastkidjp
 */
class ImplicitIntentInvokerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val textInputLayout = TextInputs.make(activityContext)
        TextInputs.setEmptyAlert(textInputLayout)

        Clipboard.getPrimary(activityContext)?.let { textInputLayout.editText?.setText(it) }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_invoker_dialog)
                .setMessage(R.string.message_invoker_dialog)
                .setView(textInputLayout)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.invoke) { d, _ ->
                    textInputLayout.editText?.text?.let { invoke(activityContext, it.toString()) }
                    d.dismiss()
                }
                .create()
    }

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

}