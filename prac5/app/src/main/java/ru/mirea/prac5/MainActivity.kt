package ru.mirea.prac5

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Внедрение UnsplashApiService и AppDatabase через Hilt
    @Inject
    lateinit var unsplashService: UnsplashApiService

    @Inject
    lateinit var db: AppDatabase

    private lateinit var imageView: ImageView
    private lateinit var buttonLoad: Button
    private lateinit var buttonViewSaved: Button

    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity)

        // Инициализация элементов интерфейса
        imageView = findViewById(R.id.imageView)
        buttonLoad = findViewById(R.id.buttonLoad)
        buttonViewSaved = findViewById(R.id.buttonViewSaved)

        // Установите обработчик нажатия кнопки для загрузки фото
        buttonLoad.setOnClickListener {
            loadRandomPhoto()
        }

        // Обработчик нажатия кнопки для перехода на экран сохраненных фото
        buttonViewSaved.setOnClickListener {
            val intent = Intent(this, SavedPhotosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRandomPhoto() {
        // Выполнение запроса с использованием внедренного UnsplashApiService
        val call = unsplashService.getRandomPhoto(ACCESS_KEY)

        // Выполнение запроса
        call.enqueue(object : retrofit2.Callback<UnsplashPhoto> {
            override fun onResponse(call: retrofit2.Call<UnsplashPhoto>, response: retrofit2.Response<UnsplashPhoto>) {
                if (response.isSuccessful) {
                    val photo = response.body()
                    val imageUrl = photo?.urls?.regular

                    // Загрузка изображения с помощью Glide
                    Glide.with(this@MainActivity)
                        .load(imageUrl)
                        .into(imageView)
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<UnsplashPhoto>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
