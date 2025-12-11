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
        // Validar que la fecha de préstamo no sea en el pasado (opcional, pero recomendado)
        val hoy = java.time.LocalDate.now()
        if (fechaP.isBefore(hoy)) {
            return "La fecha de préstamo no puede ser anterior a hoy"
        }
        return null
    } catch (e: Exception) {
        return "Formato de fecha inválido"
    }
}

// Valida días de préstamo: debe ser entre 1 y 30 días
fun validateLoanDays(days: Int): String? {
    if (days < 1) return "El préstamo debe ser de al menos 1 día"
    if (days > 30) return "El préstamo no puede exceder 30 días"
    return null
}

// Valida que el usuario esté autenticado
fun validateUserAuthenticated(userId: Long?): String? {
    if (userId == null || userId <= 0) {
        return "Debes estar autenticado para realizar esta acción"
    }
    return null
}

// Valida que el usuario tenga permisos de administrador
fun validateAdminRole(userRole: String?): String? {
    if (userRole != "admin") {
        return "No tienes permisos de administrador para realizar esta acción"
    }
    return null
}

// Valida que el libro exista y esté disponible
fun validateBookAvailable(book: com.empresa.libra_users.data.local.user.BookEntity?): String? {
    if (book == null) {
        return "El libro no existe"
    }
    if (book.status != "Available") {
        return "El libro no está disponible para préstamo"
    }
    if (book.disponibles <= 0) {
        return "No hay ejemplares disponibles de este libro"
    }
    return null
}

// Valida formato de imagen Base64 (básico)
fun validateBase64Image(base64: String?): String? {
    if (base64 == null) return null // Opcional
    if (base64.isBlank()) return null // Opcional
    
    // Validar que comience con data:image o sea solo Base64
    val isDataUri = base64.startsWith("data:image/")
    val isBase64Only = base64.matches(Regex("^[A-Za-z0-9+/=]+$"))
    
    if (!isDataUri && !isBase64Only) {
        return "Formato de imagen inválido"
    }
    
    // Validar tamaño máximo (aproximado: 5MB en Base64 = ~6.7MB en texto)
    if (base64.length > 6_700_000) {
        return "La imagen es demasiado grande (máximo 5MB)"
    }
    
    return null
}

// Valida que un ID sea válido
fun validateId(id: Long?): String? {
    if (id == null || id <= 0) {
        return "ID inválido"
    }
    return null
}

// Valida que un ID de string sea válido
fun validateIdString(id: String?): String? {
    if (id.isNullOrBlank()) {
        return "ID inválido"
    }
    val idLong = id.toLongOrNull()
    if (idLong == null || idLong <= 0) {
        return "ID inválido"
    }
    return null
}