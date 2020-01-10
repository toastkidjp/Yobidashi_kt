package jp.toastkid.yobidashi.libs.preference

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.menu.MenuPos
import jp.toastkid.yobidashi.search.SearchCategory
import java.io.File
import java.util.*
import kotlin.math.max

/**
 * Preferences wrapper.

 * @author toastkidjp
 */
class PreferenceApplier(private val context: Context) {

    @SuppressWarnings("unused")
    @Deprecated("These keys are deprecated.")
    private enum class DefunctKey {
        USE_DAILY_ALARM, USE_INTERNAL_BROWSER
    }

    private enum class Key {
        BG_COLOR, FONT_COLOR,
        ENABLE_SUGGESTION, ENABLE_SEARCH_HISTORY, ENABLE_VIEW_HISTORY, ENABLE_URL_MODULE,
        ENABLE_FAVORITE_SEARCH, ENABLE_APP_SEARCH,
        BG_IMAGE, LAST_AD_DATE,
        USE_NOTIFICATION_WIDGET, USE_INTERNAL_BROWSER, RETAIN_TABS, USE_JS, MENU_POS,
        LOAD_IMAGE, SAVE_FORM, USER_AGENT, HOME_URL, USE_COLOR_FILTER, FILTER_COLOR,
        DEFAULT_SEARCH_ENGINE, ENABLE_SEARCH_QUERY_EXTRACT, ENABLE_SEARCH_WITH_CLIP, START_UP, SAVE_VIEW_HISTORY,
        FULL_SCREEN, SCREEN_MODE, USE_INVERSION, WIFI_ONLY_MODE, AD_REMOVE, WEB_VIEW_POOL_SIZE,
        EDITOR_BACKGROUND_COLOR, EDITOR_FONT_COLOR, EDITOR_CURSOR_COLOR, EDITOR_HIGHLIGHT_COLOR,
        EDITOR_FONT_SIZE, CAMERA_FAB_BUTTON_POSITION_X, CAMERA_FAB_BUTTON_POSITION_Y,
        MENU_FAB_BUTTON_POSITION_X, MENU_FAB_BUTTON_POSITION_Y,
        WEB_VIEW_BACKGROUND_ALPHA, RSS_READER_TARGETS
    }

    private val preferences: SharedPreferences =
            context.getSharedPreferences(javaClass.canonicalName, Context.MODE_PRIVATE)

    var color: Int
        get() = preferences.getInt(Key.BG_COLOR.name, ContextCompat.getColor(context, R.color.colorPrimaryDark))
        set(color) = preferences.edit().putInt(Key.BG_COLOR.name, color).apply()

    var fontColor: Int
        get() = preferences.getInt(Key.FONT_COLOR.name, ContextCompat.getColor(context, R.color.textPrimary))
        set(color) = preferences.edit().putInt(Key.FONT_COLOR.name, color).apply()

    fun colorPair(): ColorPair = ColorPair(color, fontColor)

    val isEnableSuggestion: Boolean
        get() = preferences.getBoolean(Key.ENABLE_SUGGESTION.name, true)

    val isDisableSuggestion: Boolean
        get() = !isEnableSuggestion

    fun switchEnableSuggestion() {
        preferences.edit().putBoolean(Key.ENABLE_SUGGESTION.name, !isEnableSuggestion).apply()
    }

    val isEnableSearchHistory: Boolean
        get() = preferences.getBoolean(Key.ENABLE_SEARCH_HISTORY.name, true)

    fun switchEnableSearchHistory() {
        preferences.edit().putBoolean(Key.ENABLE_SEARCH_HISTORY.name, !isEnableSearchHistory)
                .apply()
    }

    val isEnableFavoriteSearch: Boolean
        get() = preferences.getBoolean(Key.ENABLE_FAVORITE_SEARCH.name, true)

    fun switchEnableFavoriteSearch() {
        preferences.edit().putBoolean(Key.ENABLE_FAVORITE_SEARCH.name, !isEnableFavoriteSearch)
                .apply()
    }

    val isEnableViewHistory: Boolean
        get() = preferences.getBoolean(Key.ENABLE_VIEW_HISTORY.name, true)

    fun switchEnableViewHistory() {
        preferences.edit().putBoolean(Key.ENABLE_VIEW_HISTORY.name, !isEnableViewHistory)
                .apply()
    }

    fun isEnableUrlModule(): Boolean {
        return preferences.getBoolean(Key.ENABLE_URL_MODULE.name, true)
    }

    fun switchEnableUrlModule() {
        preferences.edit().putBoolean(Key.ENABLE_URL_MODULE.name, !isEnableUrlModule())
                .apply()
    }

    fun isEnableAppSearch(): Boolean {
        return preferences.getBoolean(Key.ENABLE_APP_SEARCH.name, true)
    }

    fun switchEnableAppSearch() {
        preferences.edit().putBoolean(Key.ENABLE_APP_SEARCH.name, !isEnableAppSearch()).apply()
    }

    var backgroundImagePath: String
        get() = preferences.getString(Key.BG_IMAGE.name, "")
        set(path) = preferences.edit().putString(Key.BG_IMAGE.name, path).apply()

    fun hasBackgroundImagePath(): Boolean = backgroundImagePath.isNotEmpty()

    fun removeBackgroundImagePath() {
        preferences.edit().remove(Key.BG_IMAGE.name).apply()
    }

    val isFirstLaunch: Boolean
        get() {
            val firstLaunch = File(context.filesDir, "firstLaunch")
            if (firstLaunch.exists()) {
                return false
            }
            firstLaunch.mkdirs()
            return true
        }

