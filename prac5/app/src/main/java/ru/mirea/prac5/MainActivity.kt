package ru.mirea.prac5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.work.*
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
 * Маршруты для навигации
 */
object Routes {
    const val MAIN = "main"
    const val SAVED_PHOTOS = "saved_photos"
    const val SAVED_IMAGES = "saved_images"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var unsplashService: UnsplashApiService

    @Inject
    lateinit var db: AppDatabase

    // Ключ доступа к Unsplash API
    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"

    // Состояния для главного экрана
    private var imageUrl by mutableStateOf("")
    private var downloadedBitmap by mutableStateOf<Bitmap?>(null)

    // Состояние для сохранённых фото (Room)
    private var savedPhotosList by mutableStateOf<List<PhotoEntity>>(emptyList())

    // Состояние для сохранённых картинок (внутренняя память)
    private var savedImageFiles by mutableStateOf<List<File>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Подписка на Flow из БД (SavedPhotosScreen)
        lifecycleScope.launch {
            db.photoDao().getAllPhotos().collect { photos ->
                savedPhotosList = photos
            }
        }

        // Загрузка файлов из внутренней памяти (SavedImagesScreen)
        savedImageFiles = filesDir.listFiles { file ->
            file.name.endsWith(".png") || file.name.endsWith(".jpg")
        }?.toList() ?: emptyList()

        setContent {
            val navController = rememberNavController()
            Scaffold(
                topBar = { TopAppBar(title = { Text("Пример навигации и WorkManager") }) },
                bottomBar = {
                    BottomNavigation {
                        BottomNavigationItem(
                            selected = (navController.currentBackStackEntry?.destination?.route == Routes.MAIN),
                            onClick = { navController.navigate(Routes.MAIN) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Main") },
                            label = { Text("Main") }
                        )
                        BottomNavigationItem(
                            selected = (navController.currentBackStackEntry?.destination?.route == Routes.SAVED_PHOTOS),
                            onClick = { navController.navigate(Routes.SAVED_PHOTOS) },
                            icon = { Icon(Icons.Default.List, contentDescription = "SavedPhotos") },
                            label = { Text("Photos") }
                        )
                        BottomNavigationItem(
                            selected = (navController.currentBackStackEntry?.destination?.route == Routes.SAVED_IMAGES),
                            onClick = { navController.navigate(Routes.SAVED_IMAGES) },
                            icon = { Icon(Icons.Default.Image, contentDescription = "SavedImages") },
                            label = { Text("Images") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavHost(
                        navController = navController,
                        imageUrl = imageUrl,
                        downloadedBitmap = downloadedBitmap,
                        onLoadRandomPhoto = { loadRandomPhoto() },
                        onDownloadImageFromLink = { url -> downloadImageFromLink(url) },
                        savedPhotos = savedPhotosList,
                        savedImageFiles = savedImageFiles,
                        // функция для обновления списка файлов после worker
                        refreshSavedFiles = {
                            savedImageFiles = filesDir.listFiles { file ->
                                file.name.endsWith(".png") || file.name.endsWith(".jpg")
                            }?.toList() ?: emptyList()
                        }
                    )
                }
            }
        }
    }

    /** Загрузить случайное фото с Unsplash и сохранить в БД **/
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

    /** Загрузить картинку по ссылке, сохранить во внутреннюю память **/
    private fun downloadImageFromLink(url: String) {
        lifecycleScope.launch {
            try {
                if (url.isNotBlank()) {
                    val bitmap = withContext(Dispatchers.IO) {
                        downloadBitmap(url)
                    }
                    if (bitmap != null) {
                        val filename = "image_${System.currentTimeMillis()}.png"
                        saveBitmapToInternalStorage(bitmap, filename)

                        downloadedBitmap = bitmap
                        imageUrl = ""

                        Toast.makeText(this@MainActivity, "Изображение сохранено во внутреннюю память", Toast.LENGTH_SHORT).show()

                        // Обновим список сохранённых файлов
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

    private fun downloadBitmap(url: String): Bitmap? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Ошибка сети: $response")
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

/** Функция создаёт NavHost и навигационные графы для трёх экранов */
@Composable
fun AppNavHost(
    navController: NavHostController,
    imageUrl: String,
    downloadedBitmap: Bitmap?,
    onLoadRandomPhoto: () -> Unit,
    onDownloadImageFromLink: (String) -> Unit,
    savedPhotos: List<PhotoEntity>,
    savedImageFiles: List<File>,
    refreshSavedFiles: () -> Unit
) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(route = Routes.MAIN) {
            MainScreenUI(
                imageUrl = imageUrl,
                downloadedBitmap = downloadedBitmap,
                onLoadRandomPhoto = onLoadRandomPhoto,
                onDownloadImageFromLink = onDownloadImageFromLink,
                // Добавим кнопку, запускающую WorkManager:
                onStartWorker = {
                    val workManager = WorkManager.getInstance(it)
                    val photoUrl = "https://random.imagecdn.app/500/500"
                    // Пакуем url в inputData
                    val inputData = workDataOf("PHOTO_URL" to photoUrl)

                    val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setInputData(inputData)
                        .build()

                    workManager.enqueue(request)

                    Toast.makeText(it, "WorkManager запущен, фото сгенерируется в фоне", Toast.LENGTH_SHORT).show()
                },
                refreshSavedFiles = refreshSavedFiles
            )
        }
        composable(route = Routes.SAVED_PHOTOS) {
            SavedPhotosScreen(photos = savedPhotos)
        }
        composable(route = Routes.SAVED_IMAGES) {
            SavedImagesScreen(imageFiles = savedImageFiles)
        }
    }
}

// ---------- Экраны ---------- //

/** Главный экран. Добавлена кнопка запуска Worker. */
@Composable
fun MainScreenUI(
    imageUrl: String,
    downloadedBitmap: Bitmap?,
    onLoadRandomPhoto: () -> Unit,
    onDownloadImageFromLink: (String) -> Unit,
    onStartWorker: (android.content.Context) -> Unit,
    refreshSavedFiles: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var downloadUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        // Отображение изображения
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUrl.isNotEmpty() -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                downloadedBitmap != null -> {
                    Image(
                        bitmap = downloadedBitmap.asImageBitmap(),
                        contentDescription = null,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка запуска Worker
        Button(onClick = {
            onStartWorker(context)
            // Через пару секунд Worker скачает файл. Обновим SavedImages:
            // (можно подождать Worker, но проще вызвать refreshSavedFiles() позже)
            refreshSavedFiles()
        }) {
            Text("Запустить WorkManager")
        }
    }
}

/** Экран со списком сохранённых данных о фото (Room) */
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
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("Автор: ${photo.author}")
                Text("Размер: ${photo.width} x ${photo.height}")
                Text("Дата: ${photo.date}")
                Divider()
            }
        }
    }
}

/** Экран со списком сохранённых изображений */
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