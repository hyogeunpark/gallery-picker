package com.babitalk.android.gallerypicker.ui.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import com.babitalk.android.gallerypicker.GalleryPickerApplication
import com.babitalk.android.gallerypicker.R
import com.babitalk.android.gallerypicker.common.BaseActivity
import com.babitalk.android.gallerypicker.databinding.ActivityGalleryImageDetailBinding
import com.babitalk.android.gallerypicker.model.Image
import com.google.android.material.snackbar.Snackbar

val REQUEST_IMAGE_DETAIL = GalleryImageDetailActivity::class.hashCode() and 0x000000ff

class GalleryImageDetailActivity : BaseActivity<ActivityGalleryImageDetailBinding>() {
    companion object {
        fun newInstance(activity: Activity, image: Image, selectedImageCount: Int, maxCount: Int, imageView: View?) {
            val optionsCompat = if (imageView != null) {
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageView, GalleryPickerApplication.sContext.getString(R.string.app_name))
            } else {
                null
            }
            val intent = Intent(activity, GalleryImageDetailActivity::class.java).apply {
                putExtra("image", image)
                putExtra("selected_image_count", selectedImageCount)
                putExtra("max_count", maxCount)
            }
            activity.startActivityForResult(intent, REQUEST_IMAGE_DETAIL, optionsCompat?.toBundle())
        }
    }

    private val image by lazy { intent?.getParcelableExtra<Image>("image") }
    private val selectedImageCount by lazy { intent?.getIntExtra("selected_image_count", 0) ?: 0 }
    private val maxCount by lazy { intent?.getIntExtra("max_count", Integer.MAX_VALUE) ?: Integer.MAX_VALUE }

    override fun createViewBinding(): ActivityGalleryImageDetailBinding = ActivityGalleryImageDetailBinding.inflate(layoutInflater)

    override fun initViews() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        changeOrderNumber(image)
        binding.selectCount.setOnClickListener {
            val changeImageModel = image ?: return@setOnClickListener
            val selectOrderNumber = if (changeImageModel.selectOrderNumber == 0) selectedImageCount + 1 else changeImageModel.selectOrderNumber
            if (selectOrderNumber > maxCount) {
                Snackbar.make(binding.root, String.format("최대 %d까지 선택 가능합니다.", maxCount), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            changeImageModel.selectOrderNumber = selectOrderNumber
            changeImageModel.isSelected = !changeImageModel.isSelected
            changeOrderNumber(changeImageModel)
            setResult(Activity.RESULT_OK, Intent().apply { putExtra("image", changeImageModel) })
        }
    }

    private fun changeOrderNumber(image: Image?) {
        val isSelected = image?.isSelected == true
        binding.selectCount.isSelected = isSelected
        binding.selectCount.text = if (isSelected) image?.selectOrderNumber?.toString() else ""
    }

    override fun subscribeUi() { }

    override fun loadData() {
        val imagePath = image?.imagePath?.toString() ?: return
        val imageUri = Uri.parse(imagePath)
        binding.imageView.setImageURI(imageUri)
    }
}