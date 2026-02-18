package com.iamnippon.bmiandhealth.util

import android.os.Bundle
import androidx.navigation.NavController

fun NavController.safeNavigate(resId: Int) {
    val action = currentDestination?.getAction(resId)
    if (action != null) {
        navigate(resId)
    }
}

fun NavController.safeNavigate(resId: Int, args: Bundle) {
    val action = currentDestination?.getAction(resId)
    if (action != null) {
        navigate(resId, args)
    }
}
