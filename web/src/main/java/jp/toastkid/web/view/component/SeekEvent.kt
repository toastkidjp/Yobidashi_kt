package jp.toastkid.web.view.component

data class SeekEvent(
    private val hour: Int,
    private val minute: Int
) {

    fun toOffsetSeconds(): Int {
        return (hour * 60 + minute) * 60
    }

}