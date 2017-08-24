package jp.toastkid.jitte.browser

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import jp.toastkid.jitte.R
import jp.toastkid.jitte.browser.archive.Archive

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

    RELOAD(R.string.title_menu_reload, R.drawable.ic_reload),

    TOP(R.string.title_menu_to_top, R.drawable.ic_top),

    BOTTOM(R.string.title_menu_to_bottom, R.drawable.ic_bottom),

    BACK(R.string.title_menu_back, R.drawable.ic_back),

    FORWARD(R.string.title_menu_forward, R.drawable.ic_forward),

    PAGE_INFORMATION(R.string.title_menu_page_information, R.drawable.ic_info),

    CLOSE(R.string.close_menu, R.drawable.ic_close_white),

    SETTING(R.string.title_settings, R.drawable.ic_settings),

    USER_AGENT(R.string.title_user_agent, R.drawable.ic_user_agent),

    WIFI_SETTING(R.string.title_settings_wifi, R.drawable.ic_wifi),

    CLEAR_CACHE(R.string.title_clear_cache, R.drawable.ic_clear_cache),

    CLEAR_FORM_DATA(R.string.clear_form_data, R.drawable.ic_clear_form),

    OPEN(R.string.title_open_url, R.drawable.ic_web),

    OTHER_BROWSER(R.string.title_open_other_browser, R.drawable.ic_open_with),

    CHROME_TAB(R.string.title_open_chrome, R.drawable.ic_chrome),

    BARCODE_READER(R.string.title_code_reader, R.drawable.ic_barcode),

    FIND_IN_PAGE(R.string.title_find_in_page, R.drawable.ic_find_in_page),

    SCREENSHOT(R.string.title_screenshot, R.drawable.ic_camera),

    ARCHIVE(R.string.title_archive, R.drawable.ic_archive),

    VIEW_ARCHIVE(R.string.title_archives, R.drawable.ic_view_archive),

    SEARCH(R.string.title_search_action, R.drawable.ic_search_white),

    VOICE_SEARCH(R.string.title_voice_search, R.drawable.ic_mic),

    SITE_SEARCH(R.string.title_site_search_by_google, R.drawable.googleg_disabled_color_18),

    SHARE(R.string.section_title_share, R.drawable.ic_share),

    SHARE_BARCODE(R.string.title_share_by_code, R.drawable.ic_barcode),

    COLOR_FILTER(R.string.title_color_filter, R.drawable.ic_color_filter),

    REPLACE_HOME(R.string.title_replace_home, R.drawable.ic_replace_home),

    LOAD_HOME(R.string.title_load_home, R.drawable.ic_home),

    EXIT(R.string.exit, R.drawable.ic_exit);

    companion object {
        val list: List<Menu> =
                if (Archive.canUseArchive()) { values().toList() }
                else { values().filter{  (it != VIEW_ARCHIVE || it != ARCHIVE) } }
    }
}