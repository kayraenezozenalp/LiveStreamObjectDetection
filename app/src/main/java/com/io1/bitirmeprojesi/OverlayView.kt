package com.io1.bitirmeprojesi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

   /* private val backgroundPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    */

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 30f
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val boxes = mutableListOf<Triple<String, RectF, String>>()

    fun setBoxes(newBoxes: List<Triple<String, RectF,String>>) {
        boxes.clear()
        boxes.addAll(newBoxes)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((text, rect, confidence) in boxes) {
            canvas.drawRect(rect, paint)
            val textX = rect.left
            val textY = rect.top - textPaint.descent()
            val confidencePercentage = String.format("%.2f", confidence.toDouble() * 100)
            canvas.drawText("$text ($confidencePercentage%)", textX, textY, textPaint)
        }
    }
}
