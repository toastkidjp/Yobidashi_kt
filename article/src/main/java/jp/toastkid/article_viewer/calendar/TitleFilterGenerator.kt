package jp.toastkid.article_viewer.calendar

/**
 * Title filtering query generator.
 *
 * @author toastkidjp
 */
object TitleFilterGenerator {

    /**
     * Make filtering query with year, month, and date.
     *
     * @param year ex) 2019
     * @param month You should specify 1-12
     * @param date You should specify 1-31
     */
    operator fun invoke(year: Int, month: Int, date: Int): String {
        val monthStr = if (month < 10) "0$month" else month.toString()
        val dateStr = if (date < 10) "0$date" else date.toString()
        return "$year-$monthStr-$dateStr%"
    }
}