package com.example.mindguard.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mindguard.data.model.Friend
import com.example.mindguard.data.model.State
import com.example.mindguard.data.service.BackgroundService
import com.example.mindguard.databinding.FragmentFriendsBinding
import com.example.mindguard.ui.Tools
import com.example.mindguard.ui.activity.FriendsAdapter
import com.example.mindguard.ui.viewmodel.FriendsViewModel


class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val tools = Tools()

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)

        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val uuidView: TextView = binding.textUuid
        friendsViewModel.getUser().observe(viewLifecycleOwner) { user ->
            uuidView.text = "Your UUID : " + user.getId()
        }

        val inputNameText : EditText = binding.inputName

        val inputUuidText : EditText = binding.inputUuid
        inputUuidText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val name = inputNameText.text.toString()
                val uuid = inputUuidText.text.toString()
                if (name.isNotEmpty() && uuid.isNotEmpty()) {
                    friendsViewModel.addFriendToUser(name, uuid)
                    tools.hideKeyboard(context, binding.inputUuid)
                    inputNameText.text.clear()
                    inputUuidText.text.clear()
                }
                true
            } else {
                false
            }
        }

        val isWithFriendsView: TextView = binding.isWithFriends
        friendsViewModel.getUser().observe(viewLifecycleOwner) { user ->
            if (user.getState() == State.SOCIALLY_ENGAGED) {
                isWithFriendsView.text = "You are with your friends"
            } else {
                isWithFriendsView.text = "You are not with your friends"
            }
        }

        val friendListView: ListView = binding.listFriend
        friendListView.setOnItemClickListener { parent, view, position, id ->
            showFriendDialog(friendsViewModel.getUser().value!!.getFriendList()[position],friendsViewModel)
        }
        val adapter = FriendsAdapter(requireContext(), mutableListOf())
        friendListView.adapter = adapter
        friendsViewModel.getUser().observe(viewLifecycleOwner) { user ->
            adapter.clear()
            adapter.addAll(user.getFriendList())
            adapter.notifyDataSetChanged()
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showFriendDialog(friend : Friend?, friendsViewModel: FriendsViewModel){
        if (friend != null) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder
                .setTitle("Friend name : " + friend.name)
                .setPositiveButton("Cancel") { dialog, which ->
                }
                .setNegativeButton("Delete Friends") { dialog, which ->
                    friendsViewModel.removeFriendFromUser(friend)
                }
                .setItems(arrayOf("UUID : " + friend.uuid)) { _, _ ->
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }


}