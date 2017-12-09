package jp.toastkid.yobidashi.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppLauncherItemBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber
import java.util.*

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter(private val context: Context, private val parent: View)
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

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        this.master = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        this.installedApps = ArrayList(master)
        this.preferenceApplier = PreferenceApplier(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = DataBindingUtil.inflate<AppLauncherItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.app_launcher_item,
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
            holder.setVersionInformation(packageInfo.versionName + "(" + packageInfo.versionCode + ")")
            holder.setInstalledMs(packageInfo.firstInstallTime)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }

        setOnClick(holder, info)
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
        Toaster.snackShort(
                parent,
                R.string.message_failed_launching,
                preferenceApplier.colorPair()
        )
    }

    /**
     * Do filtering with text.
     *
     * @param str filter query
     */
    fun filter(str: String) {
        installedApps.clear()
        if (TextUtils.isEmpty(str)) {
            installedApps.addAll(master)
            notifyDataSetChanged()
            return
        }
        master.toObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .filter { appInfo -> appInfo.packageName.contains(str) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { this.notifyDataSetChanged() }
                .subscribe { installedApps.add(it) }
                .addTo(disposables)
    }

    override fun getItemCount(): Int = installedApps.size

    /**
     * Dispose disposables.
     */
    fun dispose() {
        disposables.clear()
    }
}
