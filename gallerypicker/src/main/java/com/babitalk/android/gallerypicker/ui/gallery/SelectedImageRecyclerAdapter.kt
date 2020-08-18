package com.babitalk.android.gallerypicker.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.babitalk.android.gallerypicker.common.loadUri
import com.babitalk.android.gallerypicker.databinding.RowSelectedImageViewBinding
import com.babitalk.android.gallerypicker.model.Image
import com.babitalk.android.gallerypicker.ui.gallery.viewmodel.GalleryViewModel

class SelectedImageRecyclerAdapter(private val viewModel: GalleryViewModel): ListAdapter<Image, SelectedImageRecyclerAdapter.ImageViewHolder>(object : DiffUtil.ItemCallback<Image>() {
    override fun areItemsTheSame(oldItem: Image, newItem: Image) = oldItem == newItem
    override fun areContentsTheSame(oldItem: Image, newItem: Image) = oldItem.imagePath == newItem.imagePath
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(RowSelectedImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)).apply {
            itemView.setOnClickListener { viewModel.removeImage(adapterPosition) }
        }
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    class ImageViewHolder(private val binding: RowSelectedImageViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            binding.selectedImageView.loadUri(image.imagePath)
        }
    }
}