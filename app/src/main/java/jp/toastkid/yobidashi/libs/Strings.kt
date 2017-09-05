package jp.toastkid.yobidashi.libs

/**
 * String utilities.

 * @author toastkidjp
 */
class Strings
/**
 * Disallow call from other class.
 */
private constructor() {

    init {
        throw IllegalStateException()
    }

    companion object {

        /**
         * Return containing multi byte.
         * @param str
         *
         * @return
         */
        fun containsMultiByte(str: String): Boolean {
            return str.length != str.toByteArray().size
        }
    }

}
