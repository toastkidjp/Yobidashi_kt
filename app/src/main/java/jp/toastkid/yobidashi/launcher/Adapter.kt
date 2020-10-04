package jp.toastkid.yobidashi.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemAppLauncherBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayList

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val context: Context,
        private val contentViewModel: ContentViewModel?
)
    : RecyclerView.Adapter<ViewHolder>() {

    /**
     * Master list.
     */
    private val master: List<ApplicationInfo>

    /**
     * Current list.
     */
    private val installedApps: MutableList<ApplicationInfo>

    /**
     * Preferences wrapper.
     */
    private val preferenceApplier: PreferenceApplier

    /**
     * Package manager.
     */
    private val packageManager: PackageManager = context.packageManager

    private val layoutInflater = LayoutInflater.from(context)

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    init {
        master = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        installedApps = ArrayList(master)
        preferenceApplier = PreferenceApplier(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = DataBindingUtil.inflate<ItemAppLauncherBinding>(
                layoutInflater,
                R.layout.item_app_launcher,
                parent,
                false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = installedApps[position]
        holder.setImage(info.loadIcon(packageManager))
        holder.setTitle(info.loadLabel(packageManager).toString())
        holder.setTargetSdk(info.targetSdkVersion)
        holder.setPackageName(info.packageName)
        try {
            val packageInfo = packageManager.getPackageInfo(info.packageName, PackageManager.GET_META_DATA)
            holder.setVersionInformation("${packageInfo.versionName} (${extractVersionCode(packageInfo)})")
            holder.setInstalledMs(packageInfo.firstInstallTime)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }

        setOnClick(holder, info)
    }

    private fun extractVersionCode(packageInfo: PackageInfo) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

    /**
     * Set on click actions.
     *
     * @param holder [ViewHolder]
     * @param info [ApplicationInfo]
     */
    private fun setOnClick(holder: ViewHolder, info: ApplicationInfo) {
        val intent = packageManager.getLaunchIntentForPackage(info.packageName)
        if (intent == null) {
            holder.itemView.setOnClickListener { snackCannotLaunch() }
            return
        }
        holder.itemView.setOnClickListener {
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
                snackCannotLaunch()
            }
        }
    }

    /**
     * Show cannot launch message with snackbar.
     */
    private fun snackCannotLaunch() {
        contentViewModel?.snackShort(R.string.message_failed_launching)
    }

    /**
     * Do filtering with text.
     *
     * @param str filter query
     */
    fun filter(str: String, limit: Int = -1, onResult: () -> Unit = {}) {
        installedApps.clear()
        if (str.isEmpty()) {
            installedApps.addAll(master)
            notifyDataSetChanged()
            return
        }

        CoroutineScope(Dispatchers.Main).launch(disposables) {
            master.asFlow()
                    .filter { appInfo -> appInfo.packageName.contains(str) }
                    .take(if (limit == -1) Int.MAX_VALUE else limit)
                    .collect {
                        installedApps.add(it)
                    }
            onResult()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = installedApps.size

    /**
     * Dispose disposables.
     */
    fun dispose() {
        disposables.cancel()
    }
}
