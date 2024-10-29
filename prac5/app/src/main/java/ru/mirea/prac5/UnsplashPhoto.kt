package ru.mirea.prac5

data class UnsplashPhoto(
    val urls: Urls,
    val user: User,
    val width: Int,
    val height: Int,
    val created_at: String
)

data class Urls(
    val regular: String
)

data class User(
    val name: String
)
