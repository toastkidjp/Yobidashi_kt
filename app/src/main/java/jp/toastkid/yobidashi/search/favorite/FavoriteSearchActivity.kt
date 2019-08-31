package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityEmptyBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class FavoriteSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmptyBinding

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView<ActivityEmptyBinding>(this, LAYOUT_ID)

        binding.toolbar.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(titleId())
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }

        val fragment = FavoriteSearchFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment)
        transaction.commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()

        ToolbarColorApplier()(window, binding.toolbar, preferenceApplier.colorPair())

        ImageLoader.setImageToImageView(binding.background, preferenceApplier.backgroundImagePath)
    }

    private fun clickMenu(item: MenuItem) = when (item.itemId) {
        R.id.menu_exit -> {
            moveTaskToBack(true)
            true
        }
        R.id.menu_close -> {
            finish()
            true
        }
        else -> true
    }

    private fun titleId(): Int = R.string.title_favorite_search

    companion object {
        @LayoutRes const val LAYOUT_ID: Int = R.layout.activity_empty

        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, FavoriteSearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}