package beatrate.pro.equipmentapp.data      // ← ваш пакет

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://68337a26464b499636ff960a.mockapi.io/"

    /* ----- интерцептеры ----- */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val headers = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "EquipmentApp/1.0")
                .build()
        )
    }

    /* ----- Moshi с поддержкой Kotlin ----- */
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())          // ← ключевая строка
        .build()

    /* ----- OkHttpClient ----- */
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(headers)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /* ----- Retrofit ----- */
    val api: EquipmentApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EquipmentApi::class.java)
    }
}