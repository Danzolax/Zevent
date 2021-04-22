package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.*
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.current_datetime
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.map_button
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.players_count
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.role
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.role_container
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.title
import java.util.*


class SubscribeOnEventFragment() : Fragment(R.layout.fragment_subscribe_on_event) {
    lateinit var event: Event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initSpinners()
        initUI()
        initButtons()
    }

    private fun initButtons() {
        map_button.setOnClickListener {
            val bundle = Bundle()
            bundle.putDouble("latitude", event.latitude!!)
            bundle.putDouble("longitude", event.longitude!!)
            findNavController().navigate(
                R.id.action_subscribeOnEventFragment_to_simpleMapViewerFragment,
                bundle
            )
        }
    }

    private fun initUI() {
        val gson = Gson()
        event =  gson.fromJson(requireArguments().getString("event"),Event::class.java)
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
    }


    private fun setDateTimeInTextView(pickedDateTime: Calendar) {
        current_datetime.text = DateUtils.formatDateTime(
            requireContext(),
            pickedDateTime.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                    or DateUtils.FORMAT_SHOW_TIME
        )

    }

    private fun initSpinners() {
        when (type.text.toString()) {
            "Футбол" -> {
                val rolesAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.footballRoleTypes,
                    android.R.layout.simple_spinner_item
                )
                rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                role.adapter = rolesAdapter
            }
            "Баскетбол" -> {
                val bAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.basketballRoleTypes,
                    android.R.layout.simple_spinner_item
                )
                bAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                role.adapter = bAdapter
            }
            "Волейбол" -> {
                val vAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.volleyballRoleTypes,
                    android.R.layout.simple_spinner_item
                )
                vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                role.adapter = vAdapter
            }
            else -> {
                role_container.visibility = View.GONE
            }
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Записаться"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        menu.findItem(R.id.action_subscribe).isVisible = true

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_subscribe -> {
                Snackbar.make(requireView(), "Подписан!", Snackbar.LENGTH_SHORT).show()
                true
            }
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}