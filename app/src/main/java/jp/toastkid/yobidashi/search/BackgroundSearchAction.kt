package jp.toastkid.yobidashi.search

import android.content.Context
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Background search invoker.
 *
 * @param snackbarParent
 * @param category
 * @param query
 * @param saveHistory
 * @author toastkidjp
 */
class BackgroundSearchAction(
        private val snackbarParent: View,
        private val category: String?,
        private val query: String?,
        private val saveHistory: Boolean = true
        ) {

    /**
     * Invoke action.
     */
    fun invoke() {
        val context: Context = snackbarParent.context
        invokeSearchAction(context)
        showSnack(context)
    }

    /**
     * Invoke search action.
     *
     * @param context
     */
    private fun invokeSearchAction(context: Context) {
        SearchAction(
                context,
                category ?: "",
                query ?: "",
                onBackground = true,
                saveHistory = saveHistory
        ).invoke()
    }

    /**
     * Show snackbar.
     *
     * @param context
     */
    private fun showSnack(context: Context) {
        Toaster.snackShort(
                snackbarParent,
                context.getString(R.string.message_background_search, query),
                PreferenceApplier(context).colorPair()
        )
    }
}