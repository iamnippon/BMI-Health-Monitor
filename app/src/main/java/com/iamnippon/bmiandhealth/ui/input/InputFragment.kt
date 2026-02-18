package com.iamnippon.bmiandhealth.ui.input

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.FragmentInputBinding
import com.iamnippon.bmiandhealth.domain.model.Gender
import com.iamnippon.bmiandhealth.domain.model.HeightUnit
import com.iamnippon.bmiandhealth.domain.model.InputUiState
import com.iamnippon.bmiandhealth.util.safeNavigate
import com.iamnippon.bmiandhealth.viewmodel.InputViewModel
import kotlinx.coroutines.launch

class InputFragment : Fragment(R.layout.fragment_input) {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!

    // IMPORTANT: shared ViewModel
    private val viewModel: InputViewModel by activityViewModels()

    private var isKgUnit = true
    private var lastHeightUnit: HeightUnit? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentInputBinding.bind(view)

        setupHeightDropdown()
        setupListeners()
        observeState()

        val units = listOf(
            "Centimeter (cm)",
            "Meter (m)",
            "Foot & Inch"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            units
        )

        binding.dropHeightUnit.apply {
            setAdapter(adapter)
            threshold = 100 // disables auto filtering
        }

    }



    // --------------------------------------------------
    // HEIGHT DROPDOWN SETUP
    // --------------------------------------------------

    private fun setupHeightDropdown() {

        val units = listOf("Centimeter (cm)", "Meter (m)", "Feet & Inch")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            units
        )

        binding.dropHeightUnit.setAdapter(adapter)

        // Always allow dropdown to open
        binding.dropHeightUnit.setOnClickListener {
            binding.dropHeightUnit.showDropDown()
        }

        binding.dropHeightUnit.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> viewModel.setHeightUnit(HeightUnit.CM)
                1 -> viewModel.setHeightUnit(HeightUnit.METER)
                2 -> viewModel.setHeightUnit(HeightUnit.FEET)
            }
        }
    }

    // --------------------------------------------------
    // UI LISTENERS
    // --------------------------------------------------

    private fun setupListeners() = binding.apply {

        toggleWeightUnit.check(R.id.btnKg)
        isKgUnit = true

        // -------------------------
        // Weight Handling
        // -------------------------
        etWeight.addTextChangedListener {
            val input = it?.toString()?.toFloatOrNull() ?: return@addTextChangedListener
            val weightKg = if (isKgUnit) input else input / 2.20462f
            viewModel.setWeight(weightKg)
        }

        toggleWeightUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val currentKg = viewModel.uiState.value.weightKg

            when (checkedId) {
                R.id.btnKg -> {
                    isKgUnit = true
                    etWeight.setText(String.format("%.1f", currentKg))
                }

                R.id.btnLb -> {
                    isKgUnit = false
                    etWeight.setText(String.format("%.1f", currentKg * 2.20462f))
                }
            }
        }

        // -------------------------
        // Height Handling (CORRECT IDS)
        // -------------------------

        // CM
        etHeightCm.addTextChangedListener {
            val cm = it?.toString()?.toFloatOrNull() ?: return@addTextChangedListener
            viewModel.setHeightCm(cm)
        }

        // Meter
        etHeightMeter.addTextChangedListener {
            val meter = it?.toString()?.toFloatOrNull() ?: return@addTextChangedListener
            viewModel.setHeightMeter(meter)
        }

        // Feet + Inch
        etHeightFeet.addTextChangedListener { updateFeetInchHeight() }
        etHeightInch.addTextChangedListener { updateFeetInchHeight() }

        // -------------------------
        // Age
        // -------------------------
        etAge.addTextChangedListener {
            val age = it?.toString()?.toIntOrNull() ?: return@addTextChangedListener
            viewModel.setAge(age)
        }

        btnMale.setOnClickListener { viewModel.setGender(Gender.MALE) }
        btnFemale.setOnClickListener { viewModel.setGender(Gender.FEMALE) }

        btnCalculate.setOnClickListener {

            // 🔒 Step 1: Validate first
            if (!validateInputs()) return@setOnClickListener

            // 🔄 Step 2: Sync valid values to ViewModel

            val weightInput = binding.etWeight.text.toString().toFloatOrNull()!!
            val weightKg = if (isKgUnit) weightInput else weightInput / 2.20462f
            viewModel.setWeight(weightKg)

            val ageInput = binding.etAge.text.toString().toIntOrNull()!!
            viewModel.setAge(ageInput)

            when (viewModel.uiState.value.heightUnit) {

                HeightUnit.CM -> {
                    val cm = binding.etHeightCm.text.toString().toFloatOrNull()!!
                    viewModel.setHeightCm(cm)
                }

                HeightUnit.METER -> {
                    val meter = binding.etHeightMeter.text.toString().toFloatOrNull()!!
                    viewModel.setHeightMeter(meter)
                }

                HeightUnit.FEET -> {
                    val foot = binding.etHeightFeet.text.toString().toFloatOrNull() ?: 0f
                    val inch = binding.etHeightInch.text.toString().toFloatOrNull() ?: 0f
                    viewModel.setHeightFootInch(foot, inch)
                }
            }

            // 🧮 Step 3: Calculate
            viewModel.onCalculateClicked()

            // 🚀 Step 4: Navigate
            val extras = FragmentNavigatorExtras(
                binding.tvBmiPreview to "bmi_shared"
            )

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {

                it.isEnabled = false

                findNavController().safeNavigate(
                    R.id.action_inputFragment_to_resultFragment
                )

                binding.btnCalculate.postDelayed({
                    it.isEnabled = true
                }, 500)
            }

        }

    }


        // --------------------------------------------------
    // OBSERVE STATE
    // --------------------------------------------------

    private fun observeState() {

        val units = listOf("Centimeter (cm)", "Meter (m)", "Feet & Inch")

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state: InputUiState ->

                    // Age sync
                    if (binding.etAge.text.toString() != state.age.toString()) {
                        binding.etAge.setText(state.age.toString())
                    }

                    // Gender visual
                    binding.btnMale.isSelected = (state.gender == Gender.MALE)
                    binding.btnFemale.isSelected = (state.gender == Gender.FEMALE)

                    updateAvatar(state.gender)   // 🎭 update animation
                    updateGenderStyle()

                    // Height unit UI sync (IMPORTANT FIX)
                    // Height unit UI sync (only when changed)
                    if (lastHeightUnit != state.heightUnit) {

                        lastHeightUnit = state.heightUnit

                        when (state.heightUnit) {
                            HeightUnit.CM -> {
                                binding.dropHeightUnit.setText(units[0], false)
                                showHeightLayout("cm")
                            }
                            HeightUnit.METER -> {
                                binding.dropHeightUnit.setText(units[1], false)
                                showHeightLayout("m")
                            }
                            HeightUnit.FEET -> {
                                binding.dropHeightUnit.setText(units[2], false)
                                showHeightLayout("ft")
                            }
                        }
                    }

                }
            }
        }
    }

    // --------------------------------------------------
    // NAVIGATION
    // --------------------------------------------------



    // --------------------------------------------------
    // UI HELPERS
    // --------------------------------------------------

    private fun updateGenderStyle() {

        val selectedBg = requireContext().getColor(R.color.primary)
        val unselectedBg = requireContext().getColor(R.color.card_surface)

        val selectedText = MaterialColors.getColor(
            binding.btnMale,
            com.google.android.material.R.attr.colorOnPrimary
        )

        val unselectedText = MaterialColors.getColor(
            binding.btnMale,
            com.google.android.material.R.attr.colorOnSurface
        )

        // MALE
        binding.btnMale.apply {
            if (isSelected) {
                setBackgroundColor(selectedBg)
                setTextColor(selectedText)
            } else {
                setBackgroundColor(unselectedBg)
                setTextColor(unselectedText)
            }
        }

        // FEMALE
        binding.btnFemale.apply {
            if (isSelected) {
                setBackgroundColor(selectedBg)
                setTextColor(selectedText)
            } else {
                setBackgroundColor(unselectedBg)
                setTextColor(unselectedText)
            }
        }
    }


    private fun showHeightLayout(type: String) {
        binding.layoutCm.visibility = View.GONE
        binding.layoutMeter.visibility = View.GONE
        binding.layoutFtIn.visibility = View.GONE

        when (type) {
            "cm" -> binding.layoutCm.visibility = View.VISIBLE
            "m" -> binding.layoutMeter.visibility = View.VISIBLE
            "ft" -> binding.layoutFtIn.visibility = View.VISIBLE
        }
    }

    private fun updateAvatar(gender: Gender) {

        val fileName = when (gender) {
            Gender.MALE -> "male_avatar.json"
            Gender.FEMALE -> "female_avatar.json"
            else -> "male_avatar.json"
        }

        binding.avatarView.apply {
            cancelAnimation()
            setAnimation(fileName)
            playAnimation()
        }
    }

    private fun updateFeetInchHeight() {
        val foot = binding.etHeightFeet.text.toString().toFloatOrNull() ?: 0f
        val inch = binding.etHeightInch.text.toString().toFloatOrNull() ?: 0f
        viewModel.setHeightFootInch(foot, inch)
    }

    private fun validateInputs(): Boolean {

        var isValid = true

        val weight = binding.etWeight.text.toString().toFloatOrNull()
        if (weight == null || weight <= 0f) {
            binding.tilWeight.error = "Enter valid weight"
            isValid = false
        } else {
            binding.tilWeight.error = null
        }

        val age = binding.etAge.text.toString().toIntOrNull()
        if (age == null || age <= 0) {
            binding.etAge.error = "Enter valid age"
            isValid = false
        } else {
            binding.etAge.error = null
        }

        val heightValid = when (viewModel.uiState.value.heightUnit) {

            HeightUnit.CM -> {
                val cm = binding.etHeightCm.text.toString().toFloatOrNull()
                cm != null && cm > 0f
            }

            HeightUnit.METER -> {
                val meter = binding.etHeightMeter.text.toString().toFloatOrNull()
                meter != null && meter > 0f
            }

            HeightUnit.FEET -> {
                val feet = binding.etHeightFeet.text.toString().toFloatOrNull() ?: 0f
                val inch = binding.etHeightInch.text.toString().toFloatOrNull() ?: 0f
                (feet > 0f || inch > 0f)
            }
        }

        if (!heightValid) {
            isValid = false
        }

        return isValid
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
