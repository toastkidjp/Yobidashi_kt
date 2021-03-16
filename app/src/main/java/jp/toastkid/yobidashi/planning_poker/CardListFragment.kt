/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.planning_poker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentPlanningPokerBinding

/**
 * @author toastkidjp
 */
class CardListFragment : Fragment() {

    private lateinit var binding: FragmentPlanningPokerBinding

    private var viewModel: CardListFragmentViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_planning_poker, container, false)

        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        binding.cardsView.let {
            it.adapter = Adapter()
            it.layoutManager?.scrollToPosition(Adapter.medium())
            ItemTouchHelper(
                    object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP, ItemTouchHelper.UP) {
                        override fun onMove(
                                rv: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder
                        ): Boolean {
                            val fromPos = viewHolder.adapterPosition
                            val toPos = target.adapterPosition
                            it.adapter?.notifyItemMoved(fromPos, toPos)
                            (viewHolder as? ViewHolder)?.open()
                            return true
                        }

                        override fun onSwiped(
                                viewHolder: RecyclerView.ViewHolder,
                                direction: Int
                        ) {
                            (viewHolder as? ViewHolder)?.open()
                        }
                    }).attachToRecyclerView(it)
            LinearSnapHelper().attachToRecyclerView(it)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardFragmentUseCase = CardFragmentAttachingUseCase(parentFragmentManager)

        activity?.also { fragmentActivity ->
            viewModel = ViewModelProvider(fragmentActivity)
                    .get(CardListFragmentViewModel::class.java)
            viewModel
                    ?.nextCard
                    ?.observe(fragmentActivity, Observer { cardFragmentUseCase(it) })
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel?.clearValue()
    }

}