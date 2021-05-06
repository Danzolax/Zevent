package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.adapters.MyEventsAdapter
import com.zolax.zevent.adapters.VotingsAdapter
import com.zolax.zevent.ui.viewmodels.VotingsViewModel
import com.zolax.zevent.util.Resource

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_votings.*


@AndroidEntryPoint
class VotingsFragment : Fragment(R.layout.fragment_votings) {

    private val votingsViewModel: VotingsViewModel by viewModels()
    private lateinit var votingsAdapter: VotingsAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar.isVisible = false
        setHasOptionsMenu(true)
        initAdapter(recycler_view)
        subscribeObservers()
        votingsViewModel.getVotings(FirebaseAuth.getInstance().uid!!)
    }


    private fun initAdapter(recyclerView: RecyclerView) {
        votingsAdapter = VotingsAdapter(votingsViewModel)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = votingsAdapter
        }
    }

    private fun subscribeObservers() {
        votingsViewModel.votings.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success ->{
                    progressBar.isVisible = false
                    votingsAdapter.votingsId = result.data!!.id!!
                    votingsAdapter.votings = result.data.votings!!
                }
                is Resource.Error ->{
                    progressBar.isVisible = false
                    Snackbar.make(
                        requireView(),
                        "Ошибка загрузки списка голосований",
                        Snackbar.LENGTH_SHORT
                    )
                }
                is Resource.Loading ->{
                    progressBar.isVisible = true
                }
            }
        })
        votingsViewModel.isSuccessVoting.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success ->{
                    progressBar.isVisible = false
                    votingsViewModel.getVotings(FirebaseAuth.getInstance().uid!!)
                }
                is Resource.Error ->{
                    progressBar.isVisible = false
                    Snackbar.make(
                        requireView(),
                        "Ошибка голосования",
                        Snackbar.LENGTH_SHORT
                    )
                }
                is Resource.Loading ->{
                    progressBar.isVisible = true
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Голосование"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)

        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
           android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


}