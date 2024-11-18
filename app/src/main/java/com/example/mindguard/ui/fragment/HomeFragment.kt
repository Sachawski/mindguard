package com.example.mindguard.ui.fragment

import android.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mindguard.databinding.FragmentHomeBinding
import com.example.mindguard.ui.viewmodel.HomeViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val totalScreenTimeView: TextView = binding.totalScreenTime
        if (homeViewModel.isAccessGranted(this.context)) {
            val screenTime = homeViewModel.getTotalScreenTime()
            totalScreenTimeView.text = " Total screen time : " + homeViewModel.formatMillisToHoursAndMinutes(screenTime)
        } else {
            totalScreenTimeView.text = "Please grant access to usage statistics."
        }



        // this way of displaying the usage stats is temporary (I think).
        val usageStatsView: ListView = binding.usageStats
        val usageStats = homeViewModel.getUsageStats()
        val formattedList = usageStats.map {"${it.first}: ${homeViewModel.formatMillisToHoursAndMinutes(it.second)}"}

        val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, mutableListOf())
        usageStatsView.adapter = adapter
        adapter.clear()
        adapter.addAll(formattedList)
        adapter.notifyDataSetChanged()

        // manage the display of input dialog
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
                homeViewModel.createUser(userInput)
            }
            .setNegativeButton("Cancel") { dialogInterface, which ->
                requireActivity().finish()
            }
            .create()
        dialog.show()
    }
}
