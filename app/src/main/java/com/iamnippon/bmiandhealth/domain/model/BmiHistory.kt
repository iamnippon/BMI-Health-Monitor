package com.iamnippon.bmiandhealth.domain.model

data class BmiHistory(
    val bmi: Float,
    val category: String,
    val heightCm: Int,
    val weightKg: Int,
    val gender: Gender,
    val timestamp: Long
)
