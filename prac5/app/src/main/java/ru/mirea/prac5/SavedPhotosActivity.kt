package ru.mirea.prac5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SavedPhotosActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_photos)

        recyclerView = findViewById(R.id.recyclerView)
        db = AppDatabase.getDatabase(this)

        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            db.photoDao().getAllPhotos().collect { photos ->
                adapter = PhotoAdapter(photos)
                recyclerView.adapter = adapter
            }
        }
    }
}
