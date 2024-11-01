package ru.mirea.prac5

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("photos/random")
    fun getRandomPhoto(@Query("client_id") clientId: String): Call<UnsplashPhoto>
}
