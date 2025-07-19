package property

object Version {
    private const val MAJOR  = 2
    private const val MIDDLE = 0
    private const val MINOR  = 98

    init {
        if (MIDDLE >= 100 || MINOR >= 10_000) {
            throw IllegalStateException("Middle and Minor version value is allowed under 100.")
        }
    }

    val code = ((MAJOR * 1_000_000) + (MIDDLE * 10_000) + MINOR)

    val name = "${MAJOR}.${MIDDLE}.${MINOR}"

}
