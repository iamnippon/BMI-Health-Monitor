package com.iamnippon.bmiandhealth.domain.usecase


import com.iamnippon.bmiandhealth.domain.model.BmiResult
import com.iamnippon.bmiandhealth.domain.model.Gender
import kotlin.math.roundToInt

data class DietPlan(
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val mealPlan: List<String>
)

class SmartDietGenerator {

    fun generate(
        bmiResult: BmiResult,
        age: Int
    ): DietPlan {

        val weightKg = bmiResult.weightKg
        val heightCm = bmiResult.heightCm.toFloat()
        val bmi = bmiResult.bmi
        val gender = bmiResult.gender

        // ----------------------------
        // 1️⃣ BMR (Mifflin-St Jeor)
        // ----------------------------
        val bmr = when (gender) {
            Gender.MALE ->
                (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5

            Gender.FEMALE ->
                (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161

            Gender.UNKNOWN ->
                (10 * weightKg) + (6.25 * heightCm) - (5 * age)
        }

        // Light activity multiplier
        val tdee = bmr * 1.4

        // ----------------------------
        // 2️⃣ Calorie Target Adjustment
        // ----------------------------
        val calorieTarget = when {
            bmi < 18.5 -> tdee + 300      // Surplus
            bmi < 25 -> tdee              // Maintain
            bmi < 30 -> tdee - 300        // Mild deficit
            bmi < 35 -> tdee - 500        // Moderate deficit
            bmi < 40 -> tdee - 600
            else -> tdee - 700
        }

        // ----------------------------
        // 3️⃣ Macro Distribution
        // ----------------------------

        val proteinPerKg = when {
            bmi < 18.5 -> 1.4
            bmi < 25 -> 1.1
            bmi < 30 -> 1.4
            else -> 1.8
        }

        val protein = (weightKg * proteinPerKg).roundToInt()

        val fat = (calorieTarget * 0.25 / 9).roundToInt()

        val carbs = (
                (calorieTarget - (protein * 4) - (fat * 9)) / 4
                ).roundToInt()

        // ----------------------------
        // 4️⃣ Generate Meals
        // ----------------------------
        val meals = generateMealPlan(bmi)

        return DietPlan(
            dailyCalories = calorieTarget.roundToInt(),
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            mealPlan = meals
        )
    }

    // ---------------------------------------
    // CATEGORY BASED MEAL STRUCTURE
    // ---------------------------------------
    private fun generateMealPlan(bmi: Float): List<String> {

        return when {

            bmi < 18.5 -> listOf(
                "Breakfast: Oats + Peanut Butter + 2 Eggs",
                "Lunch: Rice + Chicken + Vegetables",
                "Snack: Banana + Yogurt",
                "Dinner: Fish + Sweet Potato"
            )

            bmi < 25 -> listOf(
                "Breakfast: Oats + Eggs",
                "Lunch: Brown Rice + Grilled Chicken",
                "Snack: Nuts + Fruit",
                "Dinner: Fish + Vegetables"
            )

            bmi < 30 -> listOf(
                "Breakfast: Egg Whites + Oats",
                "Lunch: Grilled Chicken + Vegetables",
                "Snack: Apple + Almonds",
                "Dinner: Fish + Salad"
            )

            else -> listOf(
                "Breakfast: Egg Whites + Vegetables",
                "Lunch: Grilled Fish + Large Salad",
                "Snack: Greek Yogurt",
                "Dinner: Lean Protein + Steamed Vegetables"
            )
        }
    }
}
