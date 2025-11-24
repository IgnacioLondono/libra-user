package com.empresa.libra_users.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val author: String,
    val isbn: String,
    val categoryId: Long, // Relación con categoría (usado para filtrar en el catálogo)
    val categoria: String = "", // Nombre de la categoría en español
    val publisher: String,
    val publishDate: String,
    val anio: Int = 0, // Año de publicación
    val status: String,  // 'Available', 'Loaned', 'Damaged', 'Retired'
    val inventoryCode: String,
    val stock: Int = 0, // Cantidad total de ejemplares
    val disponibles: Int = 0, // Cantidad de ejemplares disponibles
    val descripcion: String = "", // Descripción corta del libro
    val coverUrl: String = "",
    val homeSection: String = "None" // Nueva propiedad para la sección en HomeScreen
)