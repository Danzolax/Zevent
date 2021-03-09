package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zolax.zevent.R
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_btn.setOnClickListener {
            findNavController().popBackStack()
        }
        accept_btn.setOnClickListener {
            Toast.makeText(requireContext(),"Профиль изменен", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        profileAvatar.setOnClickListener {
            Toast.makeText(requireContext(),"Аватар изменен", Toast.LENGTH_SHORT).show()
        }
    }
}