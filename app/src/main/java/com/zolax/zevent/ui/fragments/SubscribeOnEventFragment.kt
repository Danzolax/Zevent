package com.zolax.zevent.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.ui.viewmodels.SubscribeOnEventViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_subscribe_on_event.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SubscribeOnEventFragment() : Fragment(R.layout.fragment_subscribe_on_event) {
    lateinit var event: Event
    val subscribeOnEventViewModel: SubscribeOnEventViewModel by viewModels()
    private val REQUEST_CODE = 213

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var manager: LocationManager
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setHasOptionsMenu(true)
        initUI()
        initSpinners()
        initButtons()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        subscribeOnEventViewModel.event.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Resource.Success -> {
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.data = CalendarContract.Events.CONTENT_URI
                    intent.putExtra(CalendarContract.Events.TITLE, result.data?.title ?: "Title")
                    intent.putExtra(
                        CalendarContract.Events.EVENT_LOCATION,
                        "${result.data?.latitude} + ${result.data?.longitude}"
                    )
                    intent.putExtra(
                        CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        result.data!!.eventDateTime!!.time
                    )
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivityForResult(intent,REQUEST_CODE)
                    } else{
                        Snackbar.make(
                            requireView(),
                            "???? ?????????????????? ???? ??????????????????????",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }


                }
                is Resource.Error -> {
                    when (result.msg) {
                        "???????? ????????????!" -> {
                            Snackbar.make(
                                requireView(),
                                "???????? ????????????, ???????????????? ????????????",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        "?????????????? ?????????? ?????????????????????? ?? ???????? ??????????" -> {
                            Snackbar.make(
                                requireView(),
                                "???? ???? ???????????? ?????????????????????? ???? ??????????????????????, ???????? ?? ?????? ???? ?????????? ?? ?????? ?????????????????????????? ????????????",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Snackbar.make(requireView(), "???????????? ????????????????!", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        })
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
        players_count.setOnClickListener {
            val bundle = Bundle()
            val gson = Gson()
            bundle.putString("event", gson.toJson(event))
            findNavController().navigate(
                R.id.action_subscribeOnEventFragment_to_eventPlayersFragment,
                bundle
            )
        }
    }

    private fun initUI() {
        val gson = Gson()
        event = gson.fromJson(requireArguments().getString("event"), Event::class.java)
        title.text = event.title
        type.text = event.category
        players_count.text = "${event.players?.size}/${event.playersCount}"
        val calendar = Calendar.getInstance()
        calendar.time = event.eventDateTime
        setDateTimeInTextView(calendar)
        if (event.isNeedEquip) {
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
        when (event.category) {
            "????????????" -> {
                val rolesAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.footballRoleTypes,
                    android.R.layout.simple_spinner_item
                )
                rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                role.adapter = rolesAdapter
            }
            "??????????????????" -> {
                val bAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.basketballRoleTypes,
                    android.R.layout.simple_spinner_item
                )
                bAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                role.adapter = bAdapter
            }
            "????????????????" -> {
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



    private fun buildAlertMessageNoLocationService() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
            .setMessage("???????????????? GPS ?????? ???????????????????????? ???????????? ????????????????????????????")
            .setPositiveButton(
                "????????????????"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        val alert: AlertDialog = builder.create()
        alert.show()
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "????????????????????"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        menu.findItem(R.id.action_subscribe).isVisible = true

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_subscribe -> {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                        if (it != null) {
                            subscribeOnEventViewModel.subscribeEventById(
                                event.id!!,
                                createPlayer(it)!!
                            )
                        } else {
                            Snackbar.make(
                                requireView(),
                                "?????????????? ???????????? ?????????? ???????? ????????????",
                                Snackbar.LENGTH_SHORT
                            )
                        }
                    }
                } else {
                    buildAlertMessageNoLocationService()
                }
                true
            }
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createPlayer(location: Location): Player? {
        return FirebaseAuth.getInstance().currentUser?.uid?.let {
            if (event.category.equals("????????????")) {
                Player(
                    it,
                    "??????",
                    rank.selectedItem as String,
                    location.latitude,
                    location.longitude
                )
            } else {
                Player(
                    it,
                    role.selectedItem as String,
                    rank.selectedItem as String,
                    location.latitude,
                    location.longitude
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE){
            Snackbar.make(
                requireView(),
                "???? ?????????????????? ???? ??????????????????????",
                Snackbar.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}