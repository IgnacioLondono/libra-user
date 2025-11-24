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
 * Tests simples de UI para SearchScreen.
 * Estos tests verifican que los elementos principales se muestran correctamente.
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SearchScreen muestra el campo de búsqueda`() {
        // Arrange
        val mockViewModel = mockk<MainViewModel>(relaxed = true)

        // Act
        composeTestRule.setContent {
            LibraUsersTheme {
                SearchScreen(vm = mockViewModel)
            }
        }

        // Assert: Verificar que el campo de búsqueda está visible
        // El campo de búsqueda generalmente tiene un placeholder o icono
        composeTestRule.onNodeWithText("Buscar", substring = true)
            .assertIsDisplayed()
    }
}

