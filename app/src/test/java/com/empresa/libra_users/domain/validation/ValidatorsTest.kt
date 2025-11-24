package com.empresa.libra_users.domain.validation

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests simples para las funciones de validación.
 * Estos tests son fáciles de entender y mantener.
 */
class ValidatorsTest {

    // ==================== TESTS DE EMAIL ====================
    
    @Test
    fun `validateEmail retorna null cuando el email es válido`() {
        val resultado = validateEmail("usuario@gmail.com")
        assertNull("Un email válido no debe retornar error", resultado)
    }

    @Test
    fun `validateEmail retorna error cuando el email está vacío`() {
        val resultado = validateEmail("")
        assertNotNull("Un email vacío debe retornar error", resultado)
        assertEquals("El email es obligatorio", resultado)
    }

    @Test
    fun `validateEmail retorna error cuando el email no tiene formato válido`() {
        val resultado = validateEmail("email-invalido")
        assertNotNull("Un email sin formato válido debe retornar error", resultado)
        assertEquals("Formato de email inválido", resultado)
    }

    @Test
    fun `validateEmail retorna error cuando el email no es de gmail`() {
        val resultado = validateEmail("usuario@hotmail.com")
        assertNotNull("Un email que no es gmail debe retornar error", resultado)
        assertEquals("Debe ser @gmail.com", resultado)
    }

    @Test
    fun `validateEmail acepta emails con mayúsculas`() {
        val resultado = validateEmail("USUARIO@GMAIL.COM")
        assertNull("Un email con mayúsculas debe ser válido", resultado)
    }

    // ==================== TESTS DE NOMBRE ====================
    
    @Test
    fun `validateNameLettersOnly retorna null cuando el nombre es válido`() {
        val resultado = validateNameLettersOnly("Juan Pérez")
        assertNull("Un nombre válido no debe retornar error", resultado)
    }

    @Test
    fun `validateNameLettersOnly retorna error cuando el nombre está vacío`() {
        val resultado = validateNameLettersOnly("")
        assertNotNull("Un nombre vacío debe retornar error", resultado)
        assertEquals("El nombre es obligatorio", resultado)
    }

    @Test
    fun `validateNameLettersOnly retorna error cuando el nombre tiene números`() {
        val resultado = validateNameLettersOnly("Juan123")
        assertNotNull("Un nombre con números debe retornar error", resultado)
        assertEquals("Solo letras y espacios", resultado)
    }

    @Test
    fun `validateNameLettersOnly acepta nombres con tildes`() {
        val resultado = validateNameLettersOnly("José María")
        assertNull("Un nombre con tildes debe ser válido", resultado)
    }

    @Test
    fun `validateNameLettersOnly acepta la letra ñ`() {
        val resultado = validateNameLettersOnly("Niño")
        assertNull("Un nombre con ñ debe ser válido", resultado)
    }

    // ==================== TESTS DE TELÉFONO ====================
    
    @Test
    fun `validatePhoneDigitsOnly retorna null cuando el teléfono es válido`() {
        val resultado = validatePhoneDigitsOnly("12345678")
        assertNull("Un teléfono válido no debe retornar error", resultado)
    }

    @Test
    fun `validatePhoneDigitsOnly retorna error cuando el teléfono está vacío`() {
        val resultado = validatePhoneDigitsOnly("")
        assertNotNull("Un teléfono vacío debe retornar error", resultado)
        assertEquals("El teléfono es obligatorio", resultado)
    }

    @Test
    fun `validatePhoneDigitsOnly retorna error cuando tiene letras`() {
        val resultado = validatePhoneDigitsOnly("123abc")
        assertNotNull("Un teléfono con letras debe retornar error", resultado)
        assertEquals("Solo números", resultado)
    }

    @Test
    fun `validatePhoneDigitsOnly retorna error cuando es muy corto`() {
        val resultado = validatePhoneDigitsOnly("123")
        assertNotNull("Un teléfono muy corto debe retornar error", resultado)
        assertEquals("Debe tener entre 8 y 15 dígitos", resultado)
    }

    @Test
    fun `validatePhoneDigitsOnly retorna error cuando es muy largo`() {
        val resultado = validatePhoneDigitsOnly("1234567890123456")
        assertNotNull("Un teléfono muy largo debe retornar error", resultado)
        assertEquals("Debe tener entre 8 y 15 dígitos", resultado)
    }

    @Test
    fun `validatePhoneDigitsOnly acepta teléfonos de 15 dígitos`() {
        val resultado = validatePhoneDigitsOnly("123456789012345")
        assertNull("Un teléfono de 15 dígitos debe ser válido", resultado)
    }

    // ==================== TESTS DE CONTRASEÑA ====================
    
    @Test
    fun `validateStrongPassword retorna null cuando la contraseña es válida`() {
        val resultado = validateStrongPassword("Password123!")
        assertNull("Una contraseña válida no debe retornar error", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando está vacía`() {
        val resultado = validateStrongPassword("")
        assertNotNull("Una contraseña vacía debe retornar error", resultado)
        assertEquals("La contraseña es obligatoria", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando es muy corta`() {
        val resultado = validateStrongPassword("Pass1!")
        assertNotNull("Una contraseña muy corta debe retornar error", resultado)
        assertEquals("Mínimo 8 caracteres", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando no tiene mayúscula`() {
        val resultado = validateStrongPassword("password123!")
        assertNotNull("Una contraseña sin mayúscula debe retornar error", resultado)
        assertEquals("Debe incluir una mayúscula", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando no tiene minúscula`() {
        val resultado = validateStrongPassword("PASSWORD123!")
        assertNotNull("Una contraseña sin minúscula debe retornar error", resultado)
        assertEquals("Debe incluir una minúscula", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando no tiene número`() {
        val resultado = validateStrongPassword("Password!")
        assertNotNull("Una contraseña sin número debe retornar error", resultado)
        assertEquals("Debe incluir un número", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando no tiene símbolo`() {
        val resultado = validateStrongPassword("Password123")
        assertNotNull("Una contraseña sin símbolo debe retornar error", resultado)
        assertEquals("Debe incluir un símbolo", resultado)
    }

    @Test
    fun `validateStrongPassword retorna error cuando tiene espacios`() {
        val resultado = validateStrongPassword("Password 123!")
        assertNotNull("Una contraseña con espacios debe retornar error", resultado)
        assertEquals("No debe contener espacios", resultado)
    }

    // ==================== TESTS DE CONFIRMACIÓN DE CONTRASEÑA ====================
    
    @Test
    fun `validateConfirm retorna null cuando las contraseñas coinciden`() {
        val resultado = validateConfirm("Password123!", "Password123!")
        assertNull("Contraseñas iguales no deben retornar error", resultado)
    }

    @Test
    fun `validateConfirm retorna error cuando las contraseñas no coinciden`() {
        val resultado = validateConfirm("Password123!", "Password456!")
        assertNotNull("Contraseñas diferentes deben retornar error", resultado)
        assertEquals("Las contraseñas no coinciden", resultado)
    }

    @Test
    fun `validateConfirm retorna error cuando la confirmación está vacía`() {
        val resultado = validateConfirm("Password123!", "")
        assertNotNull("Confirmación vacía debe retornar error", resultado)
        assertEquals("Confirma tu contraseña", resultado)
    }

    // ==================== TESTS DE VALIDACIÓN DE LIBROS ====================
    
    @Test
    fun `validateBookTitle retorna null cuando el título es válido`() {
        val resultado = validateBookTitle("El Quijote")
        assertNull("Un título válido no debe retornar error", resultado)
    }

    @Test
    fun `validateBookTitle retorna error cuando está vacío`() {
        val resultado = validateBookTitle("")
        assertNotNull("Un título vacío debe retornar error", resultado)
        assertEquals("Este campo es obligatorio", resultado)
    }

    @Test
    fun `validateBookTitle retorna error cuando es muy corto`() {
        val resultado = validateBookTitle("A")
        assertNotNull("Un título muy corto debe retornar error", resultado)
        assertEquals("Debe tener al menos 2 caracteres", resultado)
    }

    @Test
    fun `validateISBN retorna null cuando el ISBN es válido de 10 dígitos`() {
        val resultado = validateISBN("1234567890")
        assertNull("Un ISBN de 10 dígitos debe ser válido", resultado)
    }

    @Test
    fun `validateISBN retorna null cuando el ISBN es válido de 13 dígitos`() {
        val resultado = validateISBN("1234567890123")
        assertNull("Un ISBN de 13 dígitos debe ser válido", resultado)
    }

    @Test
    fun `validateISBN acepta guiones en el ISBN`() {
        val resultado = validateISBN("123-456-789-0")
        assertNull("Un ISBN con guiones debe ser válido", resultado)
    }

    @Test
    fun `validateISBN retorna error cuando tiene letras`() {
        val resultado = validateISBN("123456789a")
        assertNotNull("Un ISBN con letras debe retornar error", resultado)
    }

    @Test
    fun `validateISBN retorna error cuando no tiene 10 ni 13 dígitos`() {
        val resultado = validateISBN("12345")
        assertNotNull("Un ISBN con longitud inválida debe retornar error", resultado)
        assertEquals("Ingresa un ISBN válido (10 o 13 dígitos)", resultado)
    }

    @Test
    fun `validateBookYear retorna null cuando el año es válido`() {
        val anioActual = 2024
        val resultado = validateBookYear(2020, anioActual)
        assertNull("Un año válido no debe retornar error", resultado)
    }

    @Test
    fun `validateBookYear retorna error cuando el año es muy antiguo`() {
        val anioActual = 2024
        val resultado = validateBookYear(1800, anioActual)
        assertNotNull("Un año muy antiguo debe retornar error", resultado)
    }

    @Test
    fun `validateBookYear retorna error cuando el año es futuro`() {
        val anioActual = 2024
        val resultado = validateBookYear(2030, anioActual)
        assertNotNull("Un año futuro debe retornar error", resultado)
    }

    @Test
    fun `validateStock retorna null cuando el stock es válido`() {
        val resultado = validateStock(10)
        assertNull("Un stock válido no debe retornar error", resultado)
    }

    @Test
    fun `validateStock retorna null cuando el stock es cero`() {
        val resultado = validateStock(0)
        assertNull("Un stock de cero debe ser válido", resultado)
    }

    @Test
    fun `validateStock retorna error cuando el stock es negativo`() {
        val resultado = validateStock(-5)
        assertNotNull("Un stock negativo debe retornar error", resultado)
        assertEquals("El stock no puede ser negativo", resultado)
    }

    @Test
    fun `validateStockAgainstLoaned retorna null cuando el stock es suficiente`() {
        val resultado = validateStockAgainstLoaned(10, 5)
        assertNull("Stock suficiente no debe retornar error", resultado)
    }

    @Test
    fun `validateStockAgainstLoaned retorna error cuando el stock es menor a prestados`() {
        val resultado = validateStockAgainstLoaned(5, 10)
        assertNotNull("Stock menor a prestados debe retornar error", resultado)
    }

    @Test
    fun `validateDisponibles retorna null cuando los disponibles son válidos`() {
        val resultado = validateDisponibles(5, 10)
        assertNull("Disponibles válidos no deben retornar error", resultado)
    }

    @Test
    fun `validateDisponibles retorna error cuando son negativos`() {
        val resultado = validateDisponibles(-1, 10)
        assertNotNull("Disponibles negativos deben retornar error", resultado)
        assertEquals("Los disponibles no pueden ser negativos", resultado)
    }

    @Test
    fun `validateDisponibles retorna error cuando son mayores al stock`() {
        val resultado = validateDisponibles(15, 10)
        assertNotNull("Disponibles mayores al stock deben retornar error", resultado)
        assertEquals("Los disponibles no pueden ser mayores al stock", resultado)
    }

    @Test
    fun `validateBookDescription retorna null cuando está vacía`() {
        val resultado = validateBookDescription("")
        assertNull("Descripción vacía debe ser válida (es opcional)", resultado)
    }

    @Test
    fun `validateBookDescription retorna null cuando tiene más de 10 caracteres`() {
        val resultado = validateBookDescription("Esta es una descripción válida")
        assertNull("Descripción con más de 10 caracteres debe ser válida", resultado)
    }

    @Test
    fun `validateBookDescription retorna error cuando es muy corta`() {
        val resultado = validateBookDescription("Corta")
        assertNotNull("Descripción muy corta debe retornar error", resultado)
        assertEquals("La descripción debe tener al menos 10 caracteres", resultado)
    }

    @Test
    fun `validateLoanDates retorna null cuando las fechas son válidas`() {
        val resultado = validateLoanDates("2024-01-01", "2024-01-15")
        assertNull("Fechas válidas no deben retornar error", resultado)
    }

    @Test
    fun `validateLoanDates retorna null cuando las fechas son iguales`() {
        val resultado = validateLoanDates("2024-01-01", "2024-01-01")
        assertNull("Fechas iguales deben ser válidas", resultado)
    }

    @Test
    fun `validateLoanDates retorna error cuando la fecha de préstamo es posterior`() {
        val resultado = validateLoanDates("2024-01-15", "2024-01-01")
        assertNotNull("Fecha de préstamo posterior debe retornar error", resultado)
        assertEquals("La fecha de préstamo no puede ser posterior a la fecha de devolución", resultado)
    }

    @Test
    fun `validateLoanDates retorna error cuando el formato es inválido`() {
        val resultado = validateLoanDates("01-01-2024", "15-01-2024")
        assertNotNull("Formato de fecha inválido debe retornar error", resultado)
        assertEquals("Formato de fecha inválido", resultado)
    }
}

