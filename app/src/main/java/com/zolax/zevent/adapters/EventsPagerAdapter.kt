package com.zolax.zevent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zolax.zevent.ui.fragments.AllEventsFragment
import com.zolax.zevent.ui.fragments.BeginEventsFragment
import com.zolax.zevent.ui.fragments.MyEventsFragment

const val ALL_EVENTS_PAGE_INDEX = 0
const val MY_EVENTS_PAGE_INDEX = 1
const val BEGIN_EVENTS_PAGE_INDEX = 2

class EventsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return tabFragmentsCreators.size
    }
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        ALL_EVENTS_PAGE_INDEX to { AllEventsFragment() },
        MY_EVENTS_PAGE_INDEX to { MyEventsFragment() },
        BEGIN_EVENTS_PAGE_INDEX to { BeginEventsFragment() }
    )


    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

}





