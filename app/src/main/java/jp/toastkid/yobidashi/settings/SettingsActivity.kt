package jp.toastkid.yobidashi.settings

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySettingsBinding
import jp.toastkid.yobidashi.libs.ImageLoader

/**
 * Settings activity.

 * @author toastkidjp
 */
class SettingsActivity : BaseActivity() {

    /** DataBinding object.  */
    private lateinit var binding: ActivitySettingsBinding

    /** Settings fragment.  */
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

    override fun titleId(): Int {
        return R.string.title_settings
    }

    /**
     * Switch all settings.

     * @param v
     */
    fun switchAll(v: View) {
        fragment.showAll()
    }

    /**
     * Switch displaying menu.

     * @param v
     */
    fun switchDisplaying(v: View) {
        fragment.showDisplay()
    }

    fun switchSearch(v: View) {
        fragment.showSearch()
    }

    /**
     * Switch browser menu.

     * @param v
     */
    fun switchBrowser(v: View) {
        fragment.showBrowser()
    }

    /**
     * Switch notification menu.

     * @param v
     */
    fun switchNotification(v: View) {
        fragment.showNotifications()
    }

    /**
     * Switch others menu.

     * @param v
     */
    fun switchOthers(v: View) {
        fragment.showOthers()
    }

    companion object {

        /** Layout ID.  */
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
