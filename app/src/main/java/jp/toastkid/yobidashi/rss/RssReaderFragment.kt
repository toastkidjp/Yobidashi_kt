/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentRssReaderBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.main.content.ContentSwitchOrder
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.rss.list.Adapter
import timber.log.Timber

/**
 * @author toastkidjp
 */
class RssReaderFragment : Fragment(), CommonFragmentAction {

    private lateinit var binding: FragmentRssReaderBinding

    private var viewModel: RssReaderFragmentViewModel? = null

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context

        val fragmentActivity = requireActivity()
        viewModel = ViewModelProviders.of(this).get(RssReaderFragmentViewModel::class.java)
        observeViewModelEvent(fragmentActivity)

        val adapter = Adapter(LayoutInflater.from(context), viewModel)
        binding.rssList.adapter = adapter
        binding.rssList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val readRssReaderTargets = PreferenceApplier(fragmentActivity).readRssReaderTargets()

        if (readRssReaderTargets.isEmpty()) {
            Toaster.tShort(fragmentActivity, R.string.message_rss_reader_launch_failed)
            activity?.supportFragmentManager?.popBackStack()
            return
        }

        Observable.fromIterable(readRssReaderTargets)
                .subscribeOn(Schedulers.io())
                .map { RssReaderApi().invoke(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { adapter.addAll(it?.items) },
                        Timber::e
                )
                .addTo(disposables)
    }

    private fun observeViewModelEvent(fragmentActivity: FragmentActivity) {
        viewModel?.itemClick?.observe(this, object : Observer<String> {
            override fun onChanged(t: String?) {
                if (t == null) {
                    return
                }
                fragmentActivity.startActivity(MainActivity.makeBrowserIntent(fragmentActivity, t.toUri()))
                viewModel?.itemClick?.removeObserver(this)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.rss_reader, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_rss_setting) {
            ViewModelProviders.of(requireActivity())
                    .get(ContentViewModel::class.java)
                    .nextContent(ContentSwitchOrder.RSS_SETTING)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    companion object {
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_rss_reader
    }
}