package property

object Version {
    private val major  = 2
    private val middle = 0
    private val minor  = 61

    init {
        if (middle >= 100 || minor >= 10_000) {
            throw IllegalStateException("Middle and Minor version value is allowed under 100.")
        }
    }

    val code = ((major * 1_000_000) + (middle * 10_000) + minor) 

    val name = "${major}.${middle}.${minor}" 

}
