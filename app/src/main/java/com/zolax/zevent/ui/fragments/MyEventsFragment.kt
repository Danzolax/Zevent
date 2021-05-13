package com.zolax.zevent.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.adapters.MyEventsAdapter
import com.zolax.zevent.ui.viewmodels.MyEventsViewModel
import com.zolax.zevent.util.Constants
import com.zolax.zevent.util.DialogUtil
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.filter_dialog.view.*
import kotlinx.android.synthetic.main.fragment_my_events.*
import java.lang.NumberFormatException

@AndroidEntryPoint
class MyEventsFragment : Fragment(R.layout.fragment_my_events) {
    private val myEventsViewModel: MyEventsViewModel by viewModels()
    private lateinit var myEventsAdapter: MyEventsAdapter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(recycler_view)
        subscribeObservers()
        myEventsViewModel.moveEventsToBeginByUserID(FirebaseAuth.getInstance().uid!!)
        myEventsViewModel.getAllEventsByUserId(FirebaseAuth.getInstance().uid!!)
        initButtons()
    }



    private fun subscribeObservers() {
        myEventsViewModel.eventsData.observe(viewLifecycleOwner, { events ->
            when (events) {
                is Resource.Success -> {
                    myEventsAdapter.events = events.data!!
                }
                is Resource.Error -> {
                    Snackbar.make(
                        requireView(),
                        "Ошибка загрузки мероприятий",
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
        })
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        myEventsAdapter = MyEventsAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myEventsAdapter
        }

    }

    private fun initButtons() {
        fab.setOnClickListener {
            initDialog()
        }
    }

    private fun initDialog() {
        val view = layoutInflater.inflate(R.layout.filter_dialog, null)
        DialogUtil.buildDialogWithView(requireContext(),"Фильтры", view) { _, _ ->
            var currentPlayersCount: Int? = null
            var allPlayersCount: Int? = null
            if (view.current_players_count_filter.text.toString() != "") {
                try {
                    currentPlayersCount =
                        Integer.parseInt(view.current_players_count_filter.text.toString())
                } catch (e: NumberFormatException) {
                    Snackbar.make(
                        requireView(),
                        "Не правильно введено количество игроков",
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
            if (view.all_players_count_filter.text.toString() != "") {
                try {
                    allPlayersCount =
                        Integer.parseInt(view.all_players_count_filter.text.toString())
                } catch (e: NumberFormatException) {
                    Snackbar.make(
                        requireView(),
                        "Не правильно введено количество игроков",
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
            myEventsViewModel.getFilteredListByUserId(
                FirebaseAuth.getInstance().uid!!,
                view.category_filter.selectedItem as String,
                view.date_filter.selectedItem as String,
                view.is_need_equip_filter.isChecked,
                currentPlayersCount,
                allPlayersCount
            )
        }.show()
        val categoryAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            Constants.CATEGORY_FILTER_LIST
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.category_filter.adapter = categoryAdapter
        val dateAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            Constants.DATE_FILTER_LIST
        )
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.date_filter.adapter = dateAdapter
    }
}