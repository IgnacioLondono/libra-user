package com.empresa.libra_users.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.empresa.libra_users.ui.theme.LibraUsersTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests simples de UI para LoadingScreen.
 * Estos tests verifican que la pantalla de carga se muestra correctamente.
 */
@RunWith(AndroidJUnit4::class)
class LoadingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `LoadingScreen muestra el texto de carga`() {
        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                LoadingScreen()
            }
        }

        // Assert: Verificar que se muestra algún texto relacionado con carga
        // Ajusta el texto según lo que realmente muestre tu LoadingScreen
        composeTestRule.onNodeWithText("Cargando", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }
}

