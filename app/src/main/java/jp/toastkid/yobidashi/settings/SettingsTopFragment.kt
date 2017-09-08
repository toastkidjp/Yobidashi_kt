package jp.toastkid.yobidashi.settings

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.browser.MenuPos
import jp.toastkid.yobidashi.browser.UserAgent
import jp.toastkid.yobidashi.calendar.alarm.DailyAlarm
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.FragmentSettingsBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.main.StartUpSpinnerInitializer
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
import jp.toastkid.yobidashi.search.SearchCategory
import jp.toastkid.yobidashi.search.SearchCategorySpinnerInitializer
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import jp.toastkid.yobidashi.settings.color.ColorSettingActivity

/**
 * Settings top fragment.

 * @author toastkidjp
 */
class SettingsTopFragment : BaseFragment() {

    /** Data binding object.  */
    private lateinit var binding: FragmentSettingsBinding

    /** Color filter.  */
    private var colorFilter: ColorFilter? = null

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil
                .inflate<FragmentSettingsBinding>(inflater!!, LAYOUT_ID, container, false)
        binding.fragment = this
        initMenuPos()
        TextInputs.setEmptyAlert(binding.homeInputLayout)
        SearchCategorySpinnerInitializer.initialize(binding.searchCategories as Spinner)
        binding.searchCategories.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferenceApplier().setDefaultSearchEngine(
                        SearchCategory.values()[binding.searchCategories.selectedItemPosition].name)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // NOP
            }
        }
        StartUpSpinnerInitializer.initialize(binding.startUpItems)
        binding.startUpItems.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferenceApplier().startUp =
                        StartUp.values()[binding.startUpItems.selectedItemPosition]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // NOP
            }
        }
        return binding.root
    }

    private fun initMenuPos() {
        binding.menuPosRadio.setOnCheckedChangeListener ({ group, checkedId ->
            when (group.checkedRadioButtonId) {
                R.id.menu_pos_left  -> preferenceApplier().setMenuPos(MenuPos.LEFT)
                R.id.menu_pos_right -> preferenceApplier().setMenuPos(MenuPos.RIGHT)
            }
        })
        binding.menuPosRadio.check(preferenceApplier().menuPos().id())
    }

    override fun onResume() {
        super.onResume()

        setCurrentValues()
    }

    private fun setCurrentValues() {
        val preferenceApplier = preferenceApplier()
        binding.useInternalBrowserCheck.isChecked = preferenceApplier.useInternalBrowser()
        binding.retainTabsCheck.isChecked = preferenceApplier.doesRetainTabs()
        binding.useNotificationWidgetCheck.isChecked = preferenceApplier.useNotificationWidget()
        binding.useDailyAlarmCheck.isChecked = preferenceApplier.doesUseDailyAlarm()
        Colors.setBgAndText(binding.homeButton, colorPair())
        binding.homeInputLayout.editText!!.setText(preferenceApplier.homeUrl)
        binding.browserJsCheck.isChecked = preferenceApplier.useJavaScript()
        binding.useImageCheck.isChecked = preferenceApplier.doesLoadImage()
        binding.saveFormCheck.isChecked = preferenceApplier.doesSaveForm()
        binding.userAgentValue.text = UserAgent.valueOf(preferenceApplier.userAgent()).title()
        binding.useColorFilterCheck.isChecked = preferenceApplier.useColorFilter()
        binding.enableSearchWithClipCheck.isChecked = preferenceApplier.enableSearchWithClip
        binding.saveViewHistoryCheck.isChecked = preferenceApplier.saveViewHistory
    }

    /**
     * Call color setting.
     * @param view
     */
    fun colorSettings(view: View) {
        sendLog("nav_color")
        startActivity(ColorSettingActivity.makeIntent(activity))
    }

    /**
     * Call background setting.

     * @param view
     */
    fun backgroundSettings(view: View) {
        sendLog("nav_bg_set")
        startActivity(BackgroundSettingActivity.makeIntent(activity))
    }

    /**
     * Clear background setting.

     * @param view
     */
    fun clearBackgroundSettings(view: View) {
        sendLog("nav_bg_reset")
        preferenceApplier().removeBackgroundImagePath()
        Toaster.snackShort(
                binding.root,
                R.string.message_reset_bg_image,
                preferenceApplier().colorPair()
        )
    }

    /**
     * Open search categories spinner.
     */
    fun openSearchCategory(v: View) {
        binding.searchCategories.performClick()
    }

    /**
     * Open startup spinner.
     */
    fun openStartup(v: View) {
        binding.startUpItems.performClick()
    }

    /**
     * Switch notification widget displaying.
     *
     * @param v
     */
    fun switchSearchWithClip(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.enableSearchWithClip
        preferenceApplier.enableSearchWithClip = newState
        binding.enableSearchWithClipCheck.isChecked = newState

        @StringRes val messageId
                = if (newState) { R.string.message_enable_swc } else { R.string.message_disable_swc }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * UserAgent setting.
     * @param v
     */
    fun userAgent(v: View) {
        UserAgent.showSelectionDialog(
                binding.root,
                { userAgent -> binding.userAgentValue.text = userAgent.title() }
        )
    }

    /**
     * Switch notification widget displaying.

     * @param v
     */
    fun switchNotificationWidget(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useNotificationWidget()
        preferenceApplier.setUseNotificationWidget(newState)
        binding.useNotificationWidgetCheck.isChecked = newState

        @StringRes var messageId = R.string.message_done_showing_notification_widget
        if (newState) {
            NotificationWidget.show(context)
        } else {
            NotificationWidget.hide(context)
            messageId = R.string.message_remove_notification_widget
        }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch browser.

     * @param v
     */
    fun switchInternalBrowser(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useInternalBrowser()
        preferenceApplier.setUseInternalBrowser(newState)
        binding.useInternalBrowserCheck.isChecked = newState
        @StringRes val messageId
                = if (newState) { R.string.message_use_internal_browser }
                  else { R.string.message_use_chrome }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch retaining tabs.

     * @param v
     */
    fun switchRetainTabs(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesRetainTabs()
        preferenceApplier.setRetainTabs(newState)
        binding.retainTabsCheck.isChecked = newState
        @StringRes val messageId = if (newState)
            R.string.message_check_retain_tabs
        else
            R.string.message_check_doesnot_retain_tabs
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /***
     * Commit input.
     * @param view
     */
    fun commitHomeInput(view: View) {
        val input = binding.homeInputLayout.editText!!.text.toString()
        if (TextUtils.isEmpty(input)) {
            Toaster.snackShort(
                    binding.root,
                    R.string.favorite_search_addition_dialog_empty_message,
                    colorPair()
            )
            return
        }
        if (Urls.isInvalidUrl(input)) {
            Toaster.snackShort(binding.root, R.string.message_invalid_url, colorPair())
            return
        }
        preferenceApplier().homeUrl = input

        Toaster.snackShort(
                binding.root,
                getString(R.string.message_commit_home, input),
                colorPair()
        )
    }

    /**
     * Switch JavaScript enabling.
     * @param v
     */
    fun switchJsEnabled(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useJavaScript()
        preferenceApplier.setUseJavaScript(newState)
        binding.browserJsCheck.isChecked = newState
        @StringRes val messageId = if (newState)
            R.string.message_js_enabled
        else
            R.string.message_js_disabled
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch loading images.

     * @param v
     */
    fun switchLoadingImage(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesLoadImage()
        preferenceApplier.setLoadImage(newState)
        binding.useImageCheck.isChecked = newState
    }

    /**
     * Switching saving form data.

     * @param v
     */
    fun switchSaveFormData(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesSaveForm()
        preferenceApplier.setSaveForm(newState)
        binding.saveFormCheck.isChecked = newState
    }

    /**
     * Switch saving view history.

     * @param v
     */
    fun switchViewHistory(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.saveViewHistory
        preferenceApplier.saveViewHistory = newState
        binding.saveViewHistoryCheck.isChecked = newState
    }

    /**
     * Switching daily alarm.

     * @param v
     */
    fun switchDailyAlarm(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesUseDailyAlarm()
        binding.useDailyAlarmCheck.isChecked = newState

        val dailyAlarm = DailyAlarm(context)
        val useDailyAlarm = preferenceApplier.doesUseDailyAlarm()
        if (useDailyAlarm) {
            dailyAlarm.reset()
        }

        if (newState) {
            preferenceApplier.useDailyAlarm()
            dailyAlarm.reset()
            Toaster.snackShort(
                    binding.root,
                    R.string.message_set_daily_alarm,
                    preferenceApplier.colorPair()
            )
            sendLog("nav_daily_set")
        } else {
            preferenceApplier.notUseDailyAlarm()
            dailyAlarm.cancel()
            Toaster.snackShort(
                    binding.root,
                    R.string.message_clear_daily_alarm,
                    preferenceApplier.colorPair()
            )
            sendLog("nav_daily_cancel")
        }
    }

    /**
     * Call device settings.

     * @param v
     */
    fun deviceSetting(v: View) {
        sendLog("nav_dvc_set")
        startActivity(SettingsIntentFactory.makeLaunch())
    }

    /**
     * Call Wi-Fi settings.

     * @param v
     */
    fun wifi(v: View) {
        sendLog("nav_wifi_set")
        startActivity(SettingsIntentFactory.wifi())
    }

    /**
     * Call Wireless settings.

     * @param v
     */
    fun wireless(v: View) {
        sendLog("nav_wrls_set")
        startActivity(SettingsIntentFactory.wireless())
    }

    /**
     * Call Date-and-Time settings.

     * @param v
     */
    fun dateAndTime(v: View) {
        sendLog("nav_dat")
        startActivity(SettingsIntentFactory.dateAndTime())
    }

    /**
     * Call all app settings.

     * @param v
     */
    fun allApps(v: View) {
        sendLog("nav_allapps_set")
        startActivity(SettingsIntentFactory.allApps())
    }

    override fun titleId(): Int {
        return R.string.title_settings
    }

    /**
     * Show all menu module.
     */
    fun showAll() {
        binding.displayingModule.visibility = View.VISIBLE
        binding.searchModule.visibility = View.VISIBLE
        binding.browserModule.visibility = View.VISIBLE
        binding.notificationsModule.visibility = View.VISIBLE
        binding.others.visibility = View.VISIBLE
    }

    /**
     * Show displaying menu module.
     */
    fun showDisplay() {
        binding.displayingModule.visibility = View.VISIBLE
        binding.searchModule.visibility = View.GONE
        binding.browserModule.visibility = View.GONE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.GONE
    }

    /**
     * Show displaying menu module.
     */
    fun showSearch() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.VISIBLE
        binding.browserModule.visibility = View.GONE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.GONE
    }

    /**
     * Show browser menu module.
     */
    fun showBrowser() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.GONE
        binding.browserModule.visibility = View.VISIBLE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.GONE
    }

    /**
     * Show notification menu module.
     */
    fun showNotifications() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.GONE
        binding.browserModule.visibility = View.GONE
        binding.notificationsModule.visibility = View.VISIBLE
        binding.others.visibility = View.GONE
    }

    /**
     * Show other menu module.
     */
    fun showOthers() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.GONE
        binding.browserModule.visibility = View.GONE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.VISIBLE
    }

    /**
     * Switch color filter's visibility.

     * @param v
     */
    fun switchColorFilter(v: View) {

        initColorFilterIfNeed()

        binding.useColorFilterCheck.isChecked = colorFilter!!.switchState(this, REQUEST_OVERLAY_PERMISSION)
    }

    private fun initColorFilterIfNeed() {
        if (colorFilter == null) {
            colorFilter = ColorFilter(activity, binding.root)
        }
    }

    /**
     * Clear all settings.

     * @param v
     */
    fun clearSettings(v: View) {
        sendLog("nav_clr_set")
        val preferenceApplier = preferenceApplier()
        AlertDialog.Builder(activity)
                .setTitle(R.string.title_clear_settings)
                .setMessage(Html.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, i ->
                    preferenceApplier.clear()
                    initColorFilterIfNeed()
                    colorFilter!!.stop()
                    setCurrentValues()
                    Updater.update(activity)
                    Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair())
                }
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                Toaster.snackShort(
                        binding.root,
                        R.string.message_cannot_draw_overlay,
                        colorPair()
                )
                return
            }
            colorFilter!!.start()
        }
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.fragment_settings

        /** Request code of overlay permission.  */
        private val REQUEST_OVERLAY_PERMISSION = 1
    }
}
