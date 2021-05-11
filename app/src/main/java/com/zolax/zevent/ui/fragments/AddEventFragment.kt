package com.zolax.zevent.ui.fragments
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.ui.viewmodels.AddEventViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_event.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AddEventFragment() : Fragment(R.layout.fragment_add_event) {
    private val addEventViewModel: AddEventViewModel by viewModels()
    private val needDatetime = Calendar.getInstance()

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var manager : LocationManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setDateTimeInTextView(Calendar.getInstance())
        initButtons()
        setHasOptionsMenu(true)
        initSpinners()
        subsribeObservers()
    }

    private fun createEvent( location: Location): Event? {
        if ((players_count.text.toString() == "") or ( createPlayer(location) == null)) {
            Snackbar.make(requireView(), "Введите количество игроков!", Snackbar.LENGTH_SHORT)
                .show()
            return null
        }
        if (needDatetime.timeInMillis < Calendar.getInstance().timeInMillis){
            Snackbar.make(requireView(), "Время начала мероприятия должно быть больше, чем текущее!", Snackbar.LENGTH_SHORT)
                .show()
            return null
        }
        if ((title.text.toString() == "") or (title.text.toString().length > 30)){
            Snackbar.make(requireView(), "Введите название мероприятия не более 30 символов!", Snackbar.LENGTH_SHORT)
                .show()
            return null
        }
        val event = Event()
        event.title = title.text.toString()
        event.category = types.selectedItem as String
        event.playersCount = Integer.parseInt(players_count.text.toString())
        event.eventDateTime = needDatetime.time
        event.isNeedEquip = is_need_equip.isChecked
        event.needEquip = equip.text.toString()
        event.players = arrayListOf(createPlayer(location)!!)
        val latitude: Double = requireArguments().getDouble("latitude")
        val longitude: Double = requireArguments().getDouble("longitude")
        event.latitude = latitude
        event.longitude = longitude
        return event
    }

    private fun createPlayer(location: Location): Player? {
        return FirebaseAuth.getInstance().currentUser?.uid?.let {
            Player(
                it,
                role.selectedItem as String,
                rank.selectedItem as String,
                location.latitude,
                location.longitude
            )
        }
    }

    private fun subsribeObservers() {
        addEventViewModel.isSuccessCreateEvent.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Resource.Success -> {
                    Snackbar.make(requireView(), "Мероприятие создано", Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    Snackbar.make(
                        requireView(),
                        "Ошибка создания мероприятия!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun initSpinners() {
        val typesAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sportTypes,
            android.R.layout.simple_spinner_item
        )

        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        types.adapter = typesAdapter
        types?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?, selectedItemPosition: Int, selectedId: Long
            ) {
                when (resources.getStringArray(R.array.sportTypes)[selectedItemPosition]) {
                    "Футбол" -> {
                        role_container.visibility = View.VISIBLE
                        val rolesAdapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.footballRoleTypes,
                            android.R.layout.simple_spinner_item
                        )
                        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        role.adapter = rolesAdapter
                        players_count.setText("22")
                        players_count.isEnabled = false
                    }
                    "Баскетбол" -> {
                        role_container.visibility = View.VISIBLE
                        val bAdapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.basketballRoleTypes,
                            android.R.layout.simple_spinner_item
                        )
                        bAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        role.adapter = bAdapter
                        players_count.setText("10")
                        players_count.isEnabled = false
                    }
                    "Волейбол" -> {
                        val vAdapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.volleyballRoleTypes,
                            android.R.layout.simple_spinner_item
                        )
                        role_container.visibility = View.VISIBLE
                        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        role.adapter = vAdapter
                        players_count.setText("12")
                        players_count.isEnabled = false
                    }
                    else -> {
                        role_container.visibility = View.GONE
                        players_count.setText("")
                        players_count.isEnabled = true
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
                needDatetime.set(year, month, day, hour, minute)

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

    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            R.id.action_confirm -> {
                if ( manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                        if (it != null){
                            createEvent(it)?.let {event ->
                                addEventViewModel.addEvent(event)
                            }
                        } else{
                            Snackbar.make(requireView(), "Нажмите кнопку через пару секунд", Snackbar.LENGTH_SHORT)
                        }
                    }
                } else {
                    buildAlertMessageNoLocationService()
                }
                true
            }
            else -> {
                Timber.d("menu not working!!!")
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun buildAlertMessageNoLocationService() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
            .setMessage("Включите GPS для отслеживания вашего местоположения")
            .setPositiveButton(
                "Включить"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        val alert: AlertDialog = builder.create()
        alert.show()
    }



}