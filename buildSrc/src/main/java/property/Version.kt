package property

object Version {
    private const val MAJOR  = 2
    private const val middle = 0
    private const val minor  = 73

    init {
        if (middle >= 100 || minor >= 10_000) {
            throw IllegalStateException("Middle and Minor version value is allowed under 100.")
        }
    }

    val code = ((MAJOR * 1_000_000) + (middle * 10_000) + minor)

    val name = "${MAJOR}.${middle}.${minor}"

}
