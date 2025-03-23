import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileTest {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateProfile() = baselineProfileRule.collect(
        packageName = "com.williamfq.xhat"
    ) {
        // Simula el lanzamiento de la actividad principal
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = instrumentation.context.packageManager.getLaunchIntentForPackage("com.williamfq.xhat")
        instrumentation.startActivitySync(intent)
        // Agrega más interacciones críticas aquí, como navegar por pantallas
        Thread.sleep(1000) // Ejemplo: espera para capturar el perfil
    }
}