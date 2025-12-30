package jp.toastkid.yobidashi.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R

/**
 * App's circular menu.
 *
 * @param titleId Menu title resource ID
 * @param iconId Menu icon resource ID
 * @author toastkidjp
 */
enum class Menu(
        @param:StringRes val titleId: Int,
        @param:DrawableRes val iconId: Int
) {
    FIND_IN_PAGE(R.string.title_find_in_page, R.drawable.ic_find_in_page),

    TOP(R.string.title_menu_to_top, R.drawable.ic_top),

    BOTTOM(R.string.title_menu_to_bottom, R.drawable.ic_bottom),

    WEB_SEARCH(jp.toastkid.lib.R.string.title_search, R.drawable.ic_web_search),

    SHARE(R.string.section_title_share, jp.toastkid.lib.R.drawable.ic_share),

    VIEW_ARCHIVE(R.string.title_archives, R.drawable.ic_view_archive),

    VIEW_HISTORY(jp.toastkid.lib.R.string.title_view_history, jp.toastkid.web.R.drawable.ic_history),

    BOOKMARK(jp.toastkid.web.R.string.title_bookmark, jp.toastkid.lib.R.drawable.ic_bookmark),

    CALENDAR(R.string.title_calendar, R.drawable.ic_calendar),

    CODE_READER(R.string.title_barcode_reader, R.drawable.ic_barcode),

    LOAN_CALCULATOR(jp.toastkid.loan.R.string.title_loan_calculator, jp.toastkid.loan.R.drawable.ic_loan_calculator),

    CONVERTER_TOOL(R.string.title_converter_tool, R.drawable.ic_converter),

    RSS_READER(R.string.title_rss_reader, jp.toastkid.rss.R.drawable.ic_rss_feed),

    NUMBER_PLACE(R.string.title_number_place, R.drawable.ic_number_place),

    AUDIO(R.string.title_audio_player, R.drawable.ic_music),

    IMAGE_VIEWER(R.string.title_image_viewer, R.drawable.ic_photo),

    ABOUT_THIS_APP(jp.toastkid.about.R.string.title_about_this_app, R.drawable.ic_yobidashi),

    CHAT(R.string.title_chat, R.drawable.ic_chat),

    WORLD_TIME(R.string.title_world_time, R.drawable.ic_world_time),

    SENSOR(R.string.title_sensor, R.drawable.ic_board),

    TODO_TASKS_BOARD(jp.toastkid.web.R.string.title_todo_tasks_board, R.drawable.ic_board),

    TODO_TASKS(jp.toastkid.web.R.string.title_todo_tasks, R.drawable.ic_todo_task),
    ;

}