package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.MenuItem
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityEmptyBinding
import jp.toastkid.yobidashi.libs.ImageLoader

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class FavoriteSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityEmptyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityEmptyBinding>(this, LAYOUT_ID)

        initToolbar(binding.toolbar)

        val fragment = FavoriteSearchFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment)
        transaction.commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()

        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.close) {
            finish()
            return true
        }
        return super.clickMenu(item)
    }

    override fun titleId(): Int = R.string.title_favorite_search

    companion object {
        @LayoutRes const val LAYOUT_ID: Int = R.layout.activity_empty

        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, FavoriteSearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}