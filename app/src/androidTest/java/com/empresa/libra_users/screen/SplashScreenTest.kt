package com.empresa.libra_users.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.empresa.libra_users.ui.theme.LibraUsersTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests simples de UI para SplashScreen.
 * Estos tests verifican que la pantalla de splash se muestra correctamente.
 */
@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SplashScreen se renderiza correctamente`() {
        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                SplashScreen()
            }
        }

        // Assert: Verificar que la pantalla se renderiza sin errores
        // En este caso, simplemente verificamos que no hay crashes
        composeTestRule.onRoot().assertIsDisplayed()
    }
}

