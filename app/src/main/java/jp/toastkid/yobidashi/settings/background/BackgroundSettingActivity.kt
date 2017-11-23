package jp.toastkid.yobidashi.settings.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBackgroundSettingBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.storage.FilesDir

/**
 * Background settings.
 *
 * @author toastkidjp
 */
class BackgroundSettingActivity : BaseActivity() {

    /**
     * Data Binding object.
     */
    private var binding: ActivityBackgroundSettingBinding? = null

    /**
     * ModuleAdapter.
     */
    private var adapter: Adapter? = null

    /**
     * Wrapper of FilesDir.
     */
    private var filesDir: FilesDir? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityBackgroundSettingBinding>(this, LAYOUT_ID)
        binding?.let {
            it.activity = this
            initToolbar(it.toolbar)
            it.toolbar.inflateMenu(R.menu.background_setting_menu)
        }

        filesDir = FilesDir(this, BACKGROUND_DIR)

        initImagesView()
    }

    /**
     * Initialize images RecyclerView.
     */
    private fun initImagesView() {
        binding?.let {
            it.imagesView.layoutManager = GridLayoutManager(
                    this,
                    2,
                    LinearLayoutManager.HORIZONTAL,
                    false
            )
            adapter = Adapter(preferenceApplier, filesDir!!)
            it.imagesView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        applyColorToToolbar(binding!!.toolbar)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.background_settings_toolbar_menu_add -> {
                launchAdding(binding!!.fab)
                return true
            }
            R.id.background_settings_toolbar_menu_clear -> {
                clearImages()
                return true
            }
            else -> super.clickMenu(item)
        }
    }

    /**
     * Launch Adding action.
     *
     * @param ignored For use Data Binding.
     */
    fun launchAdding(ignored: View) {
        sendLog("set_bg_img")
        startActivityForResult(IntentFactory.makePickImage(), IMAGE_READ_REQUEST)
    }

    /**
     * Clear all images.
     */
    private fun clearImages() {
        ClearImages(this, {
            sendLog("clear_bg_img")
            filesDir!!.clean()
            Toaster.snackShort(
                    binding!!.fabParent,
                    getString(R.string.message_success_image_removal),
                    colorPair()
            )
            adapter!!.notifyDataSetChanged()
        }).invoke()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {

        if (requestCode == IMAGE_READ_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null) {
            LoadedAction(data, binding!!.fabParent, colorPair(), { adapter?.notifyDataSetChanged() })
                    .invoke()
            sendLog("set_img")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @StringRes
    override fun titleId(): Int = R.string.title_background_image_setting

    companion object {

        /**
         * Background image dir.
         */
        const val BACKGROUND_DIR: String = "background_dir"

        /**
         * Request code.
         */
        private const val IMAGE_READ_REQUEST: Int = 136

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_background_setting

        /**
         * Make launcher intent.
         * @param context Context
         *
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent =
                Intent(context, BackgroundSettingActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

}
