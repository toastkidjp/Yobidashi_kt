package jp.toastkid.yobidashi

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat

import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Base activity.
 *
 * @author toastkidjp
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * Firebase analytics log sender.
     */
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
     *
     * @param toolbar Toolbar
     */
    protected fun initToolbar(toolbar: Toolbar) {
        toolbar.run {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener { finish() }
            setTitle(titleId())
            inflateMenu(R.menu.settings_toolbar_menu)
            setOnMenuItemClickListener{ clickMenu(it) }
            navigationIcon = null
        }
    }

    /**
     * Click menu action.
     *
     * @param item [MenuItem]
     */
    protected open fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.menu_exit) {
            moveTaskToBack(true)
            return true
        }
        if (itemId == R.id.menu_close) {
            finish()
            return true
        }
        return true
    }

    /**
     * Apply color to Toolbar.
     *
     * @param toolbar Toolbar
     */
    protected fun applyColorToToolbar(toolbar: Toolbar) {
        val pair = preferenceApplier.colorPair()
        toolbar.let {
            it.setBackgroundColor(pair.bgColor())

            val fontColor = pair.fontColor()
            it.setTitleTextColor(fontColor)
            it.setSubtitleTextColor(fontColor)

            applyTint(it.navigationIcon, fontColor)
            applyTint(it.overflowIcon, fontColor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color = ColorUtils.setAlphaComponent(pair.bgColor(), 255)
            window.statusBarColor     = color
            window.navigationBarColor = color
        }
    }

    /**
     * Apply tint to passed drawable.
     *
     * @param icon Drawable
     * @param fontColor color int
     */
    private fun applyTint(icon: Drawable?, @ColorInt fontColor: Int) =
            icon?.let { DrawableCompat.setTint(it, fontColor) }

    /**
     * Send log.
     *
     * @param key
     * @param bundle
     */
    @JvmOverloads
    protected fun sendLog(key: String, bundle: Bundle = Bundle.EMPTY) =
            sender?.send(key, bundle)

    /**
     * Return color pair.
     */
    protected fun colorPair(): ColorPair = preferenceApplier.colorPair()

    /**
     * Background image file path.
     */
    protected val backgroundImagePath: String
        get() = preferenceApplier.backgroundImagePath

    /**
     * Remove background image file path from preferences.
     */
    protected fun removeBackgroundImagePath() {
        preferenceApplier.removeBackgroundImagePath()
    }

    /**
     * Return title's string resource ID.
     *
     * @return title string ID
     */
    @StringRes
    protected abstract fun titleId(): Int
}