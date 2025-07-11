package model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Вычисляет значение функции e^x - 1/√x
 * @param x Входное значение (должно быть > 0)
 * @return Значение функции или NaN при недопустимых значениях
 */
fun f(x: Double): Double {
    if (x <= 0) return Double.NaN
    return exp(x) - 1.0 / sqrt(x)
}

/**
 * Реализация метода бисекции для поиска корня уравнения
 * @param a Левая граница интервала
 * @param b Правая граница интервала
 * @return Пара (корень, сообщение о результате)
 */
fun bisectionMethod(a: Double, b: Double): Pair<Double?, String> {
    if (a >= b) return null to "a должно быть < b"

    // Автоматическая корректировка границ
    val actualA = if (a < MIN_POSITIVE) MIN_POSITIVE else a
    val actualB = if (b > 100) 100.0 else b

    if (actualA >= actualB) return null to "Некорректный интервал после корректировки"

    val fa = f(actualA)
    val fb = f(actualB)

    if (fa.isNaN() || fb.isNaN()) return null to "Функция не определена на границах"
    if (fa * fb > 0) return null to "f(a) и f(b) одного знака"

    var low = actualA
    var high = actualB
    var iterations = 0
    var bestRoot: Double? = null
    var bestFValue = Double.MAX_VALUE

    while (high - low > EPSILON && iterations < MAX_ITERATIONS) {
        val mid = (low + high) / 2
        val fMid = f(mid)

        if (fMid.isNaN()) return null to "Функция не определена в $mid"
        if (abs(fMid) < abs(bestFValue)) {
            bestRoot = mid
            bestFValue = fMid
        }

        when {
            fMid == 0.0 -> return mid to "Точное решение за $iterations итераций"
            f(low) * fMid < 0 -> high = mid
            else -> low = mid
        }
        iterations++
    }

    return bestRoot to "Решение найдено за $iterations итераций"
}

/**
 * Форматирует число в научный вид при необходимости
 * @param value Число для форматирования
 * @return Отформатированная строка
 */
fun formatScientific(value: Double): String {
    if (value == 0.0) return "0"
    if (value.isNaN()) return "Не определено"

    val absValue = abs(value)
    return when {
        absValue >= 1e4 || absValue <= 1e-4 ->
            String.format(Locale.US, "%.4e", value).replace("e", " × 10^")
        else -> String.format(Locale.US, "%.8f", value)
    }
}
