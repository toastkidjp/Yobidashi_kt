package jp.toastkid.yobidashi.settings

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringRes
import android.view.Menu
import android.view.MenuItem
import android.view.View
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.ActivitySettingsBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster

/**
 * Settings activity.
 *
 * @author toastkidjp
 */
class SettingsActivity : BaseActivity() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: ActivitySettingsBinding

    /**
     * Settings fragment.
     */
    private lateinit var fragment: SettingsTopFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, LAYOUT_ID)
        binding.activity = this
        setSupportActionBar(binding.toolbar)
        initToolbar(binding.toolbar)

        val transaction = supportFragmentManager.beginTransaction()
        fragment = SettingsTopFragment()
        transaction.replace(R.id.container, fragment)
        transaction.commit()

        binding.categoryGroup.check(R.id.category_all)
    }

    override fun onResume() {
        super.onResume()
        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.common, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.settings_toolbar_menu_exit) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @StringRes override fun titleId(): Int = R.string.title_settings

    /**
     * Switch all settings.
     *
     * @param ignored
     */
    fun switchAll(ignored: View) {
        fragment.showAll()
    }

    /**
     * Switch displaying menu.
     *
     * @param ignored
     */
    fun switchDisplaying(ignored: View) {
        fragment.showDisplay()
    }

    /**
     * Switch search menu.
     *
     * @param ignored
     */
    fun switchSearch(ignored: View) {
        fragment.showSearch()
    }

    /**
     * Switch browser menu.
     *
     * @param ignored
     */
    fun switchBrowser(ignored: View) {
        fragment.showBrowser()
    }

    fun switchEditor() {
        fragment.showEditor()
    }

    /**
     * Switch notification menu.
     *
     * @param ignored
     */
    fun switchNotification(ignored: View) {
        fragment.showNotifications()
    }

    /**
     * Switch other menu.
     *
     * @param ignored
     */
    fun switchOther(ignored: View) {
        fragment.showOthers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ColorFilter.REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toaster.snackShort(
                        binding.root,
                        R.string.message_cannot_draw_overlay,
                        colorPair()
                )
                return
            }
            ColorFilter(this, binding.root).start()
        }
    }

    companion object {

        /**
         * Layout ID.
         */
        private val LAYOUT_ID = R.layout.activity_settings

        /**
         * Make this activity's intent.
         * @param context
         *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
