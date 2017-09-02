package jp.toastkid.jitte.launcher

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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.AppLauncherItemBinding
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import timber.log.Timber
import java.util.*

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter(private val context: Context, private val parent: View)
    : RecyclerView.Adapter<ViewHolder>() {

    private val master: List<ApplicationInfo>

    private val installedApps: MutableList<ApplicationInfo>

    private val preferenceApplier: PreferenceApplier

    private val packageManager: PackageManager

    init {
        this.packageManager = context.packageManager
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
            holder.setInstalled(packageInfo.firstInstallTime)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }

        val intent = packageManager.getLaunchIntentForPackage(info.packageName)
        if (intent == null) {
            holder.itemView.setOnClickListener { v -> snackCannotLaunch() }
        } else {
            holder.itemView.setOnClickListener { v ->
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                    snackCannotLaunch()
                }
            }
        }
    }

    private fun snackCannotLaunch() {
        Toaster.snackShort(
                parent,
                R.string.message_failed_launching,
                preferenceApplier.colorPair()
        )
    }

    fun filter(str: String) {
        installedApps.clear()
        if (TextUtils.isEmpty(str)) {
            installedApps.addAll(master)
            notifyDataSetChanged()
            return
        }
        Observable.fromIterable(master)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .filter { appInfo -> appInfo.packageName.contains(str) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { this.notifyDataSetChanged() }
                .subscribe { installedApps.add(it) }
    }

    override fun getItemCount(): Int {
        return installedApps.size
    }
}
