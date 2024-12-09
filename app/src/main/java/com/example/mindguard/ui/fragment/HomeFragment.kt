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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry


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

        val attentionScoreView: TextView = binding.attentionScoreHolder
        homeViewModel.getUser().observe(viewLifecycleOwner) {user ->
            attentionScoreView.text = user.getAttentionScore().toString()
        }


        val barChart : BarChart = binding.barChart
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0f, 10f)) // x = 0, y = 10
        entries.add(BarEntry(1f, 20f)) // x = 1, y = 20
        entries.add(BarEntry(2f, 15f)) // x = 2, y = 15
        entries.add(BarEntry(3f, 25f)) // x = 3, y = 25
        val barDataSet = BarDataSet(entries, "My Data Set")
        barDataSet.color = resources.getColor(R.color.holo_purple, null)  // Couleur des barres
        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.description.text = "Example BarChart"  // Description
        barChart.animateY(1000)  // Animation sur l'axe Y
        // Configurer les axes (facultatif)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false  // Désactiver l'axe Y droit



        val workTimeView: TextView = binding.workingTime
        val socialTimeView: TextView = binding.socialTime
        val workScreenTimeView: TextView = binding.workingScreenTime
        val socialScreenTimeView: TextView = binding.socialScreenTime

        homeViewModel.getUser().observe(viewLifecycleOwner) { user ->
            socialTimeView.text =
                homeViewModel.formatMillisToHoursAndMinutes(user.getScreenTimeInfos().getTotalSocialTime())
            workTimeView.text =
                homeViewModel.formatMillisToHoursAndMinutes(user.getScreenTimeInfos().getTotalWorkTime())
            socialScreenTimeView.text =
                homeViewModel.formatMillisToHoursAndMinutes(user.getScreenTimeInfos().getTotalSocialScreenTime())
            workScreenTimeView.text =
                homeViewModel.formatMillisToHoursAndMinutes(user.getScreenTimeInfos().getTotalWorkScreenTime())
        }

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
