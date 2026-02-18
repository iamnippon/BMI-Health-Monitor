package com.iamnippon.bmiandhealth.ui.history

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.FragmentHistoryBinding
import com.iamnippon.bmiandhealth.domain.model.BmiResult
import com.iamnippon.bmiandhealth.util.safeNavigate
import com.iamnippon.bmiandhealth.viewmodel.HistoryViewModel
import kotlinx.coroutines.flow.collectLatest

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    private lateinit var adapter: HistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHistoryBinding.bind(view)
        setHasOptionsMenu(true)

        adapter = HistoryAdapter(
            onItemClick = { item ->
                val json = Gson().toJson(item)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {

                    val bundle = Bundle().apply {
                        putString("bmi_result_json", json)
                    }

                    findNavController().safeNavigate(
                        R.id.action_historyFragment_to_resultFragment,
                        bundle
                    )
                }


            },
            onDeleteClick = { item ->
                viewModel.deleteItem(item)
            }
        )

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.history.collectLatest { list ->
                if (list.isEmpty()) {
                    showEmptyState()
                } else {
                    showHistory(list)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.rvHistory.visibility = View.GONE
        adapter.submitList(emptyList())
    }

    private fun showHistory(list: List<BmiResult>) {
        binding.layoutEmpty.visibility = View.GONE
        binding.rvHistory.visibility = View.VISIBLE
        adapter.submitList(list)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("HISTORY_DEBUG", "Menu clicked: ${item.itemId}")

        return when (item.itemId) {
            R.id.action_clear_history -> {
                Log.d("HISTORY_DEBUG", "Clear history selected")
                showClearConfirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showClearConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear history")
            .setMessage("This will permanently delete all BMI records.")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearAll()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
