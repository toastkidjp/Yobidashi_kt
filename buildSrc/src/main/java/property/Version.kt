package property

object Version {
    private const val MAJOR  = 2
    private const val MIDDLE = 1
    private const val MINOR  = 11

    init {
        if (MIDDLE >= 100 || MINOR >= 10_000) {
            throw IllegalStateException("Middle and Minor version value is allowed under 100.")
        }
    }

    const val code = (MAJOR * 1_000_000) + (MIDDLE * 10_000) + MINOR

    const val name = "${MAJOR}.${MIDDLE}.${MINOR}"

}
