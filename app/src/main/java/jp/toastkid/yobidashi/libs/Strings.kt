package jp.toastkid.yobidashi.libs

/**
 * String utilities.
 *
 * @author toastkidjp
 */
object Strings {

    /**
     * Return containing multi byte.
     *
     * @param str
     * @return If passed 'str' contains multi byte character, return true.
     */
    fun containsMultiByte(str: String): Boolean = str.length != str.toByteArray().size
}
