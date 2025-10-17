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

// Кастомная View для отображения круговой диаграммы (pie chart)
class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Исходные данные для отображения
    private var expenses: List<Expense> = emptyList()

    // Подготовленные сегменты диаграммы
    private var pieSlices: List<PieSlice> = emptyList()

    // Общая сумма всех значений
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

    // Кисть для рисования сегментов диаграммы
    private val piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE  // Стиль - только обводка (для кольцевой диаграммы)
        strokeCap = Paint.Cap.BUTT // Стиль концов линий
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER  // Выравнивание по центру
    }

    // Прямоугольник для ограничения области рисования дуг
    private val rectangle = RectF()

    // Ширина кольца диаграммы
    private var ringWidth = 75f

    // Выбранная категория (для highlight)
    private var selectedCategory: String? = null

    // Минимальный размер View
    private val minSize = 300.dpToPx()

    // Установка данных для отображения
    fun setData(data: List<Expense>) {
        this.expenses = data
        // Вычисление общей суммы
        totalAmount = data.sumOf { it.amount }.toFloat()
        // Подготовка сегментов
        prepareSlices()
        // Перерисовка View
        invalidate()
    }

    // Подготовка данных сегментов диаграммы
    private fun prepareSlices() {
        // Группировка данных по категориям
        val grouped = expenses.groupBy { it.category }
        val list = mutableListOf<PieSlice>()
        // Начальный угол -90° (начало сверху)
        var startAngle = -90f
        var colorIdx = 0

        // Создание сегментов для каждой категории
        for ((category, items) in grouped) {
            // Сумма значений категории
            val value = items.sumOf { it.amount }.toFloat()
            // Процент от общей суммы
            val percent = if (totalAmount == 0f) 0f else value / totalAmount
            val percentLabel = "${(percent * 100).roundToInt()}%"
            // Угол сегмента в градусах
            val sweep = percent * 360f
            // Цвет из палитры (циклически)
            val color = colors[colorIdx.rem(colors.size)]
            list.add(PieSlice(category, value, percent, percentLabel, startAngle, sweep, color))
            // Сдвиг начального угла для следующего сегмента
            startAngle += sweep
            colorIdx++
        }
        pieSlices = list
    }

    // Измерение размеров View
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

    // Основной метод рисования
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pieSlices.isEmpty()) return

        // Расчет размеров на основе минимального измерения View
        val minDim = width.coerceAtMost(height)
        val radius = minDim * 0.39f
        ringWidth = minDim * 0.18f
        piePaint.strokeWidth = ringWidth

        // Установка прямоугольника для дуг
        rectangle.set(
            width / 2f - radius,    // left
            height / 2f - radius,   // top
            width / 2f + radius,    // right
            height / 2f + radius    // bottom
        )

        // Отрисовка сегментов диаграммы
        drawSlices(canvas)
        // Отрисовка процентных подписей
        drawPercent(radius, canvas)
        // Отрисовка наименования категории
        drawText(canvas)
    }

    private fun drawSlices(canvas: Canvas) {
        for (slice in pieSlices) {
            piePaint.color = slice.color
            // Увеличение ширины для выбранного сегмента
            piePaint.strokeWidth =
                if (slice.category == selectedCategory) ringWidth * 1.2f else ringWidth
            // Рисование дуги (сегмента)
            canvas.drawArc(rectangle, slice.startAngle, slice.sweepAngle - 1, false, piePaint)
        }
    }

    private fun drawText(canvas: Canvas) {
        selectedCategory?.let {
            canvas.drawText(it, width / 2f, height / 2f - 10f, textPaint)
        }
    }

    private fun drawPercent(radius: Float, canvas: Canvas) {
        for (slice in pieSlices) {
            // Расчет позиции текста в центре сегмента
            val angleRad = (slice.startAngle + slice.sweepAngle / 2) * (PI.toFloat() / 180f)
            val textRadius = radius
            val labelX = width / 2f + textRadius * cos(angleRad)
            val labelY = height / 2f + textRadius * sin(angleRad) + textPaint.textSize / 2.6f

            // Рисование только для достаточно больших сегментов
            if (slice.sweepAngle >= 15f) {
                canvas.drawText(slice.percentLabel, labelX, labelY, textPaint)
            }
        }
    }

    // Обработка касаний для выбора сегментов
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val centerX = width / 2f
            val centerY = height / 2f
            // Смещение касания от центра
            val dx = event.x - centerX
            val dy = event.y - centerY
            // Расстояние от центра до точки касания
            val dist = sqrt(dx * dx + dy * dy)

            val minDim = width.coerceAtMost(height)
            val radius = minDim * 0.39f
            // Внутренний и внешний радиусы кольца
            val innerRadius = radius - ringWidth / 2
            val outerRadius = radius + ringWidth / 2

            // Проверка, что касание в области кольца
            if (dist in innerRadius..outerRadius) {
                // Расчет угла касания в градусах (-180 до 180)
                var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                // Конвертируем в диапазон 0-360 градусов
                if (touchAngle < 0) touchAngle += 360f

                // Поиск сегмента, в который попало касание
                val slice = pieSlices.firstOrNull { slice ->
                    val normalizedStartAngle =
                        if (slice.startAngle < 0) slice.startAngle + 360f else slice.startAngle
                    val endAngle = normalizedStartAngle + slice.sweepAngle

                    if (endAngle > 360f) {
                        // Сегмент пересекает границу 0/360 градусов
                        touchAngle >= normalizedStartAngle || touchAngle <= (endAngle - 360f)
                    } else {
                        touchAngle >= normalizedStartAngle && touchAngle <= endAngle
                    }
                }

                slice?.let {
                    // Установка выбранной категории
                    selectedCategory = it.category
                    invalidate() // Перерисовка с highlight
                }
            }
        }
        return true // Обработка события касания
    }

    // Сохранение состояния при повороте экрана и т.д.
    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("super", super.onSaveInstanceState())
            putString("selectedCategory", selectedCategory) // Сохранение выбранной категории
        }
    }

    // Восстановление состояния
    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? Bundle)?.let {
            selectedCategory = it.getString("selectedCategory", null)
            super.onRestoreInstanceState(it.getParcelable("super"))
        }
    }

    // Внутренний data class для хранения данных о сегментах диаграммы
    private data class PieSlice(
        val category: String,    // Категория
        val value: Float,        // Числовое значение
        val percent: Float,      // Процент от общего значения
        val percentLabel: String,
        val startAngle: Float,   // Начальный угол сегмента
        val sweepAngle: Float,   // Угол протяженности сегмента
        val color: Int           // Цвет сегмента
    )

    // Extension function для конвертации dp в px
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}