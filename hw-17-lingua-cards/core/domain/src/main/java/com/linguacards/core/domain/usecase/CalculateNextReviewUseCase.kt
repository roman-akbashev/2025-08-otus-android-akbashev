package com.linguacards.core.domain.usecase

import com.linguacards.core.model.SrsGrade
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

@Singleton
class CalculateNextReviewUseCase @Inject constructor() {

    companion object {
        private const val MINIMUM_EASINESS_FACTOR = 1.3
        private const val INITIAL_INTERVAL_AGAIN = 1
        private const val INITIAL_INTERVAL_HARD = 1
        private const val INITIAL_INTERVAL_GOOD = 1
        private const val INITIAL_INTERVAL_EASY = 2
        private const val SECOND_INTERVAL_GOOD = 6
        private const val SECOND_INTERVAL_EASY = 8
        private const val EASY_MULTIPLIER = 1.3
    }

    /**
     * Алгоритм SM-2
     * @param repetitions количество повторений
     * @param easinessFactor фактор легкости (EF)
     * @param interval текущий интервал (дни)
     * @param grade оценка: AGAIN(1), HARD(3), GOOD(4), EASY(5) по шкале SM-2
     * @return новые значения [repetitions, easinessFactor, interval]
     */
    operator fun invoke(
        repetitions: Int,
        easinessFactor: Double,
        interval: Int,
        grade: SrsGrade
    ): Triple<Int, Double, Int> {
        // Конвертируем нашу оценку в шкалу SM-2 (1-5)
        val gradeValue = when (grade) {
            SrsGrade.AGAIN -> 1  // Полное забывание
            SrsGrade.HARD -> 3    // Трудно, но вспомнил
            SrsGrade.GOOD -> 4    // Нормально
            SrsGrade.EASY -> 5    // Легко
        }

        // Рассчитываем новый фактор легкости
        val newEasinessFactor = calculateEasinessFactor(easinessFactor, gradeValue)

        // Рассчитываем новые repetitions и interval
        return when (grade) {
            SrsGrade.AGAIN -> {
                // Полное забывание - сброс всех параметров
                Triple(0, newEasinessFactor, INITIAL_INTERVAL_AGAIN)
            }

            SrsGrade.HARD -> {
                // сохраняем счетчик, но уменьшаем интервал
                val newRepetitions = repetitions + 1
                val newInterval = when (newRepetitions) {
                    1 -> INITIAL_INTERVAL_HARD
                    else -> max(1, (interval * 1.2).roundToInt()) // Уменьшенный интервал
                }
                Triple(newRepetitions, newEasinessFactor, newInterval)
            }

            SrsGrade.GOOD, SrsGrade.EASY -> {
                val newRepetitions = repetitions + 1
                val newInterval = calculateInterval(
                    repetitions = newRepetitions,
                    easinessFactor = newEasinessFactor,
                    interval = interval,
                    isEasy = grade == SrsGrade.EASY
                )
                Triple(newRepetitions, newEasinessFactor, newInterval)
            }
        }
    }

    private fun calculateEasinessFactor(currentEF: Double, gradeValue: Int): Double {
        val newEF = currentEF + (0.1 - (5 - gradeValue) * (0.08 + (5 - gradeValue) * 0.02))
        return max(MINIMUM_EASINESS_FACTOR, newEF)
    }

    private fun calculateInterval(
        repetitions: Int,
        easinessFactor: Double,
        interval: Int,
        isEasy: Boolean
    ): Int {
        return when (repetitions) {
            1 -> if (isEasy) INITIAL_INTERVAL_EASY else INITIAL_INTERVAL_GOOD
            2 -> if (isEasy) SECOND_INTERVAL_EASY else SECOND_INTERVAL_GOOD
            else -> {
                val multiplier = if (isEasy) EASY_MULTIPLIER else 1.0
                (interval * easinessFactor * multiplier).roundToInt()
            }
        }
    }
}