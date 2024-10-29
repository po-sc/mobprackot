package ru.mirea.prac5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(private val photos: List<PhotoEntity>) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        val sizeTextView: TextView = itemView.findViewById(R.id.sizeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        holder.authorTextView.text = photo.author
        holder.sizeTextView.text = "Размер: ${photo.width}x${photo.height}"
        holder.dateTextView.text = "Дата: ${photo.date}"
    }

    override fun getItemCount() = photos.size
}
