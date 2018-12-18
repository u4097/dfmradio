package ru.rmg.dfm.util

import com.google.gson.annotations.SerializedName


class Shoutcast {
    private var name: String? = null

    @SerializedName("stream")
    private var url: String? = null

    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String) {
        this.url = url
    }
}