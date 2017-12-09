package jp.toastkid.yobidashi.planning_poker

/**
 * @author toastkidjp
 */
internal enum class Suite constructor(private val text: String) {
    ZERO("0"), HALF("1/2"), ONE("1"), TWO("2"), THREE("3"), FIVE("5"), EIGHT("8"),
    THIRTEEN("13"), TWENTY("20"), FORTY("40"), HUNDRED("100"), QUESTION("?"), INFINITE("∞");

    fun text(): String = text
}
