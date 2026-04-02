package com.linguacards.core.domain.usecase

import com.linguacards.core.model.SrsGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateNextReviewUseCaseTest {

    private lateinit var useCase: CalculateNextReviewUseCase

    @Before
    fun setup() {
        useCase = CalculateNextReviewUseCase()
    }

    @Test
    fun `AGAIN grade should reset repetitions to 0 and interval to 1`() {
        // Given
        val repetitions = 5
        val easinessFactor = 2.5
        val interval = 30
        val grade = SrsGrade.AGAIN

        // When
        val (newRepetitions, newEasinessFactor, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = easinessFactor,
            interval = interval,
            grade = grade
        )

        // Then
        assertEquals(0, newRepetitions)
        assertEquals(1, newInterval)
        assertTrue(newEasinessFactor < easinessFactor) // EF должен уменьшиться
        assertTrue(newEasinessFactor >= 1.3) // Но не ниже минимума
    }

    @Test
    fun `AGAIN grade should decrease easiness factor`() {
        // Given
        val easinessFactor = 2.5

        // When
        val (_, newEasinessFactor, _) = useCase(
            repetitions = 5,
            easinessFactor = easinessFactor,
            interval = 30,
            grade = SrsGrade.AGAIN
        )

        // Then
        assertEquals(1.96, newEasinessFactor, 0.001)
    }

    @Test
    fun `AGAIN grade should not decrease easiness factor below minimum`() {
        // Given
        val easinessFactor = 1.4 // Чуть выше минимума

        // When
        val (_, newEasinessFactor, _) = useCase(
            repetitions = 5,
            easinessFactor = easinessFactor,
            interval = 30,
            grade = SrsGrade.AGAIN
        )

        // Then
        assertEquals(1.3, newEasinessFactor, 0.001)
    }

    @Test
    fun `HARD grade should increment repetitions`() {
        // Given
        val repetitions = 3

        // When
        val (newRepetitions, _, _) = useCase(
            repetitions = repetitions,
            easinessFactor = 2.5,
            interval = 15,
            grade = SrsGrade.HARD
        )

        // Then
        assertEquals(4, newRepetitions)
    }

    @Test
    fun `HARD grade with first repetition should set interval to 1`() {
        // Given
        val repetitions = 0 // Первое повторение

        // When
        val (_, _, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = 2.5,
            interval = 0,
            grade = SrsGrade.HARD
        )

        // Then
        assertEquals(1, newInterval)
    }

    @Test
    fun `HARD grade with subsequent repetitions should multiply interval by 1_2`() {
        // Given
        val repetitions = 3
        val interval = 10

        // When
        val (_, _, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = 2.5,
            interval = interval,
            grade = SrsGrade.HARD
        )

        // Then
        assertEquals(12, newInterval) // 10 * 1.2 = 12
    }

    @Test
    fun `HARD grade should never set interval below 1`() {
        // Given
        val repetitions = 1
        val interval = 0 // Невалидный интервал

        // When
        val (_, _, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = 2.5,
            interval = interval,
            grade = SrsGrade.HARD
        )

        // Then
        assertTrue(newInterval >= 1)
    }

    @Test
    fun `HARD grade should decrease easiness factor`() {
        // Given
        val easinessFactor = 2.5

        // When
        val (_, newEasinessFactor, _) = useCase(
            repetitions = 5,
            easinessFactor = easinessFactor,
            interval = 30,
            grade = SrsGrade.HARD
        )

        // Then
        assertEquals(2.36, newEasinessFactor, 0.001)
    }

    @Test
    fun `GOOD grade should increment repetitions`() {
        // Given
        val repetitions = 2

        // When
        val (newRepetitions, _, _) = useCase(
            repetitions = repetitions,
            easinessFactor = 2.5,
            interval = 6,
            grade = SrsGrade.GOOD
        )

        // Then
        assertEquals(3, newRepetitions)
    }

    @Test
    fun `GOOD grade first repetition should set interval to 1`() {
        // When
        val (_, _, newInterval) = useCase(
            repetitions = 0,
            easinessFactor = 2.5,
            interval = 0,
            grade = SrsGrade.GOOD
        )

        // Then
        assertEquals(1, newInterval)
    }

    @Test
    fun `GOOD grade second repetition should set interval to 6`() {
        // When
        val (_, _, newInterval) = useCase(
            repetitions = 1,
            easinessFactor = 2.5,
            interval = 1,
            grade = SrsGrade.GOOD
        )

        // Then
        assertEquals(6, newInterval)
    }

    @Test
    fun `GOOD grade third repetition should multiply interval by easiness factor`() {
        // Given
        val repetitions = 2
        val easinessFactor = 2.5
        val interval = 6

        // When
        val (_, _, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = easinessFactor,
            interval = interval,
            grade = SrsGrade.GOOD
        )

        // Then
        assertEquals(15, newInterval) // 6 * 2.5 = 15
    }

    @Test
    fun `EASY grade should increment repetitions`() {
        // Given
        val repetitions = 3

        // When
        val (newRepetitions, _, _) = useCase(
            repetitions = repetitions,
            easinessFactor = 2.5,
            interval = 15,
            grade = SrsGrade.EASY
        )

        // Then
        assertEquals(4, newRepetitions)
    }

    @Test
    fun `EASY grade first repetition should set interval to 2`() {
        // When
        val (_, _, newInterval) = useCase(
            repetitions = 0,
            easinessFactor = 2.5,
            interval = 0,
            grade = SrsGrade.EASY
        )

        // Then
        assertEquals(2, newInterval)
    }

    @Test
    fun `EASY grade second repetition should set interval to 8`() {
        // When
        val (_, _, newInterval) = useCase(
            repetitions = 1,
            easinessFactor = 2.5,
            interval = 2,
            grade = SrsGrade.EASY
        )

        // Then
        assertEquals(8, newInterval)
    }

    @Test
    fun `EASY grade third repetition should multiply interval by easiness factor and easy multiplier`() {
        // Given
        val repetitions = 2
        val easinessFactor = 2.5
        val interval = 8

        // When
        val (_, _, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = easinessFactor,
            interval = interval,
            grade = SrsGrade.EASY
        )

        // Then
        assertEquals(27, newInterval) // 8 * 2.6 * 1.3 = 27.04 -> roundToInt = 27
    }

    @Test
    fun `EASY grade should increase easiness factor`() {
        // Given
        val easinessFactor = 2.5

        // When
        val (_, newEasinessFactor, _) = useCase(
            repetitions = 5,
            easinessFactor = easinessFactor,
            interval = 30,
            grade = SrsGrade.EASY
        )

        // Then
        assertEquals(2.6, newEasinessFactor, 0.001)  // 2.5 + 0.1
    }

    @Test
    fun `multiple reviews should maintain correct progression for GOOD grades`() {
        // Симулируем последовательность GOOD ответов
        val repetitions = 0
        val easinessFactor = 2.5
        val interval = 0

        // Первый GOOD
        val (r1, ef1, i1) = useCase(repetitions, easinessFactor, interval, SrsGrade.GOOD)
        assertEquals(1, r1)
        assertEquals(2.5, ef1, 0.001)
        assertEquals(1, i1)

        // Второй GOOD
        val (r2, ef2, i2) = useCase(r1, ef1, i1, SrsGrade.GOOD)
        assertEquals(2, r2)
        assertEquals(2.5, ef2, 0.001)
        assertEquals(6, i2)

        // Третий GOOD
        val (r3, ef3, i3) = useCase(r2, ef2, i2, SrsGrade.GOOD)
        assertEquals(3, r3)
        assertEquals(2.5, ef3, 0.001)
        assertEquals(15, i3) // 6 * 2.5 = 15

        // Четвертый GOOD
        val (r4, ef4, i4) = useCase(r3, ef3, i3, SrsGrade.GOOD)
        assertEquals(4, r4)
        assertEquals(2.5, ef4, 0.001)
        assertEquals(38, i4) // Исправлено: 15 * 2.5 = 37.5 -> roundToInt = 38
    }

    @Test
    fun `EASY grade third repetition should use correct formula`() {
        // Given
        val repetitions = 2
        val easinessFactor = 2.7
        val interval = 8

        // When
        val (_, _, newInterval) = useCase(
            repetitions = repetitions,
            easinessFactor = easinessFactor,
            interval = interval,
            grade = SrsGrade.EASY
        )

        // Then
        val expectedValue = interval * easinessFactor * 1.3
        val possibleRoundedValues = listOf(
            kotlin.math.floor(expectedValue).toInt(),
            kotlin.math.round(expectedValue).toInt(),
            kotlin.math.ceil(expectedValue).toInt()
        )

        assertTrue(
            "Interval $newInterval should be one of $possibleRoundedValues",
            newInterval in possibleRoundedValues
        )
    }

    @Test
    fun `mixed reviews should correctly handle forgetting curve`() {
        // Симулируем смешанную последовательность
        val repetitions = 0
        val easinessFactor = 2.5
        val interval = 0

        // Первый GOOD
        val (r1, ef1, i1) = useCase(repetitions, easinessFactor, interval, SrsGrade.GOOD)
        assertEquals(1, r1)
        assertEquals(1, i1)

        // Второй GOOD
        val (r2, ef2, i2) = useCase(r1, ef1, i1, SrsGrade.GOOD)
        assertEquals(2, r2)
        assertEquals(6, i2)

        // Забыл (AGAIN)
        val (r3, ef3, i3) = useCase(r2, ef2, i2, SrsGrade.AGAIN)
        assertEquals(0, r3) // Сброс до 0
        assertEquals(1, i3)
        assertTrue(ef3 < ef2) // EF уменьшился

        // После забывания - первый GOOD снова
        val (r4, ef4, i4) = useCase(r3, ef3, i3, SrsGrade.GOOD)
        assertEquals(1, r4)
        assertEquals(1, i4)
        assertEquals(ef3, ef4, 0.001) // EF не меняется при GOOD
    }

    @Test
    fun `easiness factor should never go below minimum`() {
        // Пытаемся опустить EF ниже минимума через AGAIN
        var ef = 1.5

        repeat(5) {
            val (_, newEf, _) = useCase(
                repetitions = 5,
                easinessFactor = ef,
                interval = 30,
                grade = SrsGrade.AGAIN
            )
            ef = newEf
        }

        assertTrue(ef >= 1.3)
        assertEquals(1.3, ef, 0.001)
    }

    @Test
    fun `different grade values should produce different easiness factor changes`() {
        val baseEF = 2.5

        val (_, efAgain, _) = useCase(5, baseEF, 30, SrsGrade.AGAIN)
        val (_, efHard, _) = useCase(5, baseEF, 30, SrsGrade.HARD)
        val (_, efGood, _) = useCase(5, baseEF, 30, SrsGrade.GOOD)
        val (_, efEasy, _) = useCase(5, baseEF, 30, SrsGrade.EASY)

        // Проверяем порядок: AGAIN < HARD < GOOD < EASY
        assertTrue(efAgain < efHard)
        assertTrue(efHard < efGood)
        assertTrue(efGood < efEasy)

        // Проверяем конкретные значения для вашей реализации
        assertEquals(1.96, efAgain, 0.001)  // 2.5 - 0.54
        assertEquals(2.36, efHard, 0.001)   // 2.5 - 0.14
        assertEquals(2.5, efGood, 0.001)    // без изменений
        assertEquals(2.6, efEasy, 0.001)    // 2.5 + 0.1
    }

    @Test
    fun `should handle edge case with zero repetitions`() {
        // Тест для граничного случая - первое повторение
        val (repetitions, _, interval) = useCase(
            repetitions = 0,
            easinessFactor = 2.5,
            interval = 0,
            grade = SrsGrade.GOOD
        )

        assertEquals(1, repetitions)
        assertEquals(1, interval)
    }

    @Test
    fun `should handle large intervals without overflow`() {
        // Тест для больших интервалов
        val (_, _, interval) = useCase(
            repetitions = 100,
            easinessFactor = 2.5,
            interval = 365 * 5, // 5 лет
            grade = SrsGrade.GOOD
        )

        assertTrue(interval > 0)
    }
}