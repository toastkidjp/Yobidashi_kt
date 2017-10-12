package jp.toastkid.yobidashi.browser.screenshots

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityScreenshotsBinding
import jp.toastkid.yobidashi.libs.Toaster

/**
 * Screenshots viewer.

 * @author toastkidjp
 */
class ScreenshotsActivity : BaseActivity() {

    /** Data binding object.  */
    private var binding: ActivityScreenshotsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = Adapter(this, { onClickItem(it) })
        if (adapter.itemCount == 0) {
            finish()
            Toaster.tShort(this, R.string.message_empty_screenshots)
            return
        }

        binding = DataBindingUtil.setContentView<ActivityScreenshotsBinding>(this, LAYOUT_ID)
        binding?.screenshotsView?.layoutManager =
                GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        binding?.screenshotsView?.adapter = adapter

        binding?.preview?.setOnClickListener { binding?.preview?.visibility = View.GONE }
        binding?.screenshotsView?.setOnClickListener { finish() }
    }

    private fun onClickItem(bitmap: Bitmap) {
        when (binding?.preview?.visibility) {
            View.VISIBLE -> {
                binding?.preview?.visibility = View.GONE
                binding?.image?.setImageBitmap(null)
            }
            View.GONE -> {
                binding?.image?.setImageBitmap(bitmap)
                binding?.preview?.visibility = View.VISIBLE
            }
        }
    }

    public override fun titleId(): Int {
        return R.string.title_screenshot
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_screenshots

        /**
         * Make launcher intent.

         * @param context
         *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, ScreenshotsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
