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
import android.provider.MediaStore
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

        val preferenceApplier = PreferenceApplier(view.context)
        adapter = Adapter(LayoutInflater.from(view.context), preferenceApplier)

        binding.mediaList.adapter = adapter
        binding.mediaList.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)

        binding.reset.setOnClickListener {
            adapter?.reset()
        }

        val stopBitmap = BitmapFactory.decodeResource(view.context.resources, R.drawable.ic_stop)
        val playBitmap = BitmapFactory.decodeResource(view.context.resources, R.drawable.ic_play)

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
                            }
                        },
                        {
                            Timber.e(it)
                        }
                )
                .addTo(disposables)
    }

    private fun readAll() {
        adapter?.clear()

        val sortOrder = MediaStore.Audio.AudioColumns.ALBUM + " ASC"
        val contentResolver = context?.contentResolver ?: return
        val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                sortOrder
        )

        while (cursor?.moveToNext() == true) {
            val audio = Audio(
                    id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                    title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                    album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    date = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)),
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            )

            adapter?.add(audio)
        }

        activity?.runOnUiThread { adapter?.notifyDataSetChanged() }
        cursor?.close()
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