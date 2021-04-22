package com.zolax.zevent.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.opengl.Visibility
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zolax.zevent.R
import kotlinx.android.synthetic.main.fragment_add_event.*
import timber.log.Timber
import java.util.*


class AddEventFragment() : Fragment(R.layout.fragment_add_event) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDateTimeInTextView(Calendar.getInstance())
        initButtons()
        setHasOptionsMenu(true)
        initSpinners()
    }

    private fun initSpinners() {
        val typesAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sportTypes,
            android.R.layout.simple_spinner_item
        )

        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        types.adapter = typesAdapter
        types.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View, selectedItemPosition: Int, selectedId: Long
            ) {
                val choose = resources.getStringArray(R.array.sportTypes)
                when(choose[selectedItemPosition]){
                    "Футбол" ->{
                        val rolesAdapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.footballRoleTypes,
                            android.R.layout.simple_spinner_item
                        )
                        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        role.adapter = rolesAdapter
                    }
                    "Баскетбол" ->{
                        val bAdapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.basketballRoleTypes,
                            android.R.layout.simple_spinner_item
                        )
                        bAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        role.adapter = bAdapter
                    }
                    "Волейбол" ->{
                        val vAdapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.volleyballRoleTypes,
                            android.R.layout.simple_spinner_item
                        )
                        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        role.adapter = vAdapter
                    }
                    else ->{
                        role_container.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun initButtons() {
        current_datetime.setOnClickListener {
            pickDateTime()
        }
        is_need_equip.setOnClickListener {
            if (equip.visibility == View.GONE) {
                equip.visibility = View.VISIBLE
            } else {
                equip.visibility = View.GONE
            }
        }

        map_button.setOnClickListener {
            val latitude: Double = requireArguments().getDouble("latitude")
            val longitude: Double = requireArguments().getDouble("longitude")
            val bundle = Bundle()
            bundle.putDouble("latitude", latitude)
            bundle.putDouble("longitude", longitude)
            findNavController().navigate(
                R.id.action_addEventFragment_to_simpleMapViewerFragment,
                bundle
            )
        }
    }

    private fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)
                setDateTimeInTextView(pickedDateTime)
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
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
            it.title = "Добавить мероприятие"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        menu.findItem(R.id.action_confirm).isVisible = true
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
            R.id.action_confirm -> {
                Snackbar.make(requireView(), "Сохрвнено", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
                true
            }
            else -> {
                Timber.d("menu not working!!!")
                super.onOptionsItemSelected(item)
            }
        }
    }


}