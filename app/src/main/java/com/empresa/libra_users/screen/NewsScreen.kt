package com.empresa.libra_users.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class NewsArticle(
    val title: String,
    val summary: String,
    val imageUrl: String
)

val sampleNews = listOf(
    NewsArticle(
        "La biblioteca inaugura una nueva sección de ciencia ficción",
        "Explora nuevos mundos con nuestra colección expandida de clásicos y novedades del género.",
        "https://thumbs.dreamstime.com/b/libros-de-las-ciencia-ficci%C3%B3n-en-biblioteca-37541982.jpg"
    ),
    NewsArticle(
        "Taller de escritura creativa este fin de semana",
        "¿Siempre has querido escribir tu propia historia? Únete a nuestro taller gratuito este sábado.",
        "https://images.unsplash.com/photo-1455390582262-044cdead277a?q=80&w=1973&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    ),
    NewsArticle(
        "Conoce al autor: Entrevista con Joanne Rowling",
        "La aclamada autora de 'Harry Potter' nos visitará para una sesión de preguntas y respuestas.",
        "https://cloudfront-eu-central-1.images.arcpublishing.com/prisaradio/PNUXNLKILRLZ3OBAMXFP5HQX6Y.jpg"
    )
)

@Composable
fun NewsScreen() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(sampleNews) { article ->
            NewsArticleItem(article = article)
        }
    }
}

@Composable
fun NewsArticleItem(article: NewsArticle) {
    Card(
        modifier = Modifier.clickable { /* Handle click */ },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = article.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = article.summary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
