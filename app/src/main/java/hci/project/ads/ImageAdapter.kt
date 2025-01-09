package hci.project.ads

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val images: List<ImageTask>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    val selectedImages = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        Log.d("images", "$images")
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageTask: ImageTask) {
            val resId = itemView.context.resources.getIdentifier(imageTask.resName, "drawable", itemView.context.packageName)
            Log.d("ImageAdapter", "Resource ID for ${imageTask.resName}: $resId")
            Glide.with(itemView.context) .load(resId) .into(imageView)
            itemView.setOnClickListener {
                if (selectedImages.contains(imageTask.resName)) {
                    selectedImages.remove(imageTask.resName)
                    itemView.alpha = 1.0f // De-selektira sliku
                } else {
                    selectedImages.add(imageTask.resName)
                    itemView.alpha = 0.5f // Selektira sliku
                }
                onImageClick(imageTask.resName)
            }
        }
    }
}
