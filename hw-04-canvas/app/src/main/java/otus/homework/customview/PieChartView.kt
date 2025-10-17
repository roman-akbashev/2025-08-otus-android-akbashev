package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var expenses: List<Expense> = emptyList()

    private var pieSlices: List<PieSlice> = emptyList()

    private var totalAmount: Float = 0f

    private val colors = listOf(
        ContextCompat.getColor(context, R.color.red_1),
        ContextCompat.getColor(context, R.color.orange_1),
        ContextCompat.getColor(context, R.color.orange_2),
        ContextCompat.getColor(context, R.color.yellow_1),
        ContextCompat.getColor(context, R.color.green_1),
        ContextCompat.getColor(context, R.color.green_2),
        ContextCompat.getColor(context, R.color.green_3),
        ContextCompat.getColor(context, R.color.blue_2),
        ContextCompat.getColor(context, R.color.blue_3),
        ContextCompat.getColor(context, R.color.purple_1)
    )

    private val piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private val rectangle = RectF()

    private var ringWidth = 75f

    private var selectedCategory: String? = null

    private val minSize = 300.dpToPx()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = minSize
        val desiredHeight = minSize

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pieSlices.isEmpty()) return

        val minDim = width.coerceAtMost(height)
        val radius = minDim * 0.39f
        ringWidth = minDim * 0.18f
        piePaint.strokeWidth = ringWidth

        rectangle.set(
            width / 2f - radius,
            height / 2f - radius,
            width / 2f + radius,
            height / 2f + radius
        )

        drawSlices(canvas)
        drawPercent(radius, canvas)
        drawText(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val centerX = width / 2f
            val centerY = height / 2f

            val dx = event.x - centerX
            val dy = event.y - centerY

            val dist = sqrt(dx * dx + dy * dy)

            val minDim = width.coerceAtMost(height)
            val radius = minDim * 0.39f

            val innerRadius = radius - ringWidth / 2
            val outerRadius = radius + ringWidth / 2

            if (dist in innerRadius..outerRadius) {
                var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (touchAngle < 0) touchAngle += 360f
                val slice = pieSlices.firstOrNull { slice ->
                    val normalizedStartAngle =
                        if (slice.startAngle < 0) slice.startAngle + 360f else slice.startAngle
                    val endAngle = normalizedStartAngle + slice.sweepAngle
                    if (endAngle > 360f) {
                        touchAngle >= normalizedStartAngle || touchAngle <= (endAngle - 360f)
                    } else {
                        touchAngle >= normalizedStartAngle && touchAngle <= endAngle
                    }
                }
                slice?.let {
                    selectedCategory = it.category
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("super", super.onSaveInstanceState())
            putString("selectedCategory", selectedCategory)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? Bundle)?.let {
            selectedCategory = it.getString("selectedCategory", null)
            super.onRestoreInstanceState(it.getParcelable("super"))
        }
    }

    fun setData(data: List<Expense>) {
        this.expenses = data
        totalAmount = data.sumOf { it.amount }.toFloat()
        prepareSlices()
        invalidate()
    }

    private fun drawSlices(canvas: Canvas) {
        for (slice in pieSlices) {
            piePaint.color = slice.color
            piePaint.strokeWidth =
                if (slice.category == selectedCategory) ringWidth * 1.2f else ringWidth
            canvas.drawArc(
                rectangle,
                slice.startAngle,
                slice.sweepAngle - 1,
                false, piePaint
            )
        }
    }

    private fun drawText(canvas: Canvas) {
        selectedCategory?.let {
            canvas.drawText(it, width / 2f, height / 2f - 10f, textPaint)
        }
    }

    private fun drawPercent(radius: Float, canvas: Canvas) {
        for (slice in pieSlices) {
            val angleRad = (slice.startAngle + slice.sweepAngle / 2) * (PI.toFloat() / 180f)
            val textRadius = radius
            val labelX = width / 2f + textRadius * cos(angleRad)
            val labelY = height / 2f + textRadius * sin(angleRad) + textPaint.textSize / 2.6f

            if (slice.sweepAngle >= 15f) {
                canvas.drawText(slice.percentLabel, labelX, labelY, textPaint)
            }
        }
    }

    private fun prepareSlices() {
        val grouped = expenses.groupBy { it.category }
        val list = mutableListOf<PieSlice>()

        var startAngle = -90f
        var colorIdx = 0

        for ((category, items) in grouped) {
            val value = items.sumOf { it.amount }.toFloat()
            val percent = if (totalAmount == 0f) 0f else value / totalAmount
            val percentLabel = "${(percent * 100).roundToInt()}%"
            val sweep = percent * 360f
            val color = colors[colorIdx.rem(colors.size)]
            list.add(PieSlice(category, value, percent, percentLabel, startAngle, sweep, color))
            startAngle += sweep
            colorIdx++
        }
        pieSlices = list
    }

    private data class PieSlice(
        val category: String,
        val value: Float,
        val percent: Float,
        val percentLabel: String,
        val startAngle: Float,
        val sweepAngle: Float,
        val color: Int
    )

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}