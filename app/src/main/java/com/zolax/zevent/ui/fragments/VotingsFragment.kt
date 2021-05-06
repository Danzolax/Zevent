package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zolax.zevent.R

import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class VotingsFragment : Fragment(R.layout.fragment_votings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

    }

    private fun subscribeObservers() {

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