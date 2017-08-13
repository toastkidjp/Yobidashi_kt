package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View

import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBackgroundSettingBinding
import jp.toastkid.yobidashi.libs.storage.Storeroom
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory

/**
 * Background settings.

 * @author toastkidjp
 */
class BackgroundSettingActivity : BaseActivity() {

    /** Data Binding object.  */
    private var binding: ActivityBackgroundSettingBinding? = null

    /** Adapter.  */
    private var adapter: Adapter? = null

    /** Wrapper of FilesDir.  */
    private var storeroom: Storeroom? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityBackgroundSettingBinding>(this, LAYOUT_ID)
        binding!!.activity = this
        initToolbar(binding!!.toolbar)
        binding!!.toolbar.inflateMenu(R.menu.background_setting_menu)

        storeroom = Storeroom(this, BACKGROUND_DIR)

        initImagesView()
    }

    private fun initImagesView() {
        binding!!.imagesView.layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)
        adapter = Adapter(preferenceApplier, storeroom)
        binding!!.imagesView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        applyColorToToolbar(binding!!.toolbar)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.background_settings_toolbar_menu_add) {
            launchAdding(binding!!.fab)
            return true
        }
        if (itemId == R.id.background_settings_toolbar_menu_clear) {
            clearImages()
            return true
        }
        return super.clickMenu(item)
    }

    fun launchAdding(v: View) {
        sendLog("set_bg_img")
        startActivityForResult(IntentFactory.makePickImage(), IMAGE_READ_REQUEST)
    }

    private fun clearImages() {
        ClearImages(this) {
            sendLog("clear_bg_img")
            storeroom!!.clean()
            Toaster.snackShort(
                    binding!!.fabParent,
                    getString(R.string.message_success_image_removal),
                    colorPair()
            )
            adapter!!.notifyDataSetChanged()
        }.invoke()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent
    ) {

        if (requestCode == IMAGE_READ_REQUEST && resultCode == Activity.RESULT_OK) {
            LoadedAction(data, binding!!.fabParent, colorPair(), Runnable { adapter!!.notifyDataSetChanged() })
                    .invoke()
            sendLog("set_img")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun titleId(): Int {
        return R.string.title_background_image_setting
    }

    companion object {

        /** Background image dir.  */
        val BACKGROUND_DIR = "background_dir"

        /** Request code.  */
        private val IMAGE_READ_REQUEST = 136

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_background_setting

        /**
         * Make launcher intent.
         * @param context Context
         * *
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BackgroundSettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }

}
