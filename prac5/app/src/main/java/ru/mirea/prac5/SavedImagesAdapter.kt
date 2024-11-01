package ru.mirea.prac5

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SavedImagesAdapter(private val images: List<File>) : RecyclerView.Adapter<SavedImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewSaved: ImageView = itemView.findViewById(R.id.imageViewSaved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.saved_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageFile = images[position]
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        holder.imageViewSaved.setImageBitmap(bitmap)
    }

    override fun getItemCount() = images.size
}
