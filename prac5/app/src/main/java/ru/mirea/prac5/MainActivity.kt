package ru.mirea.prac5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var buttonLoad: Button
    private lateinit var buttonViewSaved: Button
    private lateinit var db: AppDatabase

    private val ACCESS_KEY = "GFhSsMRZlkOWg5Y3nZGGcnSZ78JzDQy1zqkBsCOSe2M"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity)

        imageView = findViewById(R.id.imageView)
        buttonLoad = findViewById(R.id.buttonLoad)
        buttonViewSaved = findViewById(R.id.buttonViewSaved)
        db = AppDatabase.getDatabase(this)

        buttonLoad.setOnClickListener {
            loadRandomPhoto()
        }

        buttonViewSaved.setOnClickListener{
            val intent = Intent(this, SavedPhotosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRandomPhoto() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(UnsplashApiService::class.java)
        val call = service.getRandomPhoto(ACCESS_KEY)

        call.enqueue(object : Callback<UnsplashPhoto> {
            override fun onResponse(call: Call<UnsplashPhoto>, response: Response<UnsplashPhoto>) {
                if (response.isSuccessful) {
                    val photo = response.body()
                    val imageUrl = photo?.urls?.regular

                    if (imageUrl != null) {
                        Glide.with(this@MainActivity)
                            .load(imageUrl)
                            .into(imageView)

                        // Сохранение метаданных в базу данных
                        lifecycleScope.launch {
                            db.photoDao().insertPhoto(
                                PhotoEntity(
                                    author = photo.user.name,
                                    width = photo.width,
                                    height = photo.height,
                                    date = photo.created_at
                                )
                            )
                            Toast.makeText(this@MainActivity, "Фото сохранено", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UnsplashPhoto>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_view_saved_photos -> {
//                val intent = Intent(this, SavedPhotosActivity::class.java)
//                startActivity(intent)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }


}
