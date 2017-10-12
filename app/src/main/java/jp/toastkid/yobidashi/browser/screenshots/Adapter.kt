package jp.toastkid.yobidashi.browser.screenshots

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.content.FileProvider
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.Storeroom

/**
 * Screenshot's adapter.
 * TODO clean up code.

 * @author toastkidjp
 */
internal class Adapter
/**
 * Initialize with [Context].
 * @param context
 */
(context: Context, private val onClick: (Bitmap) -> Unit) : RecyclerView.Adapter<ViewHolder>() {

    /** Files dir wrapper.  */
    private val screenshots: Storeroom

    /** Layout inflater.  */
    private val inflater: LayoutInflater

    /** For using snackbar.  */
    private val colorPair: ColorPair

    init {
        screenshots = Storeroom(context, Screenshot.DIR)
        inflater = LayoutInflater.from(context)
        colorPair = PreferenceApplier(context).colorPair()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(inflater, R.layout.item_screenshot, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screenshotFile = screenshots.get(position)!!
        val bitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        holder.setImage(bitmap)
        holder.setIconColor(colorPair.bgColor())
        holder.setDeleteAction {
            if (screenshots.remove(position)) {
                notifyItemRemoved(position)
                Toaster.snackShort(holder.itemView, R.string.done_clear, colorPair)
            }
        }
        holder.setShareAction { launchShareImage(holder, bitmap) }
        holder.setTimestamp(screenshotFile.lastModified())
        holder.itemView.setOnClickListener { onClick(bitmap) }
    }

    /**
     * Launch sharing screenshot activity.
     *
     * @param holder
     * @param bitmap
     */
    private fun launchShareImage(holder: ViewHolder, bitmap: Bitmap) {
        holder.itemView.context.startActivity(
                IntentFactory.shareImage(
                        FileProvider.getUriForFile(
                                holder.itemView.context,
                                BuildConfig.APPLICATION_ID + ".fileprovider",
                                ImageCache.saveBitmap(holder.itemView.context, bitmap).absoluteFile
                        )
                )
        )
    }

    override fun getItemCount(): Int {
        return screenshots.count
    }
}
