package ru.mirea.prac5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

class SavedImagesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageFiles = filesDir.listFiles { file ->
            file.name.endsWith(".png") || file.name.endsWith(".jpg")
        }?.toList() ?: emptyList()

        setContent {
            SavedImagesScreen(imageFiles)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedImagesScreen(images: List<File>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Сохранённые Изображения", modifier = Modifier.padding(bottom = 16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(images) { imageFile ->
                val bitmapPath = imageFile.absolutePath
                AsyncImage(
                    model = bitmapPath,
                    contentDescription = "Saved image",
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}
