package jp.toastkid.yobidashi.libs

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.LayoutRes
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityImagePreviewBinding
import java.io.File

/**
 * @author toastkidjp
 */
class ImagePreviewActivity(): BaseActivity() {

    var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!intent.hasExtra(EXTRA_KEY_IMAGE_PATH)) {
            Toaster.tShort(this, R.string.message_illegal_image_path)
            finish()
            return
        }

        setContentView(LAYOUT_ID)
        val binding: ActivityImagePreviewBinding =
                DataBindingUtil.setContentView(this, LAYOUT_ID)
        val imagePath = intent.getStringExtra(EXTRA_KEY_IMAGE_PATH)
        ImageLoader.setImageToImageView(binding.image, imagePath)
        binding.close.setOnClickListener { finish() }
        binding.background.setOnClickListener { finish() }
    }

    override fun titleId(): Int = R.string.app_name

    override fun onDestroy() {
        super.onDestroy()
        path?.let { File(path).delete() }
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_image_preview

        private const val EXTRA_KEY_IMAGE_PATH: String = "image_path"

        fun makeIntent(context: Context, file: File): Intent =
                Intent(context, ImagePreviewActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(EXTRA_KEY_IMAGE_PATH, file.absolutePath)
                }
    }
}