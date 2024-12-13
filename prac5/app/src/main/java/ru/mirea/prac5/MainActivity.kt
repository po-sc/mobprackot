package ru.mirea.prac5

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var unsplashService: UnsplashApiService

    @Inject
    lateinit var db: AppDatabase

    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"

    // Состояния для изображения:
    // imageUrl - для случайного фото с Unsplash (будет показывать через AsyncImage)
    // downloadedBitmap - для загруженного по ссылке фото (будет показывать через Image из Bitmap)
    private var imageUrl by mutableStateOf("")
    private var downloadedBitmap by mutableStateOf<Bitmap?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                imageUrl = imageUrl,
                downloadedBitmap = downloadedBitmap,
                onLoadRandomPhoto = { loadRandomPhoto() },
                onViewSaved = {
                    startActivity(Intent(this, SavedPhotosActivity::class.java))
                },
                onDownloadImageFromLink = { url ->
                    if (url.isNotBlank()) {
                        downloadImageFromLink(url)
                    } else {
                        Toast.makeText(this, "Введите URL изображения", Toast.LENGTH_SHORT).show()
                    }
                },
                onViewSavedImages = {
                    startActivity(Intent(this, SavedImagesActivity::class.java))
                }
            )
        }
    }

    private fun loadRandomPhoto() {
        val call = unsplashService.getRandomPhoto(ACCESS_KEY)

        call.enqueue(object : Callback<UnsplashPhoto> {
            override fun onResponse(call: Call<UnsplashPhoto>, response: Response<UnsplashPhoto>) {
                if (response.isSuccessful) {
                    val photo = response.body()
                    lifecycleScope.launch {
                        photo?.let {
                            val photoEntity = PhotoEntity(
                                author = it.user.name,
                                width = it.width,
                                height = it.height,
                                date = it.created_at
                            )
                            db.photoDao().insertPhoto(photoEntity)
                            Toast.makeText(this@MainActivity, "Данные о фото сохранены", Toast.LENGTH_SHORT).show()

                            // Обновляем imageUrl для отображения
                            imageUrl = it.urls.regular
                            // Сбросим bitmap, чтобы не мешалось
                            downloadedBitmap = null
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UnsplashPhoto>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Сетевая ошибка", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadImageFromLink(url: String) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "Запуск сетевого потока", Toast.LENGTH_SHORT).show()
                val bitmap = withContext(Dispatchers.IO) {
                    downloadBitmap(url)
                }
                Toast.makeText(this@MainActivity, "Сетевой поток завершен", Toast.LENGTH_SHORT).show()

                if (bitmap != null) {
                    Toast.makeText(this@MainActivity, "Запуск потока диск", Toast.LENGTH_SHORT).show()
                    val filename = "image_${System.currentTimeMillis()}.png"
                    saveBitmapToInternalStorage(bitmap, filename)
                    Toast.makeText(this@MainActivity, "Поток диск завершен", Toast.LENGTH_SHORT).show()

                    // Обновим состояние для отображения
                    downloadedBitmap = bitmap
                    // Сбросим imageUrl
                    imageUrl = ""
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Ошибка сети: $response")
            }
            val inputStream = response.body?.byteStream()
            return BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap, filename: String) {
        try {
            openFileOutput(filename, MODE_PRIVATE).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun MainScreen(
    imageUrl: String,
    downloadedBitmap: Bitmap?,
    onLoadRandomPhoto: () -> Unit,
    onViewSaved: () -> Unit,
    onDownloadImageFromLink: (String) -> Unit,
    onViewSavedImages: () -> Unit
) {
    var downloadUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // EditText (TextField) с изначально заданным текстом ссылки.
        // Пользователь может выделить и скопировать текст из поля.
        TextField(
            value = "https://random-image-pepebigotes.vercel.app/api/random-image",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = downloadUrl,
            onValueChange = { downloadUrl = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите URL изображения") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onDownloadImageFromLink(downloadUrl) }) {
            Text("Download photo from link")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Отображаем изображение, если загружено.
        // Если imageUrl не пуст - показываем AsyncImage
        // Если downloadedBitmap не null - показываем его
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUrl.isNotEmpty() -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Loaded image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                downloadedBitmap != null -> {
                    Image(
                        bitmap = downloadedBitmap.asImageBitmap(),
                        contentDescription = "Downloaded image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Если ничего не загружено, оставляем пустое место
                    Text("No image loaded", modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onLoadRandomPhoto() }) {
            Text("Download random photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onViewSaved() }) {
            Text("Photos data list")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onViewSavedImages() }) {
            Text("Saved images")
        }
    }
}



//https://random-image-pepebigotes.vercel.app/api/random-image