/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.Manifest
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
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

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            adapter?.clear()

            MediaControllerCompat.getMediaController(requireActivity())?.transportControls?.prepare()
            children.forEach { adapter?.add(it) }

            activity?.runOnUiThread { adapter?.notifyDataSetChanged() }
        }
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            MediaControllerCompat.setMediaController(
                    requireActivity(),
                    MediaControllerCompat(context, mediaBrowser.sessionToken)
            )
            mediaBrowser.subscribe(mediaBrowser.root, subscriptionCallback)
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    //binding.play.setImageResource(R.drawable.ic_pause)
                }
                else -> {
                    //binding.play.setImageResource(R.drawable.ic_play_media)
                }
            }
        }
    }

    private val disposables = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        val preferenceApplier = PreferenceApplier(context)
        adapter = Adapter(LayoutInflater.from(context), preferenceApplier) {
            val mediaController = MediaControllerCompat.getMediaController(requireActivity())
            mediaController.transportControls.playFromUri(it.description.mediaUri, bundleOf())
        }

        binding.mediaList.adapter = adapter
        binding.mediaList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val stopBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_stop)
        val playBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_play)

        val colorPair = preferenceApplier.colorPair()

        binding.playSwitch.setOnClickListener {
            val mediaController = MediaControllerCompat.getMediaController(requireActivity())
            Timber.i("mediaController.playbackState.state  ${mediaController.playbackState.state }")
            val icon = if (mediaController.playbackState.state == PlaybackState.STATE_PLAYING) {
                mediaController.transportControls.pause()
                playBitmap
            } else if (mediaController.playbackState.state == PlaybackState.STATE_PAUSED) {
                mediaController.transportControls.play()
                stopBitmap
            } else {
                playBitmap
            }
            binding.playSwitch.setImageBitmap(icon)
            binding.playSwitch.setColorFilter(colorPair.fontColor())
        }

        binding.control.setBackgroundColor(colorPair.bgColor())
        binding.reset.setColorFilter(colorPair.fontColor())
        binding.playSwitch.setColorFilter(colorPair.fontColor())
    }

    fun reset() {
        MediaControllerCompat.getMediaController(requireActivity()).transportControls.stop()
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowser = MediaBrowserCompat(
                context,
                ComponentName(requireContext(), MediaPlayerService::class.java),
                connectionCallback,
                null
        )
    }

    override fun onStart() {
        super.onStart()

        RxPermissions(requireActivity())
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it) {
                                mediaBrowser.connect()
                                return@subscribe
                            }

                            Toaster.snackShort(
                                    binding.root,
                                    R.string.message_audio_file_is_not_found,
                                    PreferenceApplier(binding.root.context).colorPair()
                            )
                            activity?.supportFragmentManager?.popBackStack()
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(requireActivity())?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
        //adapter?.dispose()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_media_player
    }
}