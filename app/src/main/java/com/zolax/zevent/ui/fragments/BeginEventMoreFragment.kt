package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.ui.viewmodels.BeginEventMoreViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.fragment_begin_event_more.*
import kotlinx.android.synthetic.main.fragment_begin_event_more.progressBar
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class BeginEventMoreFragment : Fragment(R.layout.fragment_begin_event_more) {
    private val beginEventMoreViewModel: BeginEventMoreViewModel by viewModels()
    lateinit var event: Event
    private lateinit var player: Player


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar.isVisible = false
        subscribeObservers()
        beginEventMoreViewModel.getBeginEventById(requireArguments().getString("eventId")!!)
    }



    private fun subscribeObservers() {
        beginEventMoreViewModel.isSuccessUnsubscribe.observe(viewLifecycleOwner,{ result ->
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

        beginEventMoreViewModel.isSuccessDelete.observe(viewLifecycleOwner,{ result ->
            when(result){
                is Resource.Success ->{
                    progressBar.isVisible = false
                    end_event_button.isClickable = true
                    Snackbar.make(requireView(),"Вы успешно завершили мероприятие", Snackbar.LENGTH_SHORT)
                    findNavController().popBackStack()
                }
                is Resource.Error ->{
                    progressBar.isVisible = false
                    end_event_button.isClickable = true
                    Snackbar.make(requireView(),"Ошибка завершения мероприятия, попробуйте позже", Snackbar.LENGTH_SHORT)
                }
                is Resource.Loading ->{
                    progressBar.isVisible = true
                    end_event_button.isClickable = false
                }
            }
        })

        beginEventMoreViewModel.currentEvent.observe(viewLifecycleOwner,{ result ->
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
        if (event.players?.get(0)?.userId == FirebaseAuth.getInstance().uid){
            end_event_button.visibility = View.VISIBLE
        }
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
                R.id.action_beginEventMoreFragment_to_simpleMapViewerFragment,
                bundle
            )
        }
        players_count.setOnClickListener {
            val bundle = Bundle()
            val gson = Gson()
            bundle.putString("event",gson.toJson(event))
            findNavController().navigate(R.id.action_beginEventMoreFragment_to_beginEventPlayersFragment,bundle)
        }
        end_event_button.setOnClickListener {
            beginEventMoreViewModel.addPlayersInVotingsAndDeleteBeginEvent(event.id!!, FirebaseAuth.getInstance().uid!!)
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
        if (event.players?.get(0)?.userId != FirebaseAuth.getInstance().uid){
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
                beginEventMoreViewModel.unsubscribeBeginEventById(event.id!!, player)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

