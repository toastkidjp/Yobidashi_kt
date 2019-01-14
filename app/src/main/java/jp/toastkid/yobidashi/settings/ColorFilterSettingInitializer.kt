package jp.toastkid.yobidashi.settings

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.widget.SeekBar
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingColorFilterBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Initializer of color filter's setting section.
 *
 * @author toastkidjp
 */
internal class ColorFilterSettingInitializer(
        private val binding: FragmentSettingColorFilterBinding,
        private val callback: (Int) -> Unit
) {

    private val context = binding.root.context

    private val preferenceApplier = PreferenceApplier(context)

    private val blueBase = ContextCompat.getColor(context, R.color.light_blue_200_dd)

    private val redBase = ContextCompat.getColor(context, R.color.red_200_dd)

    private val yellowBase = ContextCompat.getColor(context, R.color.default_color_filter)

    private val greenBase = ContextCompat.getColor(context, R.color.lime_bg)

    private val darkBase = ContextCompat.getColor(context, R.color.darkgray_scale)

    fun invoke() {
        binding.alpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                // NOP
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // NOP
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val filterColor = preferenceApplier.filterColor()
                setNewColor(seekBar?.progress ?: 0, filterColor)
            }

        })
        binding.blue.setOnClickListener{   setNewColor(currentAlpha(), blueBase)   }
        binding.red.setOnClickListener{    setNewColor(currentAlpha(), redBase)    }
        binding.yellow.setOnClickListener{ setNewColor(currentAlpha(), yellowBase) }
        binding.green.setOnClickListener{  setNewColor(currentAlpha(), greenBase)  }
        binding.dark.setOnClickListener{   setNewColor(currentAlpha(), darkBase)   }
        binding.defaultColor.setOnClickListener{
            setNewColor(DEFAULT_ALPHA, yellowBase)
            binding.alpha.progress = DEFAULT_ALPHA
        }
    }

    private fun currentAlpha(): Int = Color.alpha(preferenceApplier.filterColor())

    private fun setNewColor(alpha: Int, @ColorInt newBaseColor: Int) {
        val newColor = ColorUtils.setAlphaComponent(newBaseColor, alpha)
        preferenceApplier.setFilterColor(newColor)
        binding.sample.setBackgroundColor(newColor)
        callback(newColor)
    }

    companion object {
        private const val DEFAULT_ALPHA = 34
    }

}