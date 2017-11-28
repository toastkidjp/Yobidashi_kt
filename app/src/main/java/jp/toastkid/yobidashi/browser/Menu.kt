package jp.toastkid.yobidashi.browser

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.Archive

/**
 * In App Browser's circular menu.
 *
 * @author toastkidjp
 */
internal enum class Menu(
        @param:StringRes val titleId: Int,
        @param:DrawableRes val iconId: Int
) {

    TAB_LIST(R.string.title_tab_list, R.drawable.ic_tab),

    STOP_LOADING(R.string.title_stop_loading, R.drawable.ic_close),

    TAB_HISTORY(R.string.title_tab_histories, R.drawable.ic_history),

    TOP(R.string.title_menu_to_top, R.drawable.ic_top),

    BOTTOM(R.string.title_menu_to_bottom, R.drawable.ic_bottom),

    BACK(R.string.title_menu_back, R.drawable.ic_back),

    FORWARD(R.string.title_menu_forward, R.drawable.ic_forward),

    RELOAD(R.string.title_menu_reload, R.drawable.ic_reload),

    PAGE_INFORMATION(R.string.title_menu_page_information, R.drawable.ic_info),

    USER_AGENT(R.string.title_user_agent, R.drawable.ic_user_agent),

    WIFI_SETTING(R.string.title_settings_wifi, R.drawable.ic_wifi),

    OPEN(R.string.title_open_url, R.drawable.ic_web),

    OTHER_BROWSER(R.string.title_open_other_browser, R.drawable.ic_open_with),

    FIND_IN_PAGE(R.string.title_find_in_page, R.drawable.ic_find_in_page),

    SCREENSHOT(R.string.title_screenshot, R.drawable.ic_camera),

    ARCHIVE(R.string.title_archive, R.drawable.ic_archive),

    SHARE(R.string.section_title_share, R.drawable.ic_share),

    SHARE_BARCODE(R.string.title_share_by_code, R.drawable.ic_barcode),

    REPLACE_HOME(R.string.title_replace_home, R.drawable.ic_replace_home),

    LOAD_HOME(R.string.title_load_home, R.drawable.ic_home),

    VIEW_HISTORY(R.string.title_view_history, R.drawable.ic_history),

    ADD_BOOKMARK(R.string.title_add_bookmark, R.drawable.ic_bookmark_star),

    VOICE_SEARCH(R.string.title_voice_search, R.drawable.ic_mic),

    SITE_SEARCH(R.string.title_site_search_by_google, R.drawable.ic_google),

    SEARCH(R.string.title_search_action, R.drawable.ic_search_white),

    SETTING(R.string.title_settings, R.drawable.ic_settings),

    EDITOR(R.string.title_editor, R.drawable.ic_edit),

    EXIT(R.string.exit, R.drawable.ic_exit)
    ;

    companion object {
        val list: List<Menu> =
                if (Archive.canUseArchive()) { values().toList() }
                else { values().filter{  it != ARCHIVE } }
    }
}