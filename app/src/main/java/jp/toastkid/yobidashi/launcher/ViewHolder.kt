package jp.toastkid.yobidashi.launcher

import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.format.DateFormat

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppLauncherItemBinding

/**
 * View holder.
 *
 * @param binding Binding object
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: AppLauncherItemBinding)
    : RecyclerView.ViewHolder(binding.root) {

    /**
     * Date and Time format.
     */
    private val format: String = binding.appInstalled.context.getString(R.string.date_format)

    /**
     * Set image.
     * @param drawable
     */
    fun setImage(drawable: Drawable) {
        binding.appIcon.setImageDrawable(drawable)
    }

    /**
     * Set title.
     *
     * @param title
     */
    fun setTitle(title: String) {
        binding.appTitle.text = title
    }

    /**
     * Set target SDK information.
     *
     * @param targetSdkVersion
     */
    fun setTargetSdk(targetSdkVersion: Int) {
        binding.appTargetSdk.text = Html.fromHtml("<b>Target SDK</b>: " + targetSdkVersion)
    }

    /**
     * Set package name.
     *
     * @param packageName
     */
    fun setPackageName(packageName: String) {
        binding.appPackageName.text = Html.fromHtml("<b>Package Name</b>: " + packageName)
    }

    /**
     * Set installed ms.
     *
     * @param firstInstallTime
     */
    fun setInstalledMs(firstInstallTime: Long) {
        binding.appInstalled.text= Html.fromHtml(
                "<b>Installed</b>: " + DateFormat.format(format, firstInstallTime))
    }

    /**
     * Set version information.
     *
     * @param versionText
     */
    fun setVersionInformation(versionText: String) {
        binding.appVersion.text = Html.fromHtml("<b>Version</b>: " + versionText)
    }
}
