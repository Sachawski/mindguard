package com.example.mindguard.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mindguard.databinding.FragmentHomeBinding
import com.example.mindguard.ui.viewmodel.HomeViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        homeViewModel.showInputDialog.observe(viewLifecycleOwner, Observer { shouldShow ->
            if (shouldShow) {
                showInputDialog(homeViewModel)
            }
        })
        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showInputDialog(homeViewModel : HomeViewModel) {
        val inputEditText = EditText(this.context)
        val dialog = AlertDialog.Builder(this.context)
            .setTitle("Username")
            .setMessage("Please enter your username")
            .setView(inputEditText)
            .setPositiveButton("OK") { dialogInterface, which ->
                val userInput = inputEditText.text.toString()
                homeViewModel.setUserInput(userInput)
            }
            .setNegativeButton("Annuler") { dialogInterface, which ->
                println("L'utilisateur a annulé.")
            }
            .create()
        dialog.show()
    }
}