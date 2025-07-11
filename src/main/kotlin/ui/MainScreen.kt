package ui

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.*

/**
 * Основной экран приложения с интерфейсом решения уравнений
 */
@Composable
fun MainScreen() {
    var a by remember { mutableStateOf("0.1") }
    var b by remember { mutableStateOf("2.0") }
    var result by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf<Double?>(null) }
    var errorState by remember { mutableStateOf(false) }
    var adjusted by remember { mutableStateOf(false) }
    var stableRoot by remember { mutableStateOf<Double?>(null) }

    // Очистка и валидация ввода
    fun String.sanitizeNumberInput() = replace(',', '.').filter {
        it.isDigit() || it == '.' || it == '-' || it == 'e' || it == 'E'
    }

    // Решение уравнения
    fun solveEquation() {
        errorState = false
        adjusted = false

        val aVal = a.sanitizeNumberInput().toDoubleOrNull()
        val bVal = b.sanitizeNumberInput().toDoubleOrNull()

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
            stableRoot = stableRoot ?: root
            result = "✅ $message"
            if (aVal < MIN_POSITIVE) adjusted = true
        }
    }

    // Сброс значений
    fun resetValues() {
        a = "0.1"
        b = "2.0"
        result = ""
        solution = null
        errorState = false
        adjusted = false
        stableRoot = null
    }

    EquationSolverTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    "Решение нелинейных уравнений",
                    style = MaterialTheme.typography.h4,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Уравнение
                Text(
                    "eˣ = 1/√x",
                    style = TextStyle(fontSize = 24.sp, fontFamily = FontFamily.Serif),
                    color = SecondaryColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Область определения
                Text(
                    "Функция определена только для x > 0",
                    style = MaterialTheme.typography.caption,
                    color = HintColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Карточка ввода
                InputCard(
                    a = a,
                    b = b,
                    onAChanged = { newValue ->
                        a = newValue
                        solution = null
                        result = ""
                    },
                    onBChanged = { newValue ->
                        b = newValue
                        solution = null
                        result = ""
                    },
                    onSolve = { solveEquation() },
                    onReset = { resetValues() }
                )

                // Карточка результатов
                ResultCard(
                    result = result,
                    stableRoot = stableRoot,
                    errorState = errorState,
                    adjusted = adjusted,
                    onReset = { resetValues() }
                )

                // Описание метода
                MethodDescription()
            }
        }
    }
}

@Composable
private fun InputCard(
    a: String,
    b: String,
    onAChanged: (String) -> Unit,
    onBChanged: (String) -> Unit,
    onSolve: () -> Unit,
    onReset: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), backgroundColor = CardBackground) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Введите границы интервала [a; b]",
                style = MaterialTheme.typography.h6,
                color = TextColor,
                modifier = Modifier.padding(bottom = 12.dp))

            OutlinedTextField(
                value = a,
                onValueChange = onAChanged,
                label = { Text("Левая граница (a)", color = HintColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = b,
                onValueChange = onBChanged,
                label = { Text("Правая граница (b > a)", color = HintColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = onSolve,
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor),
                    modifier = Modifier.weight(1f)) {
                    Text("Решить уравнение")
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryColor),
                    modifier = Modifier.weight(0.3f)) {
                    Icon(Icons.Default.Refresh, "Сбросить")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: String,
    stableRoot: Double?,
    errorState: Boolean,
    adjusted: Boolean,
    onReset: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), backgroundColor = CardBackground) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Результаты вычислений",
                style = MaterialTheme.typography.h6,
                color = TextColor,
                modifier = Modifier.padding(bottom = 12.dp))

            if (adjusted) {
                Text(
                    "⚠️ Левая граница скорректирована до $MIN_POSITIVE",
                    color = SecondaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
            }

            stableRoot?.let { root ->
                Text("Найденный корень:", color = HintColor)
                Text(
                    "%.8f".format(root),
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SecondaryColor),
                    modifier = Modifier.padding(vertical = 8.dp))

                Text("Значение функции:", color = HintColor)
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatScientific(f(root)), style = TextStyle(fontSize = 18.sp, color = SecondaryColor))
                        Text(" ≈ 0", style = TextStyle(fontSize = 18.sp, color = SecondaryColor, fontWeight = FontWeight.Light))
                    }
                }
            }

            if (result.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    result,
                    color = if (errorState) ErrorColor else SecondaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())

                if (errorState) {
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onReset, colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor)) {
                        Text("Попробовать другие значения")
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodDescription() {
    Text(
        "Метод бисекции (дихотомии):\n" +
                "1. Проверяем f(a)·f(b) < 0\n" +
                "2. Делим отрезок пополам\n" +
                "3. Выбираем подотрезок со сменой знака\n" +
                "4. Повторяем до достижения точности",
        color = HintColor,
        style = MaterialTheme.typography.caption,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(8.dp))
}

@Composable
private fun textFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    textColor = TextColor,
    focusedBorderColor = PrimaryColor,
    unfocusedBorderColor = HintColor,
    cursorColor = PrimaryColor
)