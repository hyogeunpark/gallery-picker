package com.babitalk.android.gallerypicker.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.babitalk.android.gallerypicker.common.dpToPx
import com.babitalk.android.gallerypicker.common.loadUri
import com.babitalk.android.gallerypicker.databinding.RowImageViewBinding
import com.babitalk.android.gallerypicker.model.Image
import com.babitalk.android.gallerypicker.ui.gallery.viewmodel.GalleryViewModel

class ImageRecyclerAdapter(private val viewModel: GalleryViewModel): ListAdapter<Image, ImageRecyclerAdapter.ImageViewHolder>(object : DiffUtil.ItemCallback<Image>() {
    override fun areItemsTheSame(oldItem: Image, newItem: Image) = oldItem == newItem
    override fun areContentsTheSame(oldItem: Image, newItem: Image) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(RowImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false), viewModel)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position) ?: return
        holder.bind(image)
    }

    override fun onViewRecycled(holder: ImageViewHolder) {
        super.onViewRecycled(holder)
        holder.recycled()
    }

    class ImageViewHolder(private val binding: RowImageViewBinding, private val viewModel: GalleryViewModel): RecyclerView.ViewHolder(binding.root) {
        init {
            val (areaSize, textSize) = getSelectCountAttribute(viewModel.spanCount)
            binding.selectCount.layoutParams.width = areaSize.dpToPx()
            binding.selectCount.layoutParams.height = areaSize.dpToPx()
            binding.selectCount.textSize = textSize.toFloat()
        }

        private var image: Image? = null

        fun bind(image: Image) {
            this.image = image
            image.imagePath?.let(binding.imageView::loadUri)
            binding.selectImageBg.isSelected = image.isSelected
            changeImageCount(isSelected = image.isSelected, orderNumber = image.selectOrderNumber)
            image.selectOrderNumberHandler = {
                binding.selectImageBg.isSelected = it.isSelected
                changeImageCount(isSelected = it.isSelected, orderNumber = it.selectOrderNumber)
            }
            binding.root.setOnClickListener {
                image.isSelected = !image.isSelected
                viewModel.selectImage(image)
            }
            binding.imageDetailButton.setOnClickListener { viewModel.moveToImageDetail(image, binding.imageView) }
        }

        private fun changeImageCount(isSelected: Boolean, orderNumber: Int) {
            binding.selectCount.isSelected = isSelected
            if (isSelected) {
                binding.selectCount.text = orderNumber.toString()
                binding.selectedImageCountBg.visibility = View.VISIBLE
            } else {
                binding.selectCount.text = ""
                binding.selectedImageCountBg.visibility = View.GONE
            }
        }

        fun recycled() {
            image?.selectOrderNumberHandler = null
        }

        private fun getSelectCountAttribute(spanCount: Int) = when(spanCount) {
            1 -> 36 to 20
            2 -> 28 to 18
            3 -> 24 to 16
            else -> 18 to 13
        }
    }
}