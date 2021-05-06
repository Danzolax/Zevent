package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.adapters.ALL_EVENTS_PAGE_INDEX
import com.zolax.zevent.adapters.BEGIN_EVENTS_PAGE_INDEX
import com.zolax.zevent.adapters.EventsPagerAdapter
import com.zolax.zevent.adapters.MY_EVENTS_PAGE_INDEX
import com.zolax.zevent.ui.viewmodels.MainEventsViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_events_viewpager.*
import java.lang.IndexOutOfBoundsException

@AndroidEntryPoint
class MainEventsFragment : Fragment(R.layout.fragment_events_viewpager) {
    private val mainEventsViewModel: MainEventsViewModel by viewModels()
    private var isVotingListNoEmpty = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        eventsViewPager.adapter = EventsPagerAdapter(this)
        TabLayoutMediator(tabBar,eventsViewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        subscribeObservers()
        mainEventsViewModel.checkVotingCount(FirebaseAuth.getInstance().uid!!)
    }

    private fun subscribeObservers() {
        mainEventsViewModel.isVotingListNoEmpty.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success ->{
                    isVotingListNoEmpty = result.data!!
                    requireActivity().invalidateOptionsMenu()
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Мероприятия"
            it.setHomeButtonEnabled(false)
            it.setDisplayHomeAsUpEnabled(false)

        }
        if (isVotingListNoEmpty){
            menu.findItem(R.id.action_subscribe).isVisible = true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    private fun getTabTitle(position: Int): String {
        return when(position){
            ALL_EVENTS_PAGE_INDEX -> "Все"
            MY_EVENTS_PAGE_INDEX -> "Мои"
            BEGIN_EVENTS_PAGE_INDEX -> "Текущие"
            else -> throw IndexOutOfBoundsException()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_subscribe -> {
                findNavController().navigate(R.id.action_eventsFragment_to_votingsFragment)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


}