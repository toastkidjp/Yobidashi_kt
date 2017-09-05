package jp.toastkid.yobidashi

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.v4.graphics.ColorUtils
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem

import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
abstract class BaseActivity : AppCompatActivity() {

    /** Firebase analytics log sender.  */
    private var sender: LogSender? = null

    /**
     * Preference Applier.
     * FIXME: remove it
     * @return [PreferenceApplier]
     */
    protected lateinit var preferenceApplier: PreferenceApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sender = LogSender(this)
        sendLog("launch")
        preferenceApplier = PreferenceApplier(this)
    }

    /**
     * Initialize Toolbar.
     * @param toolbar Toolbar
     */
    protected fun initToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { v -> finish() }
        toolbar.setTitle(titleId())
        toolbar.inflateMenu(R.menu.settings_toolbar_menu)
        toolbar.setOnMenuItemClickListener{ this.clickMenu(it) }
    }

    protected open fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.settings_toolbar_menu_exit) {
            finish()
            return true
        }
        return true
    }

    /**
     * Apply color to Toolbar.
     * @param toolbar Toolbar
     */
    protected fun applyColorToToolbar(toolbar: Toolbar) {
        val pair = preferenceApplier!!.colorPair()
        toolbar.setBackgroundColor(pair.bgColor())
        toolbar.setTitleTextColor(pair.fontColor())
        toolbar.setSubtitleTextColor(pair.fontColor())

        applyTint(toolbar.navigationIcon, pair.fontColor())
        applyTint(toolbar.overflowIcon, pair.fontColor())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ColorUtils.setAlphaComponent(pair.bgColor(), 255)
        }
    }

    private fun applyTint(icon: Drawable?, @ColorInt fontColor: Int) {
        if (icon != null) {
            DrawableCompat.setTint(icon, fontColor)
        }
    }

    /**
     * Send log.
     *
     * @param key
     * @param bundle
     */
    @JvmOverloads protected fun sendLog(key: String, bundle: Bundle = Bundle.EMPTY) {
        sender!!.send(key, bundle)
    }

    protected fun colorPair(): ColorPair {
        return preferenceApplier!!.colorPair()
    }

    protected val backgroundImagePath: String
        get() = preferenceApplier!!.backgroundImagePath

    protected fun removeBackgroundImagePath() {
        preferenceApplier!!.removeBackgroundImagePath()
    }

    protected fun clearPreferences() {
        preferenceApplier!!.clear()
    }

    @StringRes protected abstract fun titleId(): Int
}