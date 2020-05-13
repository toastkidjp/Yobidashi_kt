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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentPlanningPokerBinding

/**
 * @author toastkidjp
 */
class CardListFragment : Fragment() {

    private lateinit var binding: FragmentPlanningPokerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_planning_poker, container, false)

        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        val layoutManager = LinearLayoutManager(activityContext, LinearLayoutManager.HORIZONTAL, false)
        binding.cardsView.let {
            it.layoutManager = layoutManager
            it.adapter = Adapter()
            layoutManager.scrollToPosition(Adapter.medium())
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
                            (viewHolder as ViewHolder).open()
                            return true
                        }

                        override fun onSwiped(
                                viewHolder: RecyclerView.ViewHolder,
                                direction: Int
                        ) {
                            (viewHolder as ViewHolder).open()
                        }
                    }).attachToRecyclerView(it)
            LinearSnapHelper().attachToRecyclerView(it)
        }
        return binding.root
    }

    private var viewModel: CardListFragmentViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.also { fragmentActivity ->
            viewModel = ViewModelProviders.of(fragmentActivity)
                    .get(CardListFragmentViewModel::class.java)
            viewModel
                    ?.nextCard
                    ?.observe(fragmentActivity, Observer { openCard(it) })
        }
    }

    private fun openCard(text: String?) {
        if (text.isNullOrBlank()) {
            return
        }

        val transaction = fragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
        transaction?.add(R.id.content, CardFragment.makeWithNumber(text))
        transaction?.addToBackStack(CardFragment::class.java.canonicalName)
        transaction?.commit()
    }

    override fun onDetach() {
        super.onDetach()
        viewModel?.clearValue()
    }

}