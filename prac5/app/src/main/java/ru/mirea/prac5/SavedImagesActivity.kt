package ru.mirea.prac5

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SavedImagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedImagesAdapter
    private lateinit var imageFiles: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_images)

        recyclerView = findViewById(R.id.recyclerViewSavedImages)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Получение списка файлов из внутренней памяти
        imageFiles = filesDir.listFiles { file ->
            file.name.endsWith(".png") || file.name.endsWith(".jpg")
        }?.toList() ?: emptyList()

        adapter = SavedImagesAdapter(imageFiles)
        recyclerView.adapter = adapter
    }
}
