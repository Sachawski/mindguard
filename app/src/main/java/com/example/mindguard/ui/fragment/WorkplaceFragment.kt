package com.example.mindguard.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mindguard.BuildConfig
import com.example.mindguard.R
import com.example.mindguard.databinding.FragmentWorkplaceBinding
import com.example.mindguard.ui.viewmodel.WorkplaceViewModel
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.ui.MapFragment

class WorkplaceFragment : Fragment() {

    private var _binding: FragmentWorkplaceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val workplaceViewModel =
            ViewModelProvider(this).get(WorkplaceViewModel::class.java)

        _binding = FragmentWorkplaceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textWorkplace
        workplaceViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val mapOptions = MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)
        val mapFragment = MapFragment.newInstance(mapOptions)
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}