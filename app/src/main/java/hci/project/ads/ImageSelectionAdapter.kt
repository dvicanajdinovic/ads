package hci.project.ads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageSelectionAdapter(
    private val images: List<Int>,
    private val onImageSelected: (Int) -> Unit
) : RecyclerView.Adapter<ImageSelectionAdapter.ImageViewHolder>() {

    val selectedImages = mutableSetOf<Int>()

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivImage)

        fun bind(imageRes: Int) {
            imageView.setImageResource(imageRes)
            itemView.isSelected = selectedImages.contains(imageRes)

            itemView.setBackgroundResource(
                if (itemView.isSelected) R.color.purple_200 else android.R.color.white
            )
            itemView.alpha = if (itemView.isSelected) 0.5f else 1.0f
            itemView.setOnClickListener {
                if (selectedImages.contains(imageRes)) {
                    selectedImages.remove(imageRes)
                    itemView.isSelected = false
                    itemView.alpha = 1.0f // De-selektira sliku
                    itemView.setBackgroundResource(R.color.white)
                } else {
                    selectedImages.add(imageRes)
                    itemView.isSelected = true
                    itemView.alpha = 0.5f // De-selektira sliku
                    itemView.setBackgroundResource(R.color.purple_200)
                }
                notifyItemChanged(adapterPosition, Unit)
                onImageSelected(imageRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_selection, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

    //fun getSelectedImages(): Set<Int> = selectedImages
}
