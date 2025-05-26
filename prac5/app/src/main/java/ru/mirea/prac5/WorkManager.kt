package ru.mirea.prac5

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val url = inputData.getString("PHOTO_URL") ?: return Result.failure()
        try {
            val bitmap = downloadBitmap(url) ?: return Result.failure()

            val filename = "image_worker_${System.currentTimeMillis()}.png"
            applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Log.d("DownloadWorker", "Фото сохранено: $filename")
            return Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Ошибка: ${e.message}")
            return Result.failure()
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
}