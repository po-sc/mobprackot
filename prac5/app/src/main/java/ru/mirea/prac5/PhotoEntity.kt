package ru.mirea.prac5

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val width: Int,
    val height: Int,
    val date: String
)
