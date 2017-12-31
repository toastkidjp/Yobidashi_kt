package jp.toastkid.yobidashi

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.Fragment

import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Base fragment.
 *
 * @author toastkidjp
 */
abstract class BaseFragment : Fragment() {

    /** Firebase analytics logger.  */
    private lateinit var logSender: LogSender

    /** Preferences wrapper.  */
    private lateinit var preferenceApplier: PreferenceApplier

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logSender = LogSender(context)
        preferenceApplier = PreferenceApplier(context)
    }

    /**
     * Send log which has only key.
     */
    internal fun sendLog(key: String) {
        logSender.send(key)
    }

    /**
     * Return this fragment's title ID.
     *
     * @return [StringRes]
     */
    @StringRes abstract fun titleId(): Int

    /**
     * Event of press long back key.
     */
    open fun pressLongBack(): Boolean = false

    /**
     * Event of press back key.
     *
     * @return is consumed event?
     */
    open fun pressBack(): Boolean = false

    /**
     * Header tap action.
     */
    open fun tapHeader() = Unit

    /**
     * Get Preferences wrapper.
     *
     * @return [PreferenceApplier]
     */
    fun preferenceApplier(): PreferenceApplier = preferenceApplier

    /**
     * Get color pair.
     *
     * @return [ColorPair]
     */
    fun colorPair(): ColorPair = preferenceApplier.colorPair()

}
