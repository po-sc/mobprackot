package ru.mirea.prac5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SavedPhotosActivity : ComponentActivity() {

    @Inject
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var photos by remember { mutableStateOf(emptyList<PhotoEntity>()) }

            // Загрузка данных из бд
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    db.photoDao().getAllPhotos().collect { list ->
                        photos = list
                    }
                }
            }

            SavedPhotosScreen(photos)
        }
    }
}

@Composable
fun SavedPhotosScreen(photos: List<PhotoEntity>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Saved Photos", modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn {
            items(photos) { photo ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Автор: ${photo.author}")
                    Text("Размер: ${photo.width}x${photo.height}")
                    Text("Дата: ${photo.date}")
                }
            }
        }
    }
}
