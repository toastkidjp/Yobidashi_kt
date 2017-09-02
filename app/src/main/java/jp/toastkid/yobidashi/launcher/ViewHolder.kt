package jp.toastkid.yobidashi.launcher

import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.format.DateFormat

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppLauncherItemBinding

/**
 * View holder.

 * @author toastkidjp
 */
internal class ViewHolder
/**

 * @param binding
 */
(
        /** Binding object.  */
        private val binding: AppLauncherItemBinding) : RecyclerView.ViewHolder(binding.root) {

    /** Date and Time format.  */
    private val format: String

    init {
        format = binding.appInstalled.context.getString(R.string.date_format)
    }

    fun setImage(drawable: Drawable) {
        binding.appIcon.setImageDrawable(drawable)
    }

    fun setTitle(text: String) {
        binding.appTitle.text = text
    }

    fun setTargetSdk(targetSdkVersion: Int) {
        binding.appTargetSdk.text = Html.fromHtml("<b>Target SDK</b>: " + targetSdkVersion)
    }

    fun setPackageName(packageName: String) {
        binding.appPackageName.text = Html.fromHtml("<b>Package Name</b>: " + packageName)
    }

    fun setInstalled(firstInstallTime: Long) {
        binding.appInstalled.text = Html.fromHtml("<b>Installed</b>: " + DateFormat.format(format, firstInstallTime))
    }

    fun setVersionInformation(versionText: String) {
        binding.appVersion.text = Html.fromHtml("<b>Version</b>: " + versionText)
    }
}
