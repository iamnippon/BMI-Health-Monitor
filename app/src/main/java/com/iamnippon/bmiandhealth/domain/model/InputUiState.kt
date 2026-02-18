package com.iamnippon.bmiandhealth.domain.model

enum class Gender { MALE, FEMALE, UNKNOWN }

data class InputUiState(
    val weightKg: Float = 0f,
    val heightMeters: Float = 0f,
    val age: Int? = 24,
    val gender: Gender = Gender.FEMALE,
    val heightUnit: HeightUnit = HeightUnit.CM,
    val bmi: Float? = null
)

