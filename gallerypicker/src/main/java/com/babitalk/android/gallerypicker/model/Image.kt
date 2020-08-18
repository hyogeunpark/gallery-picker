package com.babitalk.android.gallerypicker.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Image(val bucketName: String, val imagePath: Uri?, var isSelected: Boolean = false) : Parcelable {
    var selectOrderNumber: Int = 0
        set(value) {
            field = value
            selectOrderNumberHandler?.invoke(this)
        }

    var selectOrderNumberHandler: ((Image) -> Unit)? = null

    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readParcelable<Uri>(Uri::class.java.classLoader),
        1 == source.readInt()
    ) {
        selectOrderNumber = source.readInt()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(bucketName)
        writeParcelable(imagePath, flags)
        writeInt((if (isSelected) 1 else 0))
        writeInt(selectOrderNumber)
    }

    override fun equals(other: Any?): Boolean {
        return other is Image && imagePath == other.imagePath
    }

    override fun hashCode(): Int {
        var result = bucketName.hashCode()
        result = 31 * result + (imagePath?.hashCode() ?: 0)
        result = 31 * result + isSelected.hashCode()
        result = 31 * result + selectOrderNumber
        result = 31 * result + (selectOrderNumberHandler?.hashCode() ?: 0)
        return result
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Image> = object : Parcelable.Creator<Image> {
            override fun createFromParcel(source: Parcel): Image = Image(source)
            override fun newArray(size: Int): Array<Image?> = arrayOfNulls(size)
        }
    }
}