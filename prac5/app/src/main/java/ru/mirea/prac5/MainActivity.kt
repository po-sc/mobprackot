package ru.mirea.prac5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Photo
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
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Модель экрана (sealed class):
 * - Main (главный экран с загрузкой фото)
 * - SavedPhotos (экран со списком сохранённых данных из БД)
 * - SavedImages (экран со списком сохранённых изображений)
 */
sealed class Screen(val title: String) {
    object Main : Screen("Main Screen")
    object SavedPhotos : Screen("Saved Photos")
    object SavedImages : Screen("Saved Images")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var unsplashService: UnsplashApiService

    @Inject
    lateinit var db: AppDatabase

    // Ключ доступа к Unsplash API
    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"

    // Состояния для главного экрана:
    private var imageUrl by mutableStateOf("")
    private var downloadedBitmap by mutableStateOf<Bitmap?>(null)

    // Состояние для сохранённых фото (Room)
    private var savedPhotosList by mutableStateOf<List<PhotoEntity>>(emptyList())

    // Состояние для сохранённых картинок (из внутренней памяти)
    private var savedImageFiles by mutableStateOf<List<File>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загружаем файлы из внутренней памяти (SavedImagesScreen)
        savedImageFiles = filesDir.listFiles { file ->
            file.name.endsWith(".png") || file.name.endsWith(".jpg")
        }?.toList() ?: emptyList()

        // Подписка на Flow из БД (SavedPhotosScreen):
        lifecycleScope.launch {
            db.photoDao().getAllPhotos().collect { photos ->
                savedPhotosList = photos
            }
        }

        setContent {
            // Текущее состояние экрана
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

            MainAppScaffold(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreen = screen
                }
            ) {
                // Отображаем разные экраны в зависимости от currentScreen
                when (currentScreen) {
                    Screen.Main -> {
                        MainScreenUI(
                            imageUrl = imageUrl,
                            downloadedBitmap = downloadedBitmap,
                            onLoadRandomPhoto = { loadRandomPhoto() },
                            onDownloadImageFromLink = { url -> downloadImageFromLink(url) }
                        )
                    }
                    Screen.SavedPhotos -> {
                        SavedPhotosScreen(photos = savedPhotosList)
                    }
                    Screen.SavedImages -> {
                        SavedImagesScreen(imageFiles = savedImageFiles)
                    }
                }
            }
        }
    }

    /**
     * Загрузка случайного фото с Unsplash
     */
    private fun loadRandomPhoto() {
        val call = unsplashService.getRandomPhoto(ACCESS_KEY)

        call.enqueue(object : Callback<UnsplashPhoto> {
            override fun onResponse(call: Call<UnsplashPhoto>, response: Response<UnsplashPhoto>) {
                if (response.isSuccessful) {
                    val photo = response.body()
                    lifecycleScope.launch {
                        photo?.let {
                            // Сохраним данные о фото в Room (PhotoEntity)
                            val photoEntity = PhotoEntity(
                                author = it.user.name,
                                width = it.width,
                                height = it.height,
                                date = it.created_at
                            )
                            db.photoDao().insertPhoto(photoEntity)
                            Toast.makeText(this@MainActivity, "Данные о фото сохранены", Toast.LENGTH_SHORT).show()

                            // Обновим UI для отображения картинки
                            imageUrl = it.urls.regular
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

    /**
     * Загрузка картинки по ссылке (downloadUrl), сохранение во внутреннюю память
     */
    private fun downloadImageFromLink(url: String) {
        lifecycleScope.launch {
            try {
                if (url.isNotBlank()) {
                    val bitmap = withContext(Dispatchers.IO) {
                        downloadBitmap(url)
                    }
                    if (bitmap != null) {
                        // Сохраняем загруженный Bitmap во внутреннюю память
                        val filename = "image_${System.currentTimeMillis()}.png"
                        saveBitmapToInternalStorage(bitmap, filename)

                        // Обновляем состояние, чтобы показать картинку в UI
                        downloadedBitmap = bitmap
                        imageUrl = ""

                        Toast.makeText(this@MainActivity, "Изображение сохранено во внутреннюю память", Toast.LENGTH_SHORT).show()

                        // Перезагрузим список файлов для SavedImagesScreen
                        savedImageFiles = filesDir.listFiles { file ->
                            file.name.endsWith(".png") || file.name.endsWith(".jpg")
                        }?.toList() ?: emptyList()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Введите URL изображения", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Скачать Bitmap по ссылке (через OkHttp)
     */
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

    /**
     * Сохранить Bitmap во внутреннюю память
     */
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

// ---------- Scaffold и навигация ---------- //

// Подтверждаем использование экспериментальных API Material и Material3
@OptIn(ExperimentalMaterialApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val screens = listOf(Screen.Main, Screen.SavedPhotos, Screen.SavedImages)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) }
            )
        },
        drawerContent = {
            Text("Меню", modifier = Modifier.padding(16.dp))
            Divider()
            screens.forEach { screen ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onScreenSelected(screen)
                        },
                    icon = {
                        when (screen) {
                            is Screen.Main -> Icon(Icons.Default.Photo, contentDescription = null)
                            is Screen.SavedPhotos -> Icon(Icons.Default.List, contentDescription = null)
                            is Screen.SavedImages -> Icon(Icons.Default.Image, contentDescription = null)
                        }
                    },
                    secondaryText = { Text(screen.title) }
                ) {
                    Text(screen.title)
                }
            }
        },
        bottomBar = {
            BottomAppBar {
                screens.forEach { screen ->
                    BottomNavigationItem(
                        selected = (screen == currentScreen),
                        onClick = { onScreenSelected(screen) },
                        icon = {
                            when (screen) {
                                is Screen.Main -> Icon(Icons.Default.Photo, contentDescription = null)
                                is Screen.SavedPhotos -> Icon(Icons.Default.List, contentDescription = null)
                                is Screen.SavedImages -> Icon(Icons.Default.Image, contentDescription = null)
                            }
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

// ---------- Экраны ---------- //

/**
 * Главный экран (MainScreen)
 */
@Composable
fun MainScreenUI(
    imageUrl: String,
    downloadedBitmap: Bitmap?,
    onLoadRandomPhoto: () -> Unit,
    onDownloadImageFromLink: (String) -> Unit
) {
    var downloadUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Поле с постоянной ссылкой, только для копирования
        TextField(
            value = "https://random-image-pepebigotes.vercel.app/api/random-image",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Поле для ввода произвольной ссылки
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

        // Область для отображения изображения
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUrl.isNotEmpty() -> {
                    // Показ изображения по URL через Coil
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Loaded image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                downloadedBitmap != null -> {
                    // Показ скачанного Bitmap
                    Image(
                        bitmap = downloadedBitmap.asImageBitmap(),
                        contentDescription = "Downloaded image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Text("No image loaded")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onLoadRandomPhoto() }) {
            Text("Download random photo")
        }
    }
}

/**
 * Экран со списком данных о фото из БД
 */
@Composable
fun SavedPhotosScreen(photos: List<PhotoEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Saved Photos", modifier = Modifier.padding(bottom = 16.dp))
        photos.forEach { photo ->
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Автор: ${photo.author}")
                Text("Размер: ${photo.width} x ${photo.height}")
                Text("Дата: ${photo.date}")
                Divider()
            }
        }
    }
}

/**
 * Экран со списком сохранённых изображений из внутренней памяти
 */
@Composable
fun SavedImagesScreen(imageFiles: List<File>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Сохранённые Изображения", modifier = Modifier.padding(bottom = 16.dp))
        imageFiles.forEach { file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Saved image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}