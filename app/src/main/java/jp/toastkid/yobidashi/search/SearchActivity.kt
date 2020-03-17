package jp.toastkid.yobidashi.search

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R

/**
 * Search activity.
 *
 * @author toastkidjp
 */
class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        val currentTitle = intent.getStringExtra(EXTRA_KEY_TITLE)
        val currentUrl = intent.getStringExtra(EXTRA_KEY_URL)

        val fragment = SearchFragment()
        fragment.arguments = bundleOf(
                EXTRA_KEY_TITLE to currentTitle,
                EXTRA_KEY_URL to currentUrl,
                EXTRA_KEY_QUERY to intent.getStringExtra(EXTRA_KEY_QUERY)
        )

        replaceFragment(fragment)
    }

    // TODO should use main activity.
    fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
                R.anim.slide_in_right, 0, 0, android.R.anim.slide_out_right)
        transaction?.replace(R.id.content, fragment, fragment::class.java.canonicalName)
        transaction?.addToBackStack(fragment::class.java.canonicalName)
        transaction?.commit()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_empty

        /**
         * Extra key of query.
         */
        private const val EXTRA_KEY_QUERY = "query"

        /**
         * Extra key of title.
         */
        private const val EXTRA_KEY_TITLE = "title"

        /**
         * Extra key of URL.
         */
        private const val EXTRA_KEY_URL = "url"

    }
}
