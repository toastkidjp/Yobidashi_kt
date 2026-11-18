package jp.toastkid.web.view.component

interface SeekEvent {

    fun hour(): Int

    fun minute(): Int

    fun toOffsetSeconds(): Int {
        return (hour() * 60 + minute()) * 60
    }

}

data class BackwardSeekEvent(
    private val hour: Int,
    private val minute: Int
) : SeekEvent {

    override fun hour(): Int = this.hour

    override fun minute(): Int = this.minute

}

data class RelativeSeekEvent(
    private val hour: Int,
    private val minute: Int
) : SeekEvent {

    override fun hour(): Int = this.hour

    override fun minute(): Int = this.minute

}
