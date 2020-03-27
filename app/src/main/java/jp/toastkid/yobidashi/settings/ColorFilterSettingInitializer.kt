package jp.toastkid.yobidashi.settings

import jp.toastkid.yobidashi.databinding.FragmentSettingColorFilterBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * TODO Delete it.
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

    fun invoke() {
        /*
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
        binding.defaultColor.setOnClickListener{
            setNewColor(DEFAULT_ALPHA, yellowBase)
            binding.alpha.progress = DEFAULT_ALPHA
        }
         */
    }

}