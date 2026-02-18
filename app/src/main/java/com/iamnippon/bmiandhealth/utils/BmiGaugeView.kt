package com.iamnippon.bmiandhealth.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.iamnippon.bmiandhealth.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class BmiGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // BMI range
    private val minBmi = 16f
    private val maxBmi = 40f

    private var animatedBmi = minBmi

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 22f
        color = ContextCompat.getColor(context, R.color.text_secondary)
    }

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 22f
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // Public API
    fun setBmi(bmi: Float, color: Int) {
        val target = bmi.coerceIn(minBmi, maxBmi)

        ValueAnimator.ofFloat(animatedBmi, target).apply {
            duration = 900
            addUpdateListener {
                animatedBmi = it.animatedValue as Float
                invalidate()
            }
            start()
        }

        segmentPaint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height)
        val radius = size / 2f - backgroundPaint.strokeWidth
        val centerX = width / 2f
        val centerY = height / 2f + radius / 2f

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Background arc (full semi-circle)
        canvas.drawArc(rect, 180f, 180f, false, backgroundPaint)

        // BMI colored segments
        drawSegment(canvas, rect, 16f, 18.5f, R.color.secondary)
        drawSegment(canvas, rect, 18.5f, 25f, R.color.success)
        drawSegment(canvas, rect, 25f, 30f, R.color.warning)
        drawSegment(canvas, rect, 30f, 40f, R.color.danger)

        // Indicator
        drawIndicator(canvas, rect)
    }

    private fun drawSegment(
        canvas: Canvas,
        rect: RectF,
        startBmi: Float,
        endBmi: Float,
        colorRes: Int
    ) {
        val startSweep = bmiToSweep(startBmi)
        val sweep = bmiToSweep(endBmi) - startSweep

        segmentPaint.color = ContextCompat.getColor(context, colorRes)

        canvas.drawArc(
            rect,
            180f + startSweep,
            sweep,
            false,
            segmentPaint
        )
    }

    private fun drawIndicator(canvas: Canvas, rect: RectF) {
        val sweep = bmiToSweep(animatedBmi)
        val angle = Math.toRadians((180 + sweep).toDouble())

        val radius = rect.width() / 2
        val cx = rect.centerX() + radius * cos(angle)
        val cy = rect.centerY() + radius * sin(angle)

        canvas.drawCircle(cx.toFloat(), cy.toFloat(), 10f, indicatorPaint)
    }

    private fun bmiToSweep(bmi: Float): Float {
        val clamped = bmi.coerceIn(minBmi, maxBmi)
        return 180f * (clamped - minBmi) / (maxBmi - minBmi)
    }
}
