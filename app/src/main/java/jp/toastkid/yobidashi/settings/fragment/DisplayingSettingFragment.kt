/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingDisplayBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.settings.DarkModeApplier
import jp.toastkid.yobidashi.settings.background.Adapter
import jp.toastkid.yobidashi.settings.background.ClearImagesDialogFragment
import jp.toastkid.yobidashi.settings.background.LoadedAction

/**
 * Display setting fragment.
 *
 * @author toastkidjp
 */
class DisplayingSettingFragment : Fragment(), TitleIdSupplier, ClearImagesDialogFragment.Callback {

    /**
     * View binding.
     */
    private lateinit var binding: FragmentSettingDisplayBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * ModuleAdapter.
     */
    private var adapter: Adapter? = null

    /**
     * Wrapper of FilesDir.
     */
    private lateinit var filesDir: FilesDir

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                LAYOUT_ID,
                container,
                false
        )
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imagesView.layoutManager = LinearLayoutManager(
                view.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        filesDir = FilesDir(view.context, BACKGROUND_DIR)

        adapter = Adapter(preferenceApplier, filesDir)
        binding.imagesView.adapter = adapter
        if (adapter?.itemCount == 0) {
            Toaster.withAction(
                    binding.fabParent,
                    R.string.message_snackbar_suggestion_select_background_image,
                    R.string.select,
                    View.OnClickListener { launchAdding() },
                    preferenceApplier.colorPair()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceApplier.colorPair().applyReverseTo(binding.fab)
    }

    fun applyDarkMode() {
        DarkModeApplier().invoke(preferenceApplier, binding.fabParent)
    }

    /**
     * Clear background setting.
     */
    fun clearBackgroundSettings() {
        preferenceApplier.removeBackgroundImagePath()
        Toaster.snackShort(
                binding.fabParent,
                R.string.message_reset_bg_image,
                preferenceApplier.colorPair()
        )
    }

    /**
     * Launch Adding action.
     */
    fun launchAdding() {
        startActivityForResult(IntentFactory.makePickImage(), IMAGE_READ_REQUEST)
    }

    /**
     * Clear all images.
     */
    private fun clearImages() {
        ClearImagesDialogFragment().also {
            it.setTargetFragment(this, IMAGE_READ_REQUEST)
            it.show(
                    parentFragmentManager,
                    ClearImagesDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onClickClearImages() {
        filesDir.clean()
        // TODO use ContentViewModel.
        Toaster.snackShort(
                binding.fabParent,
                R.string.message_success_image_removal,
                preferenceApplier.colorPair()
        )
        adapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        if (requestCode == IMAGE_READ_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            LoadedAction(
                    data,
                    binding.fabParent,
                    preferenceApplier.colorPair(),
                    { adapter?.notifyDataSetChanged() },
                    BACKGROUND_DIR
            )
                    .invoke()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.background_setting_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.background_settings_toolbar_menu_add -> {
                launchAdding()
                true
            }
            R.id.background_settings_toolbar_menu_clear -> {
                clearImages()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @StringRes
    override fun titleId() = R.string.title_settings_display

    companion object {

        /**
         * Background image dir.
         */
        private const val BACKGROUND_DIR: String = "background_dir"

        /**
         * Request code.
         */
        private const val IMAGE_READ_REQUEST: Int = 136

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_display

        fun getBackgroundDirectory() = BACKGROUND_DIR

    }
}