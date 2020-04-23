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
    TOP(R.string.title_menu_to_top, R.drawable.ic_top),

    BOTTOM(R.string.title_menu_to_bottom, R.drawable.ic_bottom),

    WIFI_SETTING(R.string.title_settings_wifi, R.drawable.ic_wifi),

    FIND_IN_PAGE(R.string.title_find_in_page, R.drawable.ic_find_in_page),

    VIEW_ARCHIVE(R.string.title_archives, R.drawable.ic_view_archive),

    SHARE(R.string.section_title_share, R.drawable.ic_share),

    LOAD_HOME(R.string.title_load_home, R.drawable.ic_home),

    VIEW_HISTORY(R.string.title_view_history, R.drawable.ic_history),

    BOOKMARK(R.string.title_bookmark, R.drawable.ic_bookmark),

    VOICE_SEARCH(R.string.title_voice_search, R.drawable.ic_mic),

    WEB_SEARCH(R.string.title_search, R.drawable.ic_search_white),

    RANDOM_WIKIPEDIA(R.string.menu_random_wikipedia, R.drawable.ic_wikipedia_white),

    WHAT_HAPPENED_TODAY(R.string.menu_what_happened_today, R.drawable.ic_what_happened_today),

    EDITOR(R.string.title_editor, R.drawable.ic_edit),

    PDF(R.string.title_open_pdf, R.drawable.ic_pdf),

    CODE_READER(R.string.title_code_reader, R.drawable.ic_barcode),

    SCHEDULE(R.string.title_calendar, R.drawable.ic_schedule),

    CAMERA(R.string.title_camera, R.drawable.ic_camera),

    PLANNING_POKER(R.string.title_planning_poker, R.drawable.ic_card),

    TORCH(R.string.title_torch, R.drawable.ic_light),

    APP_LAUNCHER(R.string.title_apps_launcher, R.drawable.ic_launcher),

    RSS_READER(R.string.title_rss_reader, R.drawable.ic_rss_feed),

    AUDIO(R.string.title_audio_player, R.drawable.ic_music),

    IMAGE_VIEWER(R.string.title_image_viewer, R.drawable.ic_photo),

    MEMORY_CLEANER(R.string.title_memory_cleaner, R.drawable.ic_cleaned),

    ABOUT(R.string.title_about_this_app, R.drawable.ic_yobidashi),
    ;

}