package jp.toastkid.yobidashi

/**
 * Base fragment.
 *
 * @author toastkidjp
 */
interface CommonFragmentAction {

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
