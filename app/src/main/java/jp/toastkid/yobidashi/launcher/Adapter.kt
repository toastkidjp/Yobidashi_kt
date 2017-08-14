package jp.toastkid.yobidashi.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppLauncherItemBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
internal class Adapter(private val context: Context, private val parent: View) : RecyclerView.Adapter<ViewHolder>() {

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
            e.printStackTrace()
        }

        val intent = packageManager.getLaunchIntentForPackage(info.packageName)
        if (intent == null) {
            holder.itemView.setOnClickListener { v ->
                Toaster.snackShort(
                        parent,
                        R.string.message_failed_launching,
                        preferenceApplier.colorPair()
                )
            }
        } else {
            holder.itemView.setOnClickListener { v -> context.startActivity(intent) }
        }
    }

    fun filter(str: String) {
        installedApps.clear()
        if (TextUtils.isEmpty(str)) {
            installedApps.addAll(master)
            notifyDataSetChanged()
            return
        }
        Observable.fromIterable(master)
                .filter { appInfo -> appInfo.packageName.contains(str) }
                .subscribeOn(Schedulers.trampoline())
                .doOnComplete { this.notifyDataSetChanged() }
                .subscribe { installedApps.add(it) }
    }

    override fun getItemCount(): Int {
        return installedApps.size
    }
}
