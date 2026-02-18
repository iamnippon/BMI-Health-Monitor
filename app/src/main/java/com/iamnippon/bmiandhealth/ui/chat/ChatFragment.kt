package com.iamnippon.bmiandhealth.ui.chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.FragmentChatBinding
import com.iamnippon.bmiandhealth.viewmodel.ChatViewModel

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var adapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChatBinding.bind(view)


        setupRecycler()
        setupListeners()
        observeViewModel()
        showDisclaimer()
        setHasOptionsMenu(true)

    }

    private fun setupRecycler() {

        adapter = ChatAdapter(requireContext(), mutableListOf())

        binding.recyclerChat.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        binding.recyclerChat.adapter = adapter
    }



    private fun setupListeners() {
        binding.btnSend.setOnClickListener {

            val userText = binding.etMessage.text.toString().trim()
            if (userText.isEmpty()) return@setOnClickListener

            // Show user message
            viewModel.sendMessageToAI(userText)

            binding.etMessage.setText("")

            // Send to AI
        }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {

                val userText = binding.etMessage.text.toString().trim()
                if (userText.isNotEmpty()) {
                    viewModel.sendMessageToAI(userText)
                    binding.etMessage.setText("")
                }

                true
            } else {
                false
            }
        }

    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { list ->
            adapter.updateMessages(list)
            binding.recyclerChat.post {
                if (adapter.itemCount > 0) {
                    binding.recyclerChat.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }

        }
    }




    private fun showDisclaimer() {
        if (viewModel.messages.value.isNullOrEmpty()) {
            viewModel.addMessage(
                ChatMessage(
                    "Hi 👋 I provide general health information only and not medical advice.",
                    false
                )
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_chat -> {
                viewModel.clearChat()
                showDisclaimer()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }




}
