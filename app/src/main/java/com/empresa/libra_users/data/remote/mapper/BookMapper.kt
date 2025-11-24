package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.remote.dto.BookDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun BookDto.toEntity(): BookEntity {
    // Extraer año de createdAt o usar el campo year
    val publicationYear = year ?: try {
        LocalDate.parse(createdAt.substringBefore("T"), dateFormatter).year
    } catch (e: Exception) {
        LocalDate.now().year
    }
    
    // Convertir fecha al formato esperado
    val publishDate = if (year != null) "$year-01-01" else createdAt.substringBefore("T")
    
    // Mapear status
    val mappedStatus = when {
        availableCopies > 0 -> "Available"
        status.contains("retired", ignoreCase = true) -> "Retired"
        status.contains("damaged", ignoreCase = true) -> "Damaged"
        else -> "Loaned"
    }
    
    return BookEntity(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        author = author,
        isbn = isbn,
        categoryId = 0L, // Necesitarás mapear la categoría correctamente
        categoria = category,
        publisher = publisher ?: "",
        publishDate = publishDate,
        anio = publicationYear,
        status = mappedStatus,
        inventoryCode = id, // Usar el ID del backend como código de inventario
        stock = totalCopies,
        disponibles = availableCopies,
        descripcion = description ?: "",
        coverUrl = coverUrl ?: "",
        homeSection = if (featured) "Featured" else "None"
    )
}

fun BookEntity.toDto(): BookDto {
    // Extraer año de publishDate o usar anio
    val publicationYear = if (anio > 0) anio else {
        try {
            LocalDate.parse(publishDate, dateFormatter).year
        } catch (e: Exception) {
            LocalDate.now().year
        }
    }
    
    // Mapear status
    val mappedStatus = when {
        status.contains("Available", ignoreCase = true) -> "Available"
        status.contains("Retired", ignoreCase = true) -> "Retired"
        status.contains("Damaged", ignoreCase = true) -> "Damaged"
        else -> "Loaned"
    }
    
    return BookDto(
        id = if (id > 0) id.toString() else "",
        title = title,
        author = author,
        isbn = isbn,
        category = categoria,
        publisher = publisher.ifEmpty { null },
        year = publicationYear,
        description = descripcion.ifEmpty { null },
        coverUrl = coverUrl.ifEmpty { null },
        status = mappedStatus,
        totalCopies = stock,
        availableCopies = disponibles,
        price = null, // No disponible en Entity local
        featured = homeSection == "Featured",
        createdAt = publishDate,
        updatedAt = publishDate
    )
}
