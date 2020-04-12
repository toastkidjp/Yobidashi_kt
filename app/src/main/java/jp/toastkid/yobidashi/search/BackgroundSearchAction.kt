package jp.toastkid.yobidashi.search

import android.view.View

/**
 * Background search invoker.
 * TODO Delete it.
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
        SearchAction(
                snackbarParent.context,
                category ?: "",
                query ?: "",
                onBackground = true,
                saveHistory = saveHistory
        ).invoke()
    }

}