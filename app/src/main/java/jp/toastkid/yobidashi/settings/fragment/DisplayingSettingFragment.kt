/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.lib.view.CompoundDrawableColorApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingDisplayBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.settings.DarkModeApplier
import jp.toastkid.yobidashi.settings.background.Adapter
import jp.toastkid.yobidashi.settings.background.ClearImagesDialogFragment
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.background.load.LoadedAction

/**
 * Display setting fragment.
 *
 * @author toastkidjp
 */
class DisplayingSettingFragment : Fragment() {

    /**
     * View binding.
     */
    private lateinit var binding: FragmentSettingDisplayBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    /**
     * ModuleAdapter.
     */
    private var adapter: Adapter? = null

    /**
     * Wrapper of FilesDir.
     */
    private lateinit var filesDir: FilesDir

    private val addingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            LoadedAction(
                it.data?.data,
                binding.fabParent,
                preferenceApplier.colorPair(),
                { adapter?.notifyDataSetChanged() },
                BACKGROUND_DIR
            )
                .invoke()
        }
    )

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
        val activityContext = activity
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this
        contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imagesView.layoutManager = GridLayoutManager(
                view.context,
                2,
                LinearLayoutManager.VERTICAL,
                false
        )

        filesDir = FilesDir(view.context, BACKGROUND_DIR)

        adapter = Adapter(preferenceApplier, filesDir)
        binding.imagesView.adapter = adapter
        if (adapter?.itemCount == 0) {
            val activity = activity ?: return
            Toaster.withAction(
                activity.findViewById(android.R.id.content),
                R.string.message_snackbar_suggestion_select_background_image,
                R.string.select,
                { launchAdding() },
                preferenceApplier.colorPair()
            )
        }

        parentFragmentManager.setFragmentResultListener(
            "clear_images",
            viewLifecycleOwner,
            { _, _ ->
                filesDir.clean()
                contentViewModel?.snackShort(R.string.message_success_image_removal)
                adapter?.notifyDataSetChanged()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        preferenceApplier.colorPair().applyReverseTo(binding.fab)

        val color = IconColorFinder.from(binding.root).invoke()
        CompoundDrawableColorApplier().invoke(
                color,
                binding.applyDarkMode,
                binding.clearBackgroundSettings
        )
    }

    fun applyDarkMode() {
        DarkModeApplier().invoke(preferenceApplier, binding.fabParent)
    }

    /**
     * Clear background setting.
     */
    fun removeBackgroundSettings() {
        preferenceApplier.removeBackgroundImagePath()
        contentViewModel?.snackShort(R.string.message_reset_bg_image)
    }

    /**
     * Launch Adding action.
     */
    fun launchAdding() {
        addingLauncher.launch(IntentFactory.makePickImage())
    }

    /**
     * Clear all images.
     */
    private fun clearImages() {
        ClearImagesDialogFragment().also {
            it.show(parentFragmentManager, ClearImagesDialogFragment::class.java.simpleName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.background_setting_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.background_settings_toolbar_menu_add -> {
            launchAdding()
            true
        }
        R.id.background_settings_add_default -> {
            DefaultBackgroundImagePreparation().invoke(binding.root.context) {
                activity?.runOnUiThread { adapter?.notifyDataSetChanged() }
            }
            true
        }
        R.id.background_settings_toolbar_menu_clear -> {
            clearImages()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        contentViewModel?.refresh()
        addingLauncher.unregister()

        parentFragmentManager.clearFragmentResultListener("clear_images")

        super.onDetach()
    }

    companion object : TitleIdSupplier {

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

        @StringRes
        override fun titleId(): Int = R.string.title_settings_display

    }
}