package com.empresa.libra_users.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.empresa.libra_users.ui.theme.LibraUsersTheme
import com.empresa.libra_users.viewmodel.MainViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests simples de UI para RegisterScreen.
 * Estos tests verifican que los elementos principales se muestran correctamente.
 */
@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `RegisterScreen muestra el campo de nombre`() {
        // Arrange
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                RegisterScreen(
                    onRegisteredNavigateLogin = {},
                    onGoLogin = {},
                    vm = mockViewModel
                )
            }
        }

        // Assert: Verificar que el campo de nombre está visible
        composeTestRule.onNodeWithText("Nombre", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `RegisterScreen muestra el campo de email`() {
        // Arrange
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                RegisterScreen(
                    onRegisteredNavigateLogin = {},
                    onGoLogin = {},
                    vm = mockViewModel
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Email", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `RegisterScreen muestra el botón de registro`() {
        // Arrange
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                RegisterScreen(
                    onRegisteredNavigateLogin = {},
                    onGoLogin = {},
                    vm = mockViewModel
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Registrarse", substring = true)
            .assertIsDisplayed()
    }
}

