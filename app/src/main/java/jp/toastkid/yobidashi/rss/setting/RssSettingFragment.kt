/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentRssSettingBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment

/**
 * @author toastkidjp
 */
class RssSettingFragment : Fragment(), CommonFragmentAction {

    private lateinit var binding: FragmentRssSettingBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rss_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context

        val fragmentActivity = requireActivity()

        val preferenceApplier = PreferenceApplier(fragmentActivity)

        val adapter = Adapter(preferenceApplier)
        binding.rssSettingList.adapter = adapter
        binding.rssSettingList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        SwipeActionAttachment().invoke(binding.rssSettingList)

        val rssReaderTargets = preferenceApplier.readRssReaderTargets()

        if (rssReaderTargets.isEmpty()) {
            Toaster.tShort(fragmentActivity, R.string.message_rss_reader_launch_failed)
            activity?.supportFragmentManager?.popBackStack()
            return
        }

        adapter.replace(rssReaderTargets)
        adapter.notifyDataSetChanged()
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

}