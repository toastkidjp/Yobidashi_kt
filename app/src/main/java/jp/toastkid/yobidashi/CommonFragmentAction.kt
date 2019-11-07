package jp.toastkid.yobidashi

import androidx.annotation.StringRes

/**
 * Base fragment.
 *
 * @author toastkidjp
 */
interface CommonFragmentAction {

    /**
     * Return this fragment's title ID.
     *
     * @return [StringRes]
     */
    @StringRes
    fun titleId(): Int

    /**
     * Event of press long back key.
     *
     * @return is event consumed
     */
    fun pressLongBack(): Boolean = false

    /**
     * Event of press back key.
     *
     * @return is consumed event?
     */
    fun pressBack(): Boolean = false

}
