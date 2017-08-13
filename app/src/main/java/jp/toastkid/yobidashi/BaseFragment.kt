package jp.toastkid.yobidashi

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.Fragment

import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Base fragment.

 * @author toastkidjp
 */
abstract class BaseFragment : Fragment() {

    /** Firebase analytics logger.  */
    private var logSender: LogSender? = null

    /** Preferences wrapper.  */
    private var preferenceApplier: PreferenceApplier? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logSender = LogSender(context)
        preferenceApplier = PreferenceApplier(context)
    }

    protected fun sendLog(key: String) {
        logSender!!.send(key)
    }

    /**
     * Return this fragment's title ID.

     * @return [StringRes]
     */
    @StringRes abstract fun titleId(): Int

    /**
     * Event of press back key.

     * @return is consumed event?
     */
    open fun pressBack(): Boolean {
        return false
    }

    /**
     * Get Preferences wrapper.
     * @return [PreferenceApplier]
     */
    fun preferenceApplier(): PreferenceApplier? {
        return preferenceApplier
    }

    /**
     * Get color pair.
     * @return [ColorPair]
     */
    fun colorPair(): ColorPair {
        return preferenceApplier!!.colorPair()
    }

}
