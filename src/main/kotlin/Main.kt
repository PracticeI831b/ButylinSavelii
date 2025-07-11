import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.exp
import kotlin.math.sqrt
import java.util.Locale

const val EPSILON = 1e-12 // Увеличенная точность вычислений
const val MAX_ITERATIONS = 1000 // Максимальное число итераций
const val MIN_POSITIVE = 1e-10 // Минимальное положительное значение

// Цвета для темной темы
val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF1E1E1E)
val PrimaryColor = Color(0xFFBB86FC)
val SecondaryColor = Color(0xFF03DAC6)
val ErrorColor = Color(0xFFCF6679)
val TextColor = Color(0xFFFFFFFF)
val HintColor = Color(0xFFA0A0A0)

// Исходная функция: e^x = 1/√x → e^x - 1/√x = 0
private fun f(x: Double): Double {
    if (x <= 0) return Double.NaN // Функция не определена для x <= 0
    return exp(x) - 1.0 / sqrt(x)
}

// Реализация улучшенного метода бисекции
fun bisectionMethod(a: Double, b: Double): Pair<Double?, String> {
    if (a >= b) return null to "a должно быть < b"

    // Автоматическая корректировка интервала
    val actualA = if (a < MIN_POSITIVE) MIN_POSITIVE else a
    val actualB = if (b > 100) 100.0 else b // Ограничиваем максимальное значение

    if (actualA >= actualB) return null to "Некорректный интервал после корректировки"

    try {
        f(actualA)
        f(actualB)
    } catch (e: Exception) {
        return null to "Ошибка вычисления: ${e.message}"
    }

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
        val fMid = try {
            f(mid)
        } catch (e: Exception) {
            return null to "Ошибка на итерации $iterations: ${e.message}"
        }

        if (fMid.isNaN()) return null to "Функция не определена в точке $mid"

        // Сохраняем лучшее приближение
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

private fun abs(value: Double) = kotlin.math.abs(value)

// Улучшенное форматирование чисел
fun formatScientific(value: Double): String {
    if (value == 0.0) return "0"
    if (value.isNaN()) return "Не определено"

    val absValue = abs(value)
    return when {
        absValue >= 1e4 -> String.format(Locale.US, "%.4e", value).replace("e", " × 10^")
        absValue <= 1e-4 -> String.format(Locale.US, "%.4e", value).replace("e", " × 10^")
        else -> String.format(Locale.US, "%.8f", value)
    }
}

@Composable
@Preview
fun EquationSolver() {
    var a by remember { mutableStateOf("0.1") }
    var b by remember { mutableStateOf("2.0") }
    var result by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf<Double?>(null) }
    var errorState by remember { mutableStateOf(false) }
    var adjusted by remember { mutableStateOf(false) }
    var stableRoot by remember { mutableStateOf<Double?>(null) } // Стабильное значение корня

    // Поддержка ввода с запятой и точкой
    fun String.sanitizeNumberInput() = this.replace(',', '.').filter {
        it.isDigit() || it == '.' || it == '-' || it == 'e' || it == 'E'
    }

    // Функция для решения уравнения
    fun solveEquation() {
        errorState = false
        adjusted = false
        val sanitizedA = a.sanitizeNumberInput()
        val sanitizedB = b.sanitizeNumberInput()

        val aVal = sanitizedA.toDoubleOrNull()
        val bVal = sanitizedB.toDoubleOrNull()

        if (aVal == null || bVal == null) {
            result = "Ошибка: некорректные числа"
            solution = null
            errorState = true
            return
        }

        val (root, message) = bisectionMethod(aVal, bVal)
        solution = root

        if (root == null) {
            result = "❌ $message"
            errorState = true
        } else {
            // Сохраняем стабильное значение, если оно не было установлено
            if (stableRoot == null) {
                stableRoot = root
            }
            result = "✅ $message"

            if (aVal < MIN_POSITIVE) {
                adjusted = true
            }
        }
    }

    // Функция для сброса значений
    fun resetValues() {
        a = "0.1"
        b = "2.0"
        result = ""
        solution = null
        errorState = false
        adjusted = false
        stableRoot = null
    }

    // Темная тема Material Design
    MaterialTheme(
        colors = darkColors(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            error = ErrorColor,
            background = DarkBackground,
            surface = CardBackground,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = TextColor,
            onSurface = TextColor
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = "Решение нелинейных уравнений",
                    style = MaterialTheme.typography.h4,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Уравнение
                Text(
                    text = "eˣ = 1/√x",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    ),
                    color = SecondaryColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Информация об области определения
                Text(
                    text = "Функция определена только для x > 0",
                    style = MaterialTheme.typography.caption,
                    color = HintColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Карточка с вводом данных
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = CardBackground,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Введите границы интервала [a; b]",
                            style = MaterialTheme.typography.h6,
                            color = TextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Поле ввода A
                        OutlinedTextField(
                            value = a,
                            onValueChange = {
                                a = it.sanitizeNumberInput()
                                solution = null
                                result = ""
                            },
                            label = { Text("Левая граница (a)", color = HintColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = TextColor,
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = HintColor,
                                cursorColor = PrimaryColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // Поле ввода B
                        OutlinedTextField(
                            value = b,
                            onValueChange = {
                                b = it.sanitizeNumberInput()
                                solution = null
                                result = ""
                            },
                            label = { Text("Правая граница (b > a)", color = HintColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = TextColor,
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = HintColor,
                                cursorColor = PrimaryColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Кнопки действий
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { solveEquation() },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = PrimaryColor,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Решить уравнение")
                            }

                            Spacer(Modifier.width(16.dp))

                            Button(
                                onClick = { resetValues() },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = SecondaryColor,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(0.3f)
                            ) {
                                Icon(Icons.Default.Refresh, "Сбросить")
                            }
                        }
                    }
                }

                // Карточка результатов
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = CardBackground,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Результаты вычислений",
                            style = MaterialTheme.typography.h6,
                            color = TextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Предупреждение о корректировке
                        if (adjusted) {
                            Text(
                                "⚠️ Левая граница была скорректирована\nдо $MIN_POSITIVE",
                                color = SecondaryColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }

                        // Вывод корня (используем стабильное значение)
                        stableRoot?.let { root ->
                            Text(
                                "Найденный корень:",
                                color = HintColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                String.format(Locale.US, "%.8f", root),
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryColor
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                "Значение функции в корне:",
                                color = HintColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Центрированное значение функции
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatScientific(f(root)),
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = SecondaryColor
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "≈ 0",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = SecondaryColor,
                                            fontWeight = FontWeight.Light
                                        )
                                    )
                                }
                            }
                        }


                        // Вывод сообщения/ошибки
                        if (result.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = result,
                                color = if (errorState) ErrorColor else SecondaryColor,
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Предложение для ошибки
                            if (errorState) {
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { resetValues() },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = PrimaryColor,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Попробовать другие значения")
                                }
                            }
                        }
                    }
                }

                // Информация о методе
                Spacer(Modifier.height(16.dp))
                Text(
                    "Метод бисекции (дихотомии):\n" +
                            "1. Проверяем f(a)·f(b) < 0\n" +
                            "2. Делим отрезок пополам\n" +
                            "3. Выбираем подотрезок со сменой знака\n" +
                            "4. Повторяем до достижения точности",
                    color = HintColor,
                    style = MaterialTheme.typography.caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

fun main() = application {
    Window(
        title = "Решение нелинейных уравнений",
        onCloseRequest = ::exitApplication
    ) {
        EquationSolver()
    }
}