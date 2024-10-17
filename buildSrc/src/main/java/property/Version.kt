package property

object Version {
    private const val MAJOR  = 2
    private const val MIDDLE = 0
    private const val minor  = 75

    init {
        if (MIDDLE >= 100 || minor >= 10_000) {
            throw IllegalStateException("Middle and Minor version value is allowed under 100.")
        }
    }

    val code = ((MAJOR * 1_000_000) + (MIDDLE * 10_000) + minor)

    val name = "${MAJOR}.${MIDDLE}.${minor}"

}
