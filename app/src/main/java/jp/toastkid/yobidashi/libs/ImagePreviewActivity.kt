package jp.toastkid.yobidashi.libs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.MenuItem
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityImagePreviewBinding
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import java.io.File

/**
 * @author toastkidjp
 */
class ImagePreviewActivity(): BaseActivity() {

    var path: String? = null

    var imagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0)

        if (!intent.hasExtra(EXTRA_KEY_IMAGE_PATH)) {
            Toaster.tShort(this, R.string.message_illegal_image_path)
            finish()
            return
        }

        setContentView(LAYOUT_ID)
        val binding: ActivityImagePreviewBinding =
                DataBindingUtil.setContentView(this, LAYOUT_ID)
        imagePath = intent.getStringExtra(EXTRA_KEY_IMAGE_PATH)
        ImageLoader.setImageToImageView(binding.image, imagePath)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.activity_image_preview)
        binding.toolbar.setOnMenuItemClickListener{ this.clickMenu(it) }
    }

    override fun clickMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> {
                finish()
            }
            R.id.save  -> {
                startActivityForResult(
                        IntentFactory.makeDocumentOnStorage(STORE_FILE_TYPE, DEFAULT_STORE_FILE_NAME),
                        1
                )
            }
        }
        return super.clickMenu(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        intent?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val takeFlags: Int = intent.getFlags() and Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(intent.data, takeFlags)
            }
            ImageLoader.loadBitmap(this, Uri.parse(File(imagePath).toURI().toString()))
                ?.compress(Bitmap.CompressFormat.PNG, 100, contentResolver.openOutputStream(it.data))
        }
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

        private const val STORE_FILE_TYPE: String = "image/png"

        private const val DEFAULT_STORE_FILE_NAME: String = "image.png"

        fun makeIntent(context: Context, file: File): Intent =
                Intent(context, ImagePreviewActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(EXTRA_KEY_IMAGE_PATH, file.absolutePath)
                }
    }
}