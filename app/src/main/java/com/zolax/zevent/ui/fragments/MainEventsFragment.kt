package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.zolax.zevent.R
import com.zolax.zevent.adapters.ALL_EVENTS_PAGE_INDEX
import com.zolax.zevent.adapters.EventsPagerAdapter
import com.zolax.zevent.adapters.MY_EVENTS_PAGE_INDEX
import kotlinx.android.synthetic.main.fragment_events_viewpager.*
import java.lang.IndexOutOfBoundsException

class MainEventsFragment : Fragment(R.layout.fragment_events_viewpager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventsViewPager.adapter = EventsPagerAdapter(this)
        TabLayoutMediator(tabBar,eventsViewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
    }

    private fun getTabTitle(position: Int): String? {
        return when(position){
            ALL_EVENTS_PAGE_INDEX -> "Все мероприятия"
            MY_EVENTS_PAGE_INDEX -> "Мои мероприятия"
            else -> throw IndexOutOfBoundsException()
        }
    }


}