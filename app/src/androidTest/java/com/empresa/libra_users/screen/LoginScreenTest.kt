package com.empresa.libra_users.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.empresa.libra_users.ui.theme.LibraUsersTheme
import com.empresa.libra_users.viewmodel.MainViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests simples de UI para LoginScreen.
 * Estos tests verifican que los elementos principales se muestran correctamente.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `LoginScreen muestra el campo de email`() {
        // Arrange: Crear un ViewModel mock
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act: Componer la pantalla
        composeTestRule.setContent {
            LibraUsersTheme {
                LoginScreen(
                    onGoRegister = {},
                    vm = mockViewModel
                )
            }
        }

        // Assert: Verificar que el campo de email está visible
        // Nota: En un test real, buscarías por el placeholder o label del campo
        composeTestRule.onNodeWithText("Email", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `LoginScreen muestra el campo de contraseña`() {
        // Arrange
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                LoginScreen(
                    onGoRegister = {},
                    vm = mockViewModel
                )
            }
        }

        // Assert: Verificar que el campo de contraseña está visible
        composeTestRule.onNodeWithText("Contraseña", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `LoginScreen muestra el botón de login`() {
        // Arrange
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                LoginScreen(
                    onGoRegister = {},
                    vm = mockViewModel
                )
            }
        }

        // Assert: Verificar que el botón de login está visible
        composeTestRule.onNodeWithText("Iniciar Sesión", substring = true)
            .assertIsDisplayed()
    }
}

