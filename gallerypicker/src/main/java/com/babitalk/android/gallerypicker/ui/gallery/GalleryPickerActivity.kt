package com.babitalk.android.gallerypicker.ui.gallery

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.babitalk.android.gallerypicker.GalleryPickerApplication
import com.babitalk.android.gallerypicker.R
import com.babitalk.android.gallerypicker.common.BaseActivity
import com.babitalk.android.gallerypicker.ui.common.collapse
import com.babitalk.android.gallerypicker.ui.common.dpToPx
import com.babitalk.android.gallerypicker.ui.common.fromHtml
import com.babitalk.android.gallerypicker.databinding.ActivityGalleryPickerBinding
import com.babitalk.android.gallerypicker.model.Image
import com.babitalk.android.gallerypicker.ui.common.changeBoundsAnimation
import com.babitalk.android.gallerypicker.ui.gallery.viewmodel.GalleryViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList

const val EXTRA_SELECT_IMAGE = "extra_select_image"
private val REQUEST_PERMISSION_CODE = GalleryPickerActivity::class.java.hashCode() and 0x0000000ff

class GalleryPickerActivity : BaseActivity<ActivityGalleryPickerBinding>() {
    data class ParamsBuilder(val spanCount: Int = 3, val maxCount: Int = Integer.MAX_VALUE, val selectedImages: ArrayList<Uri> = arrayListOf())
    companion object {
        fun newInstance(context: Context, requestCode: Int, params: ParamsBuilder) {
            val intent = Intent(context, GalleryPickerActivity::class.java).apply {
                putExtra("span_count", params.spanCount)
                putExtra("max_count", params.maxCount)
                putParcelableArrayListExtra("selected_images", params.selectedImages)
            }
            when (context) {
                is Activity -> context.startActivityForResult(intent, requestCode)
                is Fragment -> context.startActivityForResult(intent, requestCode)
                else -> return
            }
        }
    }

    private val spanCount by lazy { (intent?.getIntExtra("span_count", 3) ?: 3).also { viewModel.spanCount = it } }
    private val maxCount by lazy { intent?.getIntExtra("max_count", Integer.MAX_VALUE) ?: Integer.MAX_VALUE }
    private val viewModel by lazy { ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[GalleryViewModel::class.java] }
    private val selectedImageAdapter by lazy { SelectedImageRecyclerAdapter(viewModel) }
    private val imageAdapter by lazy { ImageRecyclerAdapter(viewModel) }
    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            if (!isPermissionGranted()) return
            val selectedImages = viewModel.selectedImages.value?.filter { it.imagePath != null }?.map { it.imagePath!! } ?: return
            viewModel.fetchGalleryImages(selectedImages)
        }
    }

    override fun createViewBinding(): ActivityGalleryPickerBinding = ActivityGalleryPickerBinding.inflate(layoutInflater)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun isPermissionGranted(): Boolean = if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        // You can use the API that requires the permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        false
    } else {
        true
    }

    override fun initViews() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        with(binding.selectedImageRecyclerView) {
            adapter = selectedImageAdapter
            layoutManager = LinearLayoutManager(this@GalleryPickerActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        with(binding.recyclerView) {
            adapter = imageAdapter
            layoutManager = GridLayoutManager(this@GalleryPickerActivity, spanCount)
            addItemDecoration(SpacesItemDecoration(1.dpToPx()))
        }

        binding.bucketName.setOnClickListener {
            PopupMenu(this, it).apply {
                viewModel.bucketNames.forEach { bucketName -> menu.add(bucketName) }
                setOnMenuItemClickListener { item ->
                    val bucketName = item.title.toString()
                    binding.bucketName.text = bucketName
                    binding.bucketName.tag = bucketName
                    viewModel.filterWith(bucketName = bucketName)
                    return@setOnMenuItemClickListener false
                }
            }.show()
        }

        binding.sendButton.setOnClickListener {
            val images = viewModel.selectedImages.value
                ?.filter { it.imagePath != null }
                ?.map { it.imagePath!! }
                ?.toCollection(ArrayList()) ?: return@setOnClickListener

            val intent = Intent().apply { putParcelableArrayListExtra(EXTRA_SELECT_IMAGE, images) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun subscribeUi() {
        viewModel.dataList.observe(this, Observer { images ->
            imageAdapter.submitList(images.toList()) { binding.recyclerView.scrollToPosition(0) }
            val title = "${binding.bucketName.tag}&nbsp;&nbsp;<b><font color='#adaebc'><small>${String.format(Locale.getDefault(), getString(R.string.select_count), images.count())}</small></font></b>".fromHtml()
            binding.bucketName.text = title
        })

        viewModel.selectedImages.observe(this, Observer { images ->
            val count = images.count()
            binding.sendButton.isEnabled = count > 0
            binding.sendButton.text = if (count == 0) {
                getString(R.string.confirm)
            } else {
                String.format(Locale.getDefault(), "<b><small><font color='#897DFF'>%d</font></small></b>&nbsp;&nbsp;%s", count, getString(
                    R.string.confirm
                )).fromHtml()
            }
            // show/hide selected image list
            selectedImageAdapter.submitList(images.toList()) {
                binding.selectedImageRecyclerView.changeBoundsAnimation()
                if (count == 0) {
                    binding.selectedImageRecyclerView.visibility = View.GONE
                } else {
                    binding.selectedImageRecyclerView.visibility = View.VISIBLE
                }
            }
        })

        viewModel.showMessage.observe(this) { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.moveToImageDetail.observe(this) { (image, count, transitionView) ->
            GalleryImageDetailActivity.newInstance(this, image, count, maxCount, transitionView)
        }

        GalleryPickerApplication.sContext.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
    }

    override fun loadData() {
        if (!isPermissionGranted()) return
        viewModel.maxCount = maxCount
        viewModel.fetchGalleryImages(intent?.getParcelableArrayListExtra("selected_images") ?: arrayListOf())
    }

    override fun onDestroy() {
        GalleryPickerApplication.sContext.contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getParcelableExtra<Image>("image") ?: return
                val image = viewModel.find(result) ?: return
                viewModel.selectImage(image)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            val permissionGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!permissionGranted) {
                Toast.makeText(this, "권한을 허용해야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                loadData()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.left = space
            outRect.right = space
            outRect.bottom = space
            outRect.top = if (parent.getChildLayoutPosition(view) == 0) space else 0
        }
    }
}