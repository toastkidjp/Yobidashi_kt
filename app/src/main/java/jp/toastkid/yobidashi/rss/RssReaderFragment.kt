/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentRssReaderBinding
import jp.toastkid.yobidashi.main.MainActivity
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rss_reader, container, false)
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

        Maybe.fromCallable {
            RssReaderApi().invoke("https://github.com/toastkidjp.private.atom?token=ADZ5PO4BMWEOOCACE6U6WEV4CXZK2")
        }
                .subscribeOn(Schedulers.io())
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

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

}