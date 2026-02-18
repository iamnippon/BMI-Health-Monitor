package com.iamnippon.bmiandhealth.domain.usecase

import com.iamnippon.bmiandhealth.domain.model.BmiResult
import com.iamnippon.bmiandhealth.domain.model.Gender
import kotlin.math.roundToInt

class CalculateBmiUseCase {

    fun execute(
        heightCm: Int,
        weightKg: Float,
        gender: Gender
    ): BmiResult {

        val heightMeter = heightCm / 100f
        val bmiRaw = weightKg / (heightMeter * heightMeter)
        val bmi = ((bmiRaw * 10).roundToInt()) / 10f

        return when {
            bmi < 18.5 -> BmiResult(
                bmi = bmi,
                category = "Underweight",
                stage = null,
                advice = "Increase calorie intake with nutritious food.",
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                timestamp = System.currentTimeMillis()
            )

            bmi < 25 -> BmiResult(
                bmi = bmi,
                category = "Normal",
                stage = null,
                advice = "Maintain your healthy lifestyle.",
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                timestamp = System.currentTimeMillis()
            )

            bmi < 30 -> BmiResult(
                bmi = bmi,
                category = "Overweight",
                stage = null,
                advice = "Exercise regularly and manage your diet.",
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                timestamp = System.currentTimeMillis()
            )

            bmi < 35 -> BmiResult(
                bmi = bmi,
                category = "Obese",
                stage = "Class I",
                advice = "Lifestyle changes strongly recommended.",
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                timestamp = System.currentTimeMillis()
            )

            bmi < 40 -> BmiResult(
                bmi = bmi,
                category = "Obese",
                stage = "Class II",
                advice = "Medical guidance advised.",
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                timestamp = System.currentTimeMillis()
            )

            else -> BmiResult(
                bmi = bmi,
                category = "Obese",
                stage = "Class III (Severe)",
                advice = "Seek professional medical supervision.",
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
