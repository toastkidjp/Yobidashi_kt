/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentMediaPlayerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * @author toastkidjp
 */
class MediaPlayerFragment : Fragment(), CommonFragmentAction {

    private lateinit var binding: FragmentMediaPlayerBinding

    private var adapter: Adapter? = null

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media_player, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        val preferenceApplier = PreferenceApplier(context)
        adapter = Adapter(LayoutInflater.from(context), preferenceApplier)

        binding.mediaList.adapter = adapter
        binding.mediaList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.reset.setOnClickListener {
            adapter?.reset()
        }

        val stopBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_stop)
        val playBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_play)

        val colorPair = preferenceApplier.colorPair()

        binding.playSwitch.setOnClickListener {
            val displayStop = adapter?.switch() ?: false
            binding.playSwitch.setImageBitmap(if (displayStop) stopBitmap else playBitmap)
            binding.playSwitch.setColorFilter(colorPair.fontColor())
        }

        binding.control.setBackgroundColor(colorPair.bgColor())
        binding.reset.setColorFilter(colorPair.fontColor())
        binding.playSwitch.setColorFilter(colorPair.fontColor())

        RxPermissions(requireActivity())
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it) {
                                readAll()
                                return@subscribe
                            }

                            Toaster.snackShort(binding.root, "Audio file is not found.", colorPair)
                            activity?.supportFragmentManager?.popBackStack()
                        },
                        {
                            Timber.e(it)
                        }
                )
                .addTo(disposables)
    }

    private fun readAll() {
        adapter?.clear()

        AudioFileFinder()(context?.contentResolver) { adapter?.add(it) }

        activity?.runOnUiThread { adapter?.notifyDataSetChanged() }
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
        adapter?.dispose()
    }
}