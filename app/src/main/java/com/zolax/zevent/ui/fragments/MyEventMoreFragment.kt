package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.ui.viewmodels.MyEventMoreViewModel
import com.zolax.zevent.util.DialogUtil
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_event_more.*
import java.util.*

@AndroidEntryPoint
class MyEventMoreFragment : Fragment(R.layout.fragment_my_event_more) {
    private val myEventMoreViewModel: MyEventMoreViewModel by viewModels()
    lateinit var event: Event
    private lateinit var player: Player
    private val gson = Gson()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        myEventMoreViewModel.getEventById(requireArguments().getString("eventId")!!)
    }



    private fun subscribeObservers() {
        myEventMoreViewModel.isSuccessUnsubscribe.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success ->{
                    Snackbar.make(requireView(),"Вы успешно отписались", Snackbar.LENGTH_SHORT)
                    findNavController().popBackStack()
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Ошибка отписки, попробуйте позже", Snackbar.LENGTH_SHORT)
                }
            }
        })

        myEventMoreViewModel.currentEvent.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success ->{
                    result.data?.let {
                        event = it
                        initUI()
                        initButtons()
                        setHasOptionsMenu(true)
                    }
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Мероприятие не найдено", Snackbar.LENGTH_SHORT)
                    findNavController().popBackStack()
                }
            }
        })

    }

    private fun initUI() {

        title.text = event.title
        type.text = event.category
        players_count.text = "${event.players?.size}/${event.playersCount}"
        val calendar = Calendar.getInstance()
        calendar.time = event.eventDateTime
        setDateTimeInTextView(calendar)
        if (event.isNeedEquip){
            equip_container.visibility = View.VISIBLE
            equip.text = event.needEquip
        }
        event.players?.forEach { elem ->
            if (elem.userId == FirebaseAuth.getInstance().uid){
                player = elem
                role.text = elem.role
                rank.text = elem.rank
                return@forEach
            }
        }
        if (event.category.equals("Другое")){
            role_container.visibility = View.GONE
        }
    }

    private fun initButtons() {
        map_button.setOnClickListener {
            val bundle = Bundle()
            bundle.putDouble("latitude", event.latitude!!)
            bundle.putDouble("longitude", event.longitude!!)
            findNavController().navigate(
                R.id.action_myEventMoreFragment_to_simpleMapViewerFragment,
                bundle
            )
        }
        players_count.setOnClickListener {
            val bundle = Bundle()
            val gson = Gson()
            bundle.putString("event",gson.toJson(event))
            findNavController().navigate(R.id.action_myEventMoreFragment_to_myEventPlayersFragment,bundle)
        }
    }

    private fun setDateTimeInTextView(pickedDateTime: Calendar) {
        current_datetime.text = DateUtils.formatDateTime(
            requireContext(),
            pickedDateTime.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                    or DateUtils.FORMAT_SHOW_TIME
        )

    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Подробности"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        if (event.players?.get(0)?.userId == FirebaseAuth.getInstance().uid){
            menu.findItem(R.id.action_delete_event).isVisible = true
            menu.findItem(R.id.action_edit).isVisible = true
        }else{
            menu.findItem(R.id.action_unsubscribe).isVisible = true

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            R.id.action_unsubscribe ->{
                myEventMoreViewModel.unsubscribeEventById(event.id!!, player)
                true
            }
            R.id.action_delete_event ->{
                DialogUtil.buildConfirmDialog(requireContext(),"Вы точно хотите удалить мероприятие") { _, _ ->
                    myEventMoreViewModel.deleteEventById(event.id!!)
                }.show()
                true
            }
            R.id.action_edit ->{
                val bundle = Bundle()
                bundle.putString("event",gson.toJson(event))
                findNavController().navigate(R.id.action_myEventMoreFragment_to_eventLocationEditFragment,bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

