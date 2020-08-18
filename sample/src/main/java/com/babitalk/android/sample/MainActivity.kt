package com.babitalk.android.sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.babitalk.android.gallerypicker.common.loadUri
import com.babitalk.android.gallerypicker.ui.gallery.EXTRA_SELECT_IMAGE
import com.babitalk.android.gallerypicker.ui.gallery.GalleryPickerActivity
import com.babitalk.android.sample.databinding.ActivityMainBinding
import com.babitalk.android.sample.databinding.RowResultImageViewBinding
import kotlin.math.max

const val REQUEST_SELECT_IMAGE = 99
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val adapter by lazy { ImageViewListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imageViewPager.adapter = adapter
        binding.imageViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.addImagesButton.setOnClickListener {
            val selectedImages = adapter.currentList.toCollection(ArrayList())
            val params = GalleryPickerActivity.ParamsBuilder(spanCount = binding.spanCount.text.toString().toInt(), maxCount = 5, selectedImages = selectedImages)
            GalleryPickerActivity.newInstance(this, REQUEST_SELECT_IMAGE, params)
        }

        binding.addButton.setOnClickListener {
            val spanCount = binding.spanCount.text.toString().toInt()
            binding.spanCount.text = (spanCount + 1).toString()
        }

        binding.subtractButton.setOnClickListener {
            val spanCount = binding.spanCount.text.toString().toInt()
            binding.spanCount.text = max(spanCount - 1, 1).toString()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val images = data?.getParcelableArrayListExtra<Uri>(EXTRA_SELECT_IMAGE) ?: return
                adapter.submitList(images.toList())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    class ImageViewListAdapter: ListAdapter<Uri, ImageViewListAdapter.ViewHolder>(object: DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem

    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(RowResultImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            getItem(position)?.let(holder::bind)
        }

        class ViewHolder(private val binding: RowResultImageViewBinding): RecyclerView.ViewHolder(binding.root) {

            fun bind(uri: Uri) {
                binding.imageView.loadUri(uri)
            }
        }
    }
}