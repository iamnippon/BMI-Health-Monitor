package com.iamnippon.bmiandhealth.ui.diet


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.FragmentDietBinding
import com.iamnippon.bmiandhealth.domain.model.BmiResult
import com.iamnippon.bmiandhealth.domain.usecase.DietPlan
import com.iamnippon.bmiandhealth.domain.usecase.SmartDietGenerator
import com.iamnippon.bmiandhealth.viewmodel.InputViewModel
import kotlinx.coroutines.launch

class DietFragment : Fragment(R.layout.fragment_diet) {

    private var _binding: FragmentDietBinding? = null
    private val binding get() = _binding!!
    private val inputViewModel: InputViewModel by activityViewModels()
    private val generator = SmartDietGenerator()
    private val viewModel: InputViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentDietBinding.bind(view)
        observeState()
        setupCategoryClicks()

    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    val bmi = state.bmi

                    if (bmi == null) {
                        showNoBmiState()
                        return@collect
                    } else {
                        showPlanState()
                    }
                    val age = state.age ?: return@collect
                    if (age <= 0) return@collect

                    val heightCm = (state.heightMeters * 100).toInt()

                    val bmiResult = BmiResult(
                        bmi = bmi,
                        category = "",
                        stage = null,
                        advice = "",
                        heightCm = heightCm,
                        weightKg = state.weightKg,
                        gender = state.gender,
                        timestamp = System.currentTimeMillis()
                    )

                    val plan = generator.generate(
                        bmiResult = bmiResult,
                        age = age
                    )

                    val colorRes = when {
                        bmi < 18.5 -> R.color.secondary
                        bmi < 25 -> R.color.success
                        bmi < 30 -> R.color.warning
                        else -> R.color.danger
                    }

                    binding.cardSummary.strokeColor =
                        requireContext().getColor(colorRes)


                    bindPlan(plan)
                    scrollToCategory(bmi)
                }
            }
        }
    }
    // ✅ This was missing

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun scrollToCategory(bmi: Float) {

        if (!isAdded || _binding == null) return

        val targetCard = when {
            bmi < 18.5 -> binding.cardUnderweight
            bmi < 25 -> binding.cardNormal
            bmi < 30 -> binding.cardOverweight
            bmi < 35 -> binding.cardObese1
            bmi < 40 -> binding.cardObese2
            else -> binding.cardObese3
        }

        resetCardHighlights()

        targetCard.strokeWidth = 4
        targetCard.strokeColor = requireContext().getColor(R.color.primary)

        binding.scrollDiet.post {
            safeUi {
                binding.scrollDiet.smoothScrollTo(0, targetCard.top)
            }
        }
    }


    private fun resetCardHighlights() {
        val cards = listOf(
            binding.cardUnderweight,
            binding.cardNormal,
            binding.cardOverweight,
            binding.cardObese1,
            binding.cardObese2,
            binding.cardObese3
        )

        cards.forEach {
            it.strokeWidth = 0
        }
    }

    private fun bindPlan(plan: DietPlan) {

        binding.tvCalories.text =
            "🔥 Daily Target: ${plan.dailyCalories} kcal"

        binding.tvProtein.text =
            "💪 Protein: ${plan.proteinGrams} g"

        binding.tvCarbs.text =
            "🍞 Carbohydrates: ${plan.carbsGrams} g"

        binding.tvFat.text =
            "🥑 Fats: ${plan.fatGrams} g"

        binding.tvMeal1.text = "• ${plan.mealPlan[0]}"
        binding.tvMeal2.text = "• ${plan.mealPlan[1]}"
        binding.tvMeal3.text = "• ${plan.mealPlan[2]}"
        binding.tvMeal4.text = "• ${plan.mealPlan[3]}"
    }


    private fun setupCategoryClicks() {

        val categories = listOf(
            binding.cardUnderweight,
            binding.cardNormal,
            binding.cardOverweight,
            binding.cardObese1,
            binding.cardObese2,
            binding.cardObese3
        )

        categories.forEach { card ->
            card.setOnClickListener {
                scrollToTop()
                highlightSummaryCard()
            }
        }
    }
    private fun scrollToTop() {
        if (!isAdded || _binding == null) return

        binding.scrollDiet.post {
            if (!isAdded || _binding == null) return@post
            binding.scrollDiet.smoothScrollTo(0, 0)
        }
    }


    private fun highlightSummaryCard() {
        binding.cardSummary.strokeWidth = 4
        binding.cardSummary.strokeColor =
            requireContext().getColor(R.color.primary)

        binding.cardSummary.postDelayed({
            if (!isAdded || _binding == null) return@postDelayed
            binding.cardSummary.strokeWidth = 0
        }, 1500)
    }

    private fun showNoBmiState() {
        binding.tvNoBmiMessage.visibility = View.VISIBLE
        binding.cardSummary.visibility = View.GONE
    }

    private fun showPlanState() {
        binding.tvNoBmiMessage.visibility = View.GONE
        binding.cardSummary.visibility = View.VISIBLE
    }



    private inline fun safeUi(block: () -> Unit) {
        if (view != null && isAdded && _binding != null) {
            block()
        }
    }



}