    fun updateLastAd() {
        preferences.edit()
                .putInt(Key.LAST_AD_DATE.name, Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_YEAR))
                .apply()
    }

    fun allowShowingAd(): Boolean {
        val today = Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_YEAR)
        return today != preferences.getInt(Key.LAST_AD_DATE.name, -1)
    }

    fun setUseNotificationWidget(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_NOTIFICATION_WIDGET.name, newState).apply()
    }

    fun useNotificationWidget(): Boolean =
            preferences.getBoolean(Key.USE_NOTIFICATION_WIDGET.name, false)

    fun setRetainTabs(newState: Boolean) {
        preferences.edit().putBoolean(Key.RETAIN_TABS.name, newState).apply()
    }

    fun doesRetainTabs(): Boolean = preferences.getBoolean(Key.RETAIN_TABS.name, true)

    fun setUseJavaScript(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_JS.name, newState).apply()
    }

    fun useJavaScript(): Boolean = preferences.getBoolean(Key.USE_JS.name, true)

    fun setMenuPos(newState: MenuPos) {
        preferences.edit().putString(Key.MENU_POS.name, newState.name).apply()
    }

    fun menuPosId(): Int =
            MenuPos.valueOf(preferences.getString(Key.MENU_POS.name, MenuPos.RIGHT.name)).id

    fun menuPos(): MenuPos =
            MenuPos.valueOf(preferences.getString(Key.MENU_POS.name, MenuPos.RIGHT.name))

    fun setLoadImage(newState: Boolean) {
        preferences.edit().putBoolean(Key.LOAD_IMAGE.name, newState).apply()
    }

    fun doesLoadImage(): Boolean = preferences.getBoolean(Key.LOAD_IMAGE.name, true)

    fun setSaveForm(newState: Boolean) {
        preferences.edit().putBoolean(Key.SAVE_FORM.name, newState).apply()
    }

    fun doesSaveForm(): Boolean = preferences.getBoolean(Key.SAVE_FORM.name, false)

    fun setUserAgent(path: String) {
        preferences.edit().putString(Key.USER_AGENT.name, path).apply()
    }

    fun userAgent(): String = preferences.getString(Key.USER_AGENT.name, "DEFAULT")

    var homeUrl: String
        get() = preferences.getString(Key.HOME_URL.name,
                if (Locale.getDefault().language == Locale.ENGLISH.language) {
                    "https://www.yahoo.com"
                } else {
                    "https://m.yahoo.co.jp"
                }
        )
        set(path) {
            if (Urls.isInvalidUrl(path)) {
                return
            }
            preferences.edit().putString(Key.HOME_URL.name, path).apply()
        }

    fun setUseColorFilter(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_COLOR_FILTER.name, newState).apply()
    }

    fun useColorFilter(): Boolean = preferences.getBoolean(Key.USE_COLOR_FILTER.name, false)

    fun setFilterColor(@ColorInt newState: Int) {
        preferences.edit().putInt(Key.FILTER_COLOR.name, newState).apply()
    }

    @ColorInt fun filterColor(): Int =
            preferences.getInt(
                    Key.FILTER_COLOR.name,
                    ContextCompat.getColor(context, R.color.default_color_filter)
            )

    fun setDefaultSearchEngine(category: String) {
        preferences.edit().putString(Key.DEFAULT_SEARCH_ENGINE.name, category).apply()
    }

    fun getDefaultSearchEngine(): String {
        return preferences.getString(
                Key.DEFAULT_SEARCH_ENGINE.name,
                SearchCategory.getDefaultCategoryName()
        )
    }

    var enableSearchQueryExtract: Boolean
        get () = preferences.getBoolean(Key.ENABLE_SEARCH_WITH_CLIP.name, true)
        set (newState) {
            preferences.edit().putBoolean(Key.ENABLE_SEARCH_WITH_CLIP.name, newState).apply()
        }

    var enableSearchWithClip: Boolean
        get () = preferences.getBoolean(Key.ENABLE_SEARCH_QUERY_EXTRACT.name, true)
        set (newState) {
            preferences.edit().putBoolean(Key.ENABLE_SEARCH_QUERY_EXTRACT.name, newState).apply()
        }

    var startUp: StartUp
        get () = StartUp.findByName(preferences.getString(Key.START_UP.name, ""))
        set (newValue) = preferences.edit().putString(Key.START_UP.name, newValue.name).apply()

    var saveViewHistory: Boolean
        get () = preferences.getBoolean(Key.SAVE_VIEW_HISTORY.name, true)
        set (newState) = preferences.edit().putBoolean(Key.SAVE_VIEW_HISTORY.name, newState).apply()

    var fullScreen: Boolean
        get () = preferences.getBoolean(Key.FULL_SCREEN.name, false)
        set (newState) {
            preferences.edit().putBoolean(Key.FULL_SCREEN.name, newState).apply()
        }

    internal fun setBrowserScreenMode(newState: ScreenMode) {
        preferences.edit().putString(Key.SCREEN_MODE.name, newState.name).apply()
    }

    internal fun browserScreenMode(): ScreenMode =
            ScreenMode.valueOf(preferences.getString(Key.SCREEN_MODE.name, ScreenMode.EXPANDABLE.name))

    var useInversion: Boolean
        get () = preferences.getBoolean(Key.USE_INVERSION.name, false)
        set (newValue) = preferences.edit().putBoolean(Key.USE_INVERSION.name, newValue).apply()

    var wifiOnly: Boolean
        get () = preferences.getBoolean(Key.WIFI_ONLY_MODE.name, true)
        set (newValue) = preferences.edit().putBoolean(Key.WIFI_ONLY_MODE.name, newValue).apply()

    var adRemove: Boolean
        get () = preferences.getBoolean(Key.AD_REMOVE.name, true)
        set (newValue) = preferences.edit().putBoolean(Key.AD_REMOVE.name, newValue).apply()

    var poolSize: Int
        get () = preferences.getInt(Key.WEB_VIEW_POOL_SIZE.name, 6)
        set (newValue) = preferences.edit().putInt(Key.WEB_VIEW_POOL_SIZE.name, newValue).apply()

    fun setEditorBackgroundColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_BACKGROUND_COLOR.name, newValue).apply()
    }

    fun editorBackgroundColor(): Int {
        return preferences.getInt(Key.EDITOR_BACKGROUND_COLOR.name, Color.WHITE)
    }

    fun setEditorFontColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_FONT_COLOR.name, newValue).apply()
    }

    fun editorFontColor(): Int {
        return preferences.getInt(Key.EDITOR_FONT_COLOR.name, Color.BLACK)
    }

    fun setEditorCursorColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_CURSOR_COLOR.name, newValue).apply()
    }

    @ColorInt
    fun editorCursorColor(): Int {
        return preferences.getInt(
                Key.EDITOR_CURSOR_COLOR.name,
                ContextCompat.getColor(context, R.color.colorAccent)
        )
    }

    fun setEditorHighlightColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_HIGHLIGHT_COLOR.name, newValue).apply()
    }

    @ColorInt
    fun editorHighlightColor(): Int {
        return preferences.getInt(
                Key.EDITOR_HIGHLIGHT_COLOR.name,
                ContextCompat.getColor(context, R.color.light_blue_200_dd)
        )
    }

    fun setEditorFontSize(newSize: Int) {
        preferences.edit().putInt(Key.EDITOR_FONT_SIZE.name, newSize).apply()
    }

    fun editorFontSize(): Int {
        return preferences.getInt(Key.EDITOR_FONT_SIZE.name, 16)
    }

    fun setNewCameraFabPosition(x: Float, y: Float) {
        preferences.edit()
                .putFloat(Key.CAMERA_FAB_BUTTON_POSITION_X.name, x)
                .putFloat(Key.CAMERA_FAB_BUTTON_POSITION_Y.name, y)
                .apply()
    }

    fun clearCameraFabPosition() {
        preferences.edit()
                .remove(Key.CAMERA_FAB_BUTTON_POSITION_X.name)
                .remove(Key.CAMERA_FAB_BUTTON_POSITION_Y.name)
                .apply()
    }

    fun cameraFabPosition(): Pair<Float, Float>? {
        if (!preferences.contains(Key.CAMERA_FAB_BUTTON_POSITION_X.name)
                || !preferences.contains(Key.CAMERA_FAB_BUTTON_POSITION_Y.name)) {
            return null
        }
        return preferences.getFloat(Key.CAMERA_FAB_BUTTON_POSITION_X.name, -1f) to
                preferences.getFloat(Key.CAMERA_FAB_BUTTON_POSITION_Y.name, -1f)
    }


    fun setNewMenuFabPosition(x: Float, y: Float) {
        preferences.edit()
                .putFloat(Key.MENU_FAB_BUTTON_POSITION_X.name, max(0f, x))
                .putFloat(Key.MENU_FAB_BUTTON_POSITION_Y.name, max(0f, y))
                .apply()
    }

    fun clearMenuFabPosition() {
        preferences.edit()
                .remove(Key.MENU_FAB_BUTTON_POSITION_X.name)
                .remove(Key.MENU_FAB_BUTTON_POSITION_Y.name)
                .apply()
    }

    fun menuFabPosition(): Pair<Float, Float>? {
        if (!preferences.contains(Key.MENU_FAB_BUTTON_POSITION_X.name)
                || !preferences.contains(Key.MENU_FAB_BUTTON_POSITION_Y.name)) {
            return null
        }
        return preferences.getFloat(Key.MENU_FAB_BUTTON_POSITION_X.name, -1f) to
                preferences.getFloat(Key.MENU_FAB_BUTTON_POSITION_Y.name, -1f)
    }

    fun setWebViewBackgroundAlpha(alpha: Float) {
        preferences.edit().putFloat(Key.WEB_VIEW_BACKGROUND_ALPHA.name, alpha).apply()
    }

    fun getWebViewBackgroundAlpha(): Float {
        return preferences.getFloat(Key.WEB_VIEW_BACKGROUND_ALPHA.name, 0f)
    }

    fun readRssReaderTargets(): MutableSet<String> {
        return preferences.getStringSet(Key.RSS_READER_TARGETS.name, mutableSetOf())
                ?: mutableSetOf()
    }

    fun saveNewRssReaderTargets(url: String) {
        val targets = readRssReaderTargets()
        targets.add(url)
        preferences.edit().putStringSet(Key.RSS_READER_TARGETS.name, targets).apply()
    }

    fun removeFromRssReaderTargets(url: String) {
        val targets = readRssReaderTargets()
        targets.remove(url)
        preferences.edit().putStringSet(Key.RSS_READER_TARGETS.name, targets).apply()
    }

    fun containsRssTarget(url: String) = readRssReaderTargets().contains(url)

    fun clear() {
        preferences.edit().clear().apply()
    }

}
