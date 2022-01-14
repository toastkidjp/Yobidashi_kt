package jp.toastkid.lib.fragment

/**
 * Base fragment.
 *
 * @author toastkidjp
 */
interface CommonFragmentAction {

    /**
     * Event of press back key.
     *
     * @return is consumed event?
     */
    fun pressBack(): Boolean = false

    fun share() = Unit

}
