package jp.toastkid.yobidashi.browser.archive

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemArchiveBinding
import timber.log.Timber
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

/**
 * Initialize with Context.
 *
 * @param context
 * @param callback Return read content
 *
 * @author toastkidjp
 */
internal class Adapter(
        context: Context,
        private val callback: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /**
     * Archive folder wrapper.
     */
    private val archiveDir: FilesDir = Archive.makeNew(context)

    /**
     * Layout inflater.
     */
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Preference's wrapper.
     */
    private val preferenceApplier = PreferenceApplier(context)

    /**
     * Data binding object.
     */
    private var binding: ItemArchiveBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.item_archive, parent, false)
        return ViewHolder(binding as ItemArchiveBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = archiveDir.get(position) ?: return
        holder.setText(file.name)
        holder.setSubText(
                "${toLastModifiedText(file.lastModified())} / ${toKiloBytes(file.length())}[KB]")
        holder.itemView.setOnClickListener {
            try {
                callback(file.absolutePath)
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
        holder.setDelete({
            file.delete()
            notifyItemRemoved(position)
        })
        holder.setIconColor(preferenceApplier.color)
    }

    /**
     * Convert milliseconds to text.
     *
     * @param lastModifiedMs milliseconds
     */
    private fun toLastModifiedText(lastModifiedMs: Long) =
        DateFormat.format("yyyyMMdd HH:mm:ss", lastModifiedMs)

    /**
     * Convert file byte length to KB text.
     *
     * @param length file byte length
     */
    private fun toKiloBytes(length: Long): String
            = NumberFormat.getIntegerInstance(Locale.getDefault()).format(length / 1024)

    override fun getItemCount(): Int = archiveDir.count

}
