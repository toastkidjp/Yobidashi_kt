package jp.toastkid.yobidashi.browser.archive

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.functions.Consumer
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemArchiveBinding
import jp.toastkid.yobidashi.libs.storage.Storeroom
import timber.log.Timber
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Initialize with Context.
 * @param context
 * @author toastkidjp
 */
internal class Adapter(
        context: Context,
        /** Return read content.  */
        private val callback: Consumer<String>
) : RecyclerView.Adapter<ViewHolder>() {

    /** Archive folder wrapper.  */
    private val archiveDir: Storeroom

    /** Layout inflater.  */
    private val layoutInflater: LayoutInflater

    /** Data binding object.  */
    private var binding: ItemArchiveBinding? = null

    init {
        this.archiveDir = Archive.makeNew(context)
        this.layoutInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate<ItemArchiveBinding>(layoutInflater, R.layout.item_archive, parent, false)
        return ViewHolder(binding as ItemArchiveBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = archiveDir.get(position) ?: return
        holder.setText(file.name)
        holder.setSubText(convertLastModified(file.lastModified())
                + " / " + convertKb(file.length()) + "[KB]")
        holder.itemView.setOnClickListener { v ->
            try {
                callback.accept(file.absolutePath)
            } catch (e: IOException) {
                Timber.e(e)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        holder.setDelete(View.OnClickListener{ view ->
            file.delete()
            notifyItemRemoved(position)
        })
    }

    private fun convertLastModified(lastModifiedMs: Long): String {
        return SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault())
                .format(Date(lastModifiedMs))
    }

    private fun convertKb(length: Long): String {
        return NumberFormat.getIntegerInstance(Locale.getDefault()).format(length / 1024)
    }

    override fun getItemCount(): Int {
        return archiveDir.count
    }
}
