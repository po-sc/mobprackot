package ru.mirea.prac5

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log
import javax.inject.Inject
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var unsplashService: UnsplashApiService

    @Inject
    lateinit var db: AppDatabase

    private lateinit var imageView: ImageView
    private lateinit var buttonLoad: Button
    private lateinit var buttonViewSaved: Button
    private lateinit var buttonLinkDownload: Button
    private lateinit var buttonSavedImage: Button
    private lateinit var editTextLink: EditText

    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity)

        imageView = findViewById(R.id.imageView)
        buttonLoad = findViewById(R.id.buttonLoad)
        buttonViewSaved = findViewById(R.id.buttonViewSaved)
        buttonLinkDownload = findViewById(R.id.buttonlinkdownload)
        buttonSavedImage = findViewById(R.id.buttonSavedImage)
        editTextLink = findViewById(R.id.editTextLink)

        buttonLoad.setOnClickListener {
            loadRandomPhoto()
        }

        buttonViewSaved.setOnClickListener {
            val intent = Intent(this, SavedPhotosActivity::class.java)
            startActivity(intent)
        }

        buttonLinkDownload.setOnClickListener {
            val url = editTextLink.text.toString()
            if (url.isNotBlank()) {
                downloadImageFromLink(url)
            } else {
                Toast.makeText(this, "Введите URL изображения", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSavedImage.setOnClickListener {
            val intent = Intent(this, SavedImagesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRandomPhoto() {
        val call = unsplashService.getRandomPhoto(ACCESS_KEY)

        call.enqueue(object : retrofit2.Callback<UnsplashPhoto> {
            override fun onResponse(call: retrofit2.Call<UnsplashPhoto>, response: retrofit2.Response<UnsplashPhoto>) {
                if (response.isSuccessful) {
                    val photo = response.body()
                    val imageUrl = photo?.urls?.regular

                    Glide.with(this@MainActivity)
                        .load(imageUrl)
                        .into(imageView)

                    // Сохранение данных о фото в бд
                    photo?.let {
                        val photoEntity = PhotoEntity(
                            author = it.user.name,
                            width = it.width,
                            height = it.height,
                            date = it.created_at
                        )
                        lifecycleScope.launch {
                            db.photoDao().insertPhoto(photoEntity)
                            Toast.makeText(this@MainActivity, "Данные о фото сохранены", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<UnsplashPhoto>, t: Throwable) {
            }
        })
    }

    private fun downloadImageFromLink(url: String) {
        lifecycleScope.launch {
            try {
                // Запуск потокаNetwork
                Log.i("MainActivity", "buttonLinkDownload: Запуск сетевого потока")
                Toast.makeText(this@MainActivity, "Запуск сетевого потока", Toast.LENGTH_SHORT).show()

                val bitmap = withContext(Dispatchers.IO) {
                    downloadBitmap(url)
                }

                Log.i("MainActivity", "buttonLinkDownload: Сетевой поток завершен")
                Toast.makeText(this@MainActivity, "Сетевой поток завершен", Toast.LENGTH_SHORT).show()

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)

                    // Запуск потока Disk
                    Log.i("MainActivity", "buttonLinkDownload: Запуск потока диск")
                    Toast.makeText(this@MainActivity, "Запуск потока диск", Toast.LENGTH_SHORT).show()

                    val filename = "image_${System.currentTimeMillis()}.png"
                    saveBitmapToInternalStorage(bitmap, filename)

                    Log.i("MainActivity", "buttonLinkDownload: Поток диск завершен")
                    Toast.makeText(this@MainActivity, "Поток диск завершен", Toast.LENGTH_SHORT).show()
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
        Log.i("MainActivity", "buttonLinkDownload:сохранение в память")
        try {
            openFileOutput(filename, MODE_PRIVATE).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}







//package ru.mirea.prac5
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class MainActivity : AppCompatActivity() {
//
//    // Внедрение UnsplashApiService и AppDatabase через Hilt
//    @Inject
//    lateinit var unsplashService: UnsplashApiService
//
//    @Inject
//    lateinit var db: AppDatabase
//
//    private lateinit var imageView: ImageView
//    private lateinit var buttonLoad: Button
//    private lateinit var buttonViewSaved: Button
//
//    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.layout_activity)
//
//        // Инициализация элементов интерфейса
//        imageView = findViewById(R.id.imageView)
//        buttonLoad = findViewById(R.id.buttonLoad)
//        buttonViewSaved = findViewById(R.id.buttonViewSaved)
//
//        // Установите обработчик нажатия кнопки для загрузки фото
//        buttonLoad.setOnClickListener {
//            loadRandomPhoto()
//        }
//
//        // Обработчик нажатия кнопки для перехода на экран сохраненных фото
//        buttonViewSaved.setOnClickListener {
//            val intent = Intent(this, SavedPhotosActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    private fun loadRandomPhoto() {
//        // Выполнение запроса с использованием внедренного UnsplashApiService
//        val call = unsplashService.getRandomPhoto(ACCESS_KEY)
//
//        // Выполнение запроса
//        call.enqueue(object : retrofit2.Callback<UnsplashPhoto> {
//            override fun onResponse(call: retrofit2.Call<UnsplashPhoto>, response: retrofit2.Response<UnsplashPhoto>) {
//                if (response.isSuccessful) {
//                    val photo = response.body()
//                    val imageUrl = photo?.urls?.regular
//
//                    // Загрузка изображения с помощью Glide
//                    Glide.with(this@MainActivity)
//                        .load(imageUrl)
//                        .into(imageView)
//                } else {
//                    Toast.makeText(this@MainActivity, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: retrofit2.Call<UnsplashPhoto>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}

