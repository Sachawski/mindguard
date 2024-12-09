package com.example.mindguard.ui.fragment


import android.R
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Visibility
import com.example.mindguard.data.model.State
import com.example.mindguard.databinding.FragmentWorkplaceBinding
import com.example.mindguard.ui.viewmodel.WorkplaceViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles


class WorkplaceFragment : Fragment() {

    private var _binding: FragmentWorkplaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var workplaceViewModel : WorkplaceViewModel
    private lateinit var theContext : Context
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workplaceViewModel = ViewModelProvider(this).get(WorkplaceViewModel::class.java)

        _binding = FragmentWorkplaceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        theContext = this.requireContext()



        val textView: TextView = binding.textWorkplace
        workplaceViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val isWithFriendsView: TextView = binding.isAtWorkplace
        workplaceViewModel.getUser().observe(viewLifecycleOwner){ user ->
            if (user.getState() == State.WORKING) {
                    isWithFriendsView.visibility = View.VISIBLE
                isWithFriendsView.text = "You are at work, you should stop looking at your phone"
            } else {
                isWithFriendsView.visibility = View.INVISIBLE
            }
            Log.d("ok","displaying")
            Log.d("ok",user.getState().toString())

        }


        val mapView: MapView = binding.mapView
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(error: Exception) {
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                val location = workplaceViewModel.getUser().value!!.getLocation()
                val defaultPosition = LatLng.from(location.first, location.second)

                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(defaultPosition))

                workplaceViewModel.getUser().observe(viewLifecycleOwner){ user ->
                    kakaoMap.labelManager!!.layer!!.removeAll()
                    displayUserLocationOnMap(user.getLocation(),kakaoMap)
                    displayWorkplaceOnMap(user.getWorkplace(),kakaoMap)
                }

                kakaoMap.setOnMapClickListener { kakaoMap, latLng, pointF, POI ->
                    workplaceViewModel.addWorkplaceToUser(latLng.latitude,latLng.longitude)
                }

                kakaoMap.setOnLabelClickListener { kakaoMap, layer, label ->
                    AlertDialog.Builder(context).apply {
                        setTitle("Delete workplace")
                        setMessage("Do you want to delete this workplace?")
                        setPositiveButton("Yes") { _, _ ->
                            kakaoMap.labelManager?.layer!!.remove(label)
                            val latLng : LatLng = label.position
                            workplaceViewModel.removeWorkplaceToUser(latLng.latitude,latLng.longitude)
                        }
                        setNegativeButton("No", null)
                    }.show()

                    true
                }
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayUserLocationOnMap(location : Pair<Double,Double>,kakaoMap: KakaoMap) {
        val userStyle = kakaoMap.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.ic_menu_more)))
        val options = LabelOptions.from(LatLng.from(location.first, location.second)).setStyles(userStyle)
        val layer = kakaoMap.labelManager!!.layer
        layer!!.addLabel(options)
    }

    private fun displayWorkplaceOnMap(workplaceLocations : MutableSet<Pair<Double,Double>>,kakaoMap: KakaoMap) {
        val userStyle = kakaoMap.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.ic_input_add)))
        val layer = kakaoMap.labelManager!!.layer
        for (workplace in workplaceLocations) {
            val options =
                LabelOptions.from(LatLng.from(workplace.first, workplace.second)).setStyles(userStyle)
            options.setClickable(true)
            layer!!.addLabel(options)
        }
    }
}