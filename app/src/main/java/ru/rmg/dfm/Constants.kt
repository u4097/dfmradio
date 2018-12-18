package ru.rmg.dfm


object Constants {

    // Network
    val DOMAIN = "http://178.57.222.1:8000/"
    val API_VERSION = "0"
    val URL_REST_API = String.format("http://%s/v%s/", DOMAIN, API_VERSION)
    val URL_REST = "http://178.57.222.1:8000/"

    // Player buffer size in ms
    const val MIN_BUFFER_MS = 30_000
    const val MAX_BUFFER_MS = 85_000
    const val BUFFER_PLAYBACK_MS =  30_000

    // Player transport state actions
    const val ACTION_PLAY = "ru.rmg.dfm.player.ACTION_PLAY"
    const val ACTION_PAUSE = "ru.rmg.dfm.player.ACTION_PAUSE"
    const val ACTION_STOP = "ru.rmg.dfm.player.ACTION_STOP"
}