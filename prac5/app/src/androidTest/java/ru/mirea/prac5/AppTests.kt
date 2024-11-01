// File: src/test/java/ru/mirea/prac5/AppTests.kt
package ru.mirea.prac5

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File

class AppTests {

    // Правило для Room
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var photoDao: PhotoDao

    @Before
    fun setup() {
        // Создаем in-memory базу данных
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        photoDao = database.photoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // Тест 1: Проверка сохранения данных в базу данных
    @Test
    fun insertPhoto_andRetrieve() = runBlocking {
        val photo = PhotoEntity(
            author = "Test Author",
            width = 1920,
            height = 1080,
            date = "2024-04-01"
        )
        photoDao.insertPhoto(photo)

        val photos = photoDao.getAllPhotos().first()
        assertNotNull(photos)
        assertEquals(1, photos.size)
        assertEquals("Test Author", photos[0].author)
    }

    // Тест 2: Проверка сохранения изображения во внутреннее хранилище
    @Test
    fun saveImageToInternalStorage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val filename = "test_image.png"

        // Сохранение изображения
        context.openFileOutput(filename, android.content.Context.MODE_PRIVATE).use { fos ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        }

        // Проверка, что файл существует
        val file = File(context.filesDir, filename)
        assertTrue(file.exists())

        // Очистка
        file.delete()
    }
}
