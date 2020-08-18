package com.babitalk.android.gallerypicker.ui.gallery.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babitalk.android.gallerypicker.GalleryPickerApplication
import com.babitalk.android.gallerypicker.R
import com.babitalk.android.gallerypicker.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max


class GalleryViewModel : ViewModel() {
    init { ContentResolver.setMasterSyncAutomatically(true) }

    private val originImages = arrayListOf<Image>()
    var spanCount: Int = 3
    var maxCount = Integer.MAX_VALUE
    val dataList = MutableLiveData<List<Image>>()
    val bucketNames = arrayListOf(GalleryPickerApplication.sContext.getString(R.string.bucket_all))
    val selectedImages = MutableLiveData<ArrayList<Image>>(arrayListOf())

    private var isLoading = false
    fun fetchGalleryImages(selectedImages: List<Uri>) {
        if (isLoading) return
        isLoading = true
        this@GalleryViewModel.selectedImages.value = arrayListOf()
        initBuckNames()
        viewModelScope.launch(Dispatchers.IO) {
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val cursor = GalleryPickerApplication.sContext.contentResolver.query(uri, projection, null, null, sortOrder) ?: return@launch
            val images = arrayListOf<Image>()
            try {
                val bucketDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val bucketName = cursor.getString(bucketDisplayName)
                    val imageId = cursor.getLong(columnIndexID)
                    val uriImage = Uri.withAppendedPath(uri, imageId.toString())
                    val selectOrderNumber = selectedImages.indexOf(uriImage)
                    val image = Image(bucketName, uriImage, selectOrderNumber != -1).apply {
                        this.selectOrderNumber = max(selectOrderNumber + 1, 0)
                    }
                    images.add(image)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor.close()
            }
            this@GalleryViewModel.originImages.clear()
            this@GalleryViewModel.originImages.addAll(images)
            this@GalleryViewModel.selectedImages.postValue(originImages.filter { it.isSelected }.toCollection(ArrayList()))
            dataList.postValue(images)
            bucketNames.addAll(images.map { it.bucketName }.distinct())
            isLoading = false
        }
    }

    private fun initBuckNames() {
        bucketNames.clear()
        bucketNames.add(GalleryPickerApplication.sContext.getString(R.string.bucket_all))
    }

    fun find(image: Image) = originImages.find { it.imagePath == image.imagePath }?.apply {
        selectOrderNumber = image.selectOrderNumber
        isSelected = image.isSelected
    }

    fun filterWith(bucketName: String?) {
        val list = if (bucketName.isNullOrEmpty() || bucketName == GalleryPickerApplication.sContext.getString(R.string.bucket_all)) {
            originImages
        } else {
            originImages.filter { image -> image.bucketName == bucketName }
        }
        dataList.postValue(list)
    }

    fun removeImage(position: Int) {
        val removeImage = selectedImages.value?.get(position) ?: return
        removeImage.isSelected = false
        notifyAboutSelectedImage(removeImage)
    }

    fun selectImage(image: Image) {
        notifyAboutSelectedImage(image)
    }

    val showMessage = MutableLiveData<String>()
    private fun notifyAboutSelectedImage(target: Image) {
        val selectedList = selectedImages.value ?: return
        originImages.find { it == target }?.isSelected = target.isSelected
        if (target.isSelected) {
            if (selectedList.count() + 1 > maxCount) {
                target.isSelected = !target.isSelected
                showMessage.postValue(String.format("최대 %d까지 선택 가능합니다.", maxCount))
                return
            }
            if (selectedList.none { it.imagePath == target.imagePath }) {
                selectedList.add(target)
            }
        } else {
            selectedList.remove(target)
            target.selectOrderNumber = 0 // 초기화
        }
        selectedList.forEachIndexed { index, _image -> _image.selectOrderNumber = index + 1 }
        selectedImages.postValue(selectedList)
    }

    val moveToImageDetail = MutableLiveData<Triple<Image, Int, ImageView>>()
    fun moveToImageDetail(image: Image, selectImageView: ImageView) {
        moveToImageDetail.value = Triple(image, selectedImages.value?.count() ?: 0, selectImageView)
    }
}