package com.empresa.libra_users.domain.validation

// Valida que el email no esté vacío y cumpla patrón de email
fun validateEmail(email: String): String? {                            // Retorna String? (mensaje) o null si está OK
    if (email.isBlank()) return "El email es obligatorio"              // Regla 1: no vacío
    // Usamos regex simple que funciona en tests unitarios y Android
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    val ok = emailRegex.matches(email)                                 // Regla 2: coincide con patrón de email
    if (!ok) return "Formato de email inválido"                         // Si no cumple formato, devolvemos mensaje
    if (!email.endsWith("@gmail.com", ignoreCase = true)) return "Debe ser @gmail.com"  // Regla 3: debe ser gmail.com
    return null                                                          // OK
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {                   // Valida nombre
    if (name.isBlank()) return "El nombre es obligatorio"              // Regla 1: no vacío
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla 2: solo letras y espacios (con tildes/ñ)
    return if (!regex.matches(name)) "Solo letras y espacios" else null// Mensaje si falla
}

// Valida que el teléfono tenga solo dígitos y una longitud razonable
// Retorna Int: 0=OK, 1=vacío, 2=no solo dígitos, 3=longitud inválida
private fun validatePhoneDigitsOnlyInt(phone: String): Int {
    if (phone.isBlank()) return 1           // Regla 1: no vacío
    if (!phone.all { it.isDigit() }) return 2             // Regla 2: todos dígitos
    if (phone.length !in 8..15) return 3 // Regla 3: tamaño razonable
    return 0                                                          // OK
}

// Convierte código de error a mensaje de texto


private fun getPhoneErrorMessage(code: Int): String? {
    return when (code) {
        1 -> "El teléfono es obligatorio"
        2 -> "Solo números"
        3 -> "Debe tener entre 8 y 15 dígitos"
        else -> null
    }
}

// Función pública que mantiene compatibilidad (retorna String?)
// Las pantallas y ViewModels siguen funcionando sin cambios
fun validatePhoneDigitsOnly(phone: String): String? {
    val code = validatePhoneDigitsOnlyInt(phone)
    return getPhoneErrorMessage(code)
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {                    // Requisitos mínimos de seguridad
    if (pass.isBlank()) return "La contraseña es obligatoria"          // No vacío
    if (pass.length < 8) return "Mínimo 8 caracteres"                  // Largo mínimo
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula" // Al menos 1 mayúscula
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula" // Al menos 1 minúscula
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"         // Al menos 1 número
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo" // Al menos 1 símbolo
    if (pass.contains(' ')) return "No debe contener espacios"          // Sin espacios
    return null                                                         // OK
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {          // Confirmación de contraseña
    if (confirm.isBlank()) return "Confirma tu contraseña"             // No vacío
    return if (pass != confirm) "Las contraseñas no coinciden" else null // Deben ser iguales
}

// ============================================================================
// VALIDACIONES PARA LIBROS
// ============================================================================

// Valida título, autor, categoría, editorial: obligatorios, mínimo 2 caracteres
fun validateBookTitle(title: String): String? {
    if (title.isBlank()) return "Este campo es obligatorio"
    if (title.length < 2) return "Debe tener al menos 2 caracteres"
    return null
}

fun validateBookAuthor(author: String): String? {
    if (author.isBlank()) return "Este campo es obligatorio"
    if (author.length < 2) return "Debe tener al menos 2 caracteres"
    return null
}

fun validateBookCategory(categoria: String): String? {
    if (categoria.isBlank()) return "Este campo es obligatorio"
    if (categoria.length < 2) return "Debe tener al menos 2 caracteres"
    return null
}

fun validateBookPublisher(publisher: String): String? {
    if (publisher.isBlank()) return "Este campo es obligatorio"
    if (publisher.length < 2) return "Debe tener al menos 2 caracteres"
    return null
}

// Valida ISBN: obligatorio, 10 o 13 dígitos (permitir guiones, normalizar)
fun validateISBN(isbn: String): String? {
    if (isbn.isBlank()) return "Este campo es obligatorio"
    // Normalizar: quitar guiones y espacios
    val normalized = isbn.replace("-", "").replace(" ", "")
    // Verificar que solo contenga dígitos
    if (!normalized.all { it.isDigit() }) return "El ISBN solo debe contener dígitos y guiones"
    // Validar longitud: 10 o 13 dígitos
    if (normalized.length != 10 && normalized.length != 13) {
        return "Ingresa un ISBN válido (10 o 13 dígitos)"
    }
    return null
}

// Valida año: numérico entre 1900 y el año actual
fun validateBookYear(anio: Int, anioActual: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)): String? {
    if (anio < 1900 || anio > anioActual) {
        return "El año debe estar entre 1900 y $anioActual"
    }
    return null
}

// Valida stock: entero ≥ 0
fun validateStock(stock: Int): String? {
    if (stock < 0) return "El stock no puede ser negativo"
    return null
}

// Valida que el stock no sea menor a los ejemplares prestados
fun validateStockAgainstLoaned(stock: Int, prestados: Int): String? {
    if (stock < prestados) {
        return "No puedes fijar un stock menor a los ejemplares actualmente prestados ($prestados)"
    }
    return null
}

// Valida que disponibles <= stock
fun validateDisponibles(disponibles: Int, stock: Int): String? {
    if (disponibles < 0) return "Los disponibles no pueden ser negativos"
    if (disponibles > stock) return "Los disponibles no pueden ser mayores al stock"
    return null
}

// Valida descripción (opcional, pero si se proporciona debe tener al menos 10 caracteres)
fun validateBookDescription(descripcion: String): String? {
    if (descripcion.isNotBlank() && descripcion.length < 10) {
        return "La descripción debe tener al menos 10 caracteres"
    }
    return null
}

// Valida fechas de préstamo: fechaPrestamo ≤ fechaDevolucion
fun validateLoanDates(fechaPrestamo: String, fechaDevolucion: String): String? {
    try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaP = java.time.LocalDate.parse(fechaPrestamo, formatter)
        val fechaD = java.time.LocalDate.parse(fechaDevolucion, formatter)
        if (fechaP.isAfter(fechaD)) {
            return "La fecha de préstamo no puede ser posterior a la fecha de devolución"
        }
        return null
    } catch (e: Exception) {
        return "Formato de fecha inválido"
    }
}