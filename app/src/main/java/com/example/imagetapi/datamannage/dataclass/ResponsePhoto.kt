package com.example.imagetapi.datamannage.dataclass

import com.google.gson.annotations.SerializedName

data class ResponsePhoto(@SerializedName("id") var id: String, @SerializedName("urls") var url: ResponseURL,
                         @SerializedName("links") var link: ResponseLink, var isDownload: Boolean) {


}