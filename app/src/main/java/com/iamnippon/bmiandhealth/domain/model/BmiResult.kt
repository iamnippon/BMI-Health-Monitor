package com.iamnippon.bmiandhealth.domain.model

data class BmiResult(
    val bmi: Float,
    val category: String,
    val stage: String?,
    val advice: String,
    val heightCm: Int,
    val weightKg: Float,
    val gender: Gender,
    val timestamp: Long
)

