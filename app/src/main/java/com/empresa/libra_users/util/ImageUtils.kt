package com.empresa.libra_users.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {
    
    /**
     * Convierte un Bitmap a Base64 con formato data URI
     * @param bitmap La imagen a convertir
     * @param format Formato de imagen (Bitmap.CompressFormat.PNG o JPEG)
     * @param quality Calidad de compresión (0-100, solo para JPEG)
     * @return String Base64 con prefijo data URI
     */
    suspend fun bitmapToBase64(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 85
    ): String = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        
        val mimeType = when (format) {
            Bitmap.CompressFormat.PNG -> "image/png"
            Bitmap.CompressFormat.JPEG -> "image/jpeg"
            Bitmap.CompressFormat.WEBP -> "image/webp"
            else -> "image/jpeg"
        }
        
        "data:$mimeType;base64,$base64"
    }
    
    /**
     * Convierte un Bitmap a Base64 sin prefijo data URI (solo el string base64)
     * @param bitmap La imagen a convertir
     * @param format Formato de imagen
     * @param quality Calidad de compresión
     * @return String Base64 sin prefijo
     */
    suspend fun bitmapToBase64Only(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 85
    ): String = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }
    
    /**
     * Carga un Bitmap desde un Uri (desde galería o cámara)
     * @param context Contexto de la aplicación
     * @param uri Uri de la imagen
     * @param maxWidth Ancho máximo deseado (para reducir tamaño)
     * @param maxHeight Alto máximo deseado (para reducir tamaño)
     * @return Bitmap o null si hay error
     */
    suspend fun loadBitmapFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // Calcular escala para reducir tamaño
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            
            val inputStream2 = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()
            
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Carga un Bitmap desde un Uri usando MediaStore (más eficiente)
     */
    suspend fun loadBitmapFromUriMediaStore(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Intentar usar MediaStore primero
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val filePath = it.getString(columnIndex)
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(filePath, options)
                    options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
                    options.inJustDecodeBounds = false
                    BitmapFactory.decodeFile(filePath, options)
                } else {
                    null
                }
            } ?: loadBitmapFromUri(context, uri, maxWidth, maxHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            loadBitmapFromUri(context, uri, maxWidth, maxHeight)
        }
    }
    
    /**
     * Calcula el factor de escala para reducir el tamaño de la imagen
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Redimensiona un Bitmap manteniendo la proporción
     */
    suspend fun resizeBitmap(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap = withContext(Dispatchers.IO) {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return@withContext bitmap
        }
        
        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Convierte un Uri a Base64 (método completo)
     * @param context Contexto de la aplicación
     * @param uri Uri de la imagen
     * @param maxWidth Ancho máximo (default 800)
     * @param maxHeight Alto máximo (default 800)
     * @param quality Calidad de compresión JPEG (default 85)
     * @return String Base64 con data URI o null si hay error
     */
    suspend fun uriToBase64(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 85
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Cargar bitmap desde Uri
            val bitmap = loadBitmapFromUriMediaStore(context, uri, maxWidth, maxHeight)
                ?: return@withContext null
            
            // Redimensionar si es necesario
            val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)
            
            // Convertir a Base64 con data URI
            bitmapToBase64(resizedBitmap, Bitmap.CompressFormat.JPEG, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}




