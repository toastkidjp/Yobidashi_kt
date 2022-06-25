package jp.toastkid.yobidashi.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R

/**
 * In App Browser's circular menu.
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

    WEB_SEARCH(R.string.title_search, R.drawable.ic_web_search),

    SHARE(R.string.section_title_share, R.drawable.ic_share),

    VIEW_ARCHIVE(R.string.title_archives, R.drawable.ic_view_archive),

    VIEW_HISTORY(R.string.title_view_history, R.drawable.ic_history),

    BOOKMARK(R.string.title_bookmark, R.drawable.ic_bookmark),

    CALENDAR(R.string.title_calendar, R.drawable.ic_calendar),

    CODE_READER(R.string.title_code_reader, R.drawable.ic_barcode),

    LOAN_CALCULATOR(R.string.title_loan_calculator, R.drawable.ic_loan_calculator),

    RSS_READER(R.string.title_rss_reader, R.drawable.ic_rss_feed),

    NUMBER_PLACE(R.string.title_number_place, R.drawable.ic_number_place),

    AUDIO(R.string.title_audio_player, R.drawable.ic_music),

    IMAGE_VIEWER(R.string.title_image_viewer, R.drawable.ic_photo),

    ABOUT_THIS_APP(R.string.title_about_this_app, R.drawable.ic_yobidashi),

    TODO_TASKS_BOARD(R.string.title_todo_tasks_board, R.drawable.ic_board),

    TODO_TASKS(R.string.title_todo_tasks, R.drawable.ic_todo_task),
    ;

}