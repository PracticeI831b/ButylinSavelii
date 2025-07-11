import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MainScreen

fun main() = application {
    Window(
        title = "Решение нелинейных уравнений",
        onCloseRequest = ::exitApplication
    ) {
        MainScreen()
    }
}