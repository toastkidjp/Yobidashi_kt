package jp.toastkid.yobidashi.editor

import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.libs.facade.BaseModule

/**
 * Editor activity.
 *
 * @author toastkidjp
 */
class EditorModule(private val binding: ModuleEditorBinding): BaseModule(binding.root) {

    fun view(): View = binding.root

    companion object {

        /**
         * Layout ID.
         */
        private val LAYOUT_ID = R.layout.module_editor

    }
}