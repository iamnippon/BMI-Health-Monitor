package com.iamnippon.bmiandhealth.ui.result

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.FragmentResultBinding
import com.iamnippon.bmiandhealth.domain.usecase.CalculateBmiUseCase
import com.iamnippon.bmiandhealth.viewmodel.ChatViewModel
import com.iamnippon.bmiandhealth.viewmodel.InputViewModel
import com.iamnippon.bmiandhealth.viewmodel.ResultViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ResultFragment : Fragment(R.layout.fragment_result) {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val inputViewModel: InputViewModel by activityViewModels()
    private val viewModel: ResultViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentResultBinding.bind(view)

        // -----------------------------
        // 1️⃣ Get Fresh Data From InputViewModel
        // -----------------------------

        val chatViewModel =
            ViewModelProvider(requireActivity())[ChatViewModel::class.java]

        val currentState = inputViewModel.uiState.value

        val inputState = inputViewModel.uiState.value

        val heightCm = (inputState.heightMeters * 100).toInt()
        val weightKg = inputState.weightKg
        val gender = inputState.gender

        chatViewModel.updateFromInputState(currentState)

        // If BMI not calculated yet → stop
        if (heightCm <= 0 || weightKg <= 0f) return

        // Always calculate fresh result
        viewModel.reset()
        viewModel.calculate(heightCm, weightKg, gender)

        // -----------------------------
        // 2️⃣ Observe UI State
        // -----------------------------
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->

                val colorRes = when (state.category) {
                    "Underweight" -> R.color.secondary
                    "Normal" -> R.color.success
                    "Overweight" -> R.color.warning
                    "Obese" -> R.color.danger
                    else -> R.color.text_primary
                }

                chatViewModel.updateFromResult(
                    weight = weightKg,
                    heightMeters = inputState.heightMeters,
                    age = inputState.age,
                    gender = inputState.gender.name,
                    bmi = state.bmi
                )


                val color = requireContext().getColor(colorRes)

                binding.tvBmiValue.setTextColor(color)
                binding.tvCategory.setTextColor(color)
                binding.cardBmi.strokeColor = color

                binding.tvCategory.text = state.category
                binding.tvAdvice.text = state.advice
                binding.tvStage.text = state.stage

                binding.tvStage.visibility =
                    if (state.stage.isNotEmpty()) View.VISIBLE else View.GONE

                animateBmiValue(state.bmi)

                binding.bmiGauge.setBmi(
                    bmi = state.bmi,
                    color = color
                )

                // Highlight correct table row
                resetRows()

                when (state.category) {
                    "Underweight" -> highlightRow(binding.rowUnderweight, color)
                    "Normal" -> highlightRow(binding.rowNormal, color)
                    "Overweight" -> highlightRow(binding.rowOverweight, color)
                    "Obese" -> highlightRow(binding.rowObese, color)
                }
            }
        }

        // -----------------------------
        // 3️⃣ Recalculate Button
        // -----------------------------
        binding.btnRecalculate.setOnClickListener {

            inputViewModel.clearBmiOnly()

            val navController = findNavController()

            val cameFromHistory = arguments?.containsKey("bmi_result_json") == true

            if (cameFromHistory) {
                // If opened from History → go directly to Calculator
                navController.navigate(R.id.inputFragment)
            } else {
                // If opened from Calculator → normal back
                navController.popBackStack()
            }
        }

    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private fun animateBmiValue(targetBmi: Float) {
        val animator = ValueAnimator.ofFloat(0f, targetBmi).apply {
            duration = 800
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                binding.tvBmiValue.text = String.format("%.1f", value)
            }
        }
        animator.start()
    }

    private fun highlightRow(row: View, color: Int) {
        val tinted = ColorUtils.setAlphaComponent(color, 40)
        row.setBackgroundColor(tinted)
    }

    private fun resetRows() {
        binding.rowUnderweight.setBackgroundColor(Color.TRANSPARENT)
        binding.rowNormal.setBackgroundColor(Color.TRANSPARENT)
        binding.rowOverweight.setBackgroundColor(Color.TRANSPARENT)
        binding.rowObese.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
