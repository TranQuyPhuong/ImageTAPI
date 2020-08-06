package com.example.imagetapi.datamannage.dataclass

import android.os.Parcel
import android.os.Parcelable

data class ImageDataClass(val path: String?, val nameImage: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(nameImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageDataClass> {
        override fun createFromParcel(parcel: Parcel): ImageDataClass {
            return ImageDataClass(parcel)
        }

        override fun newArray(size: Int): Array<ImageDataClass?> {
            return arrayOfNulls(size)
        }
    }


}
