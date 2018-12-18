package ru.rmg.dfm.player

class MediaNotificationManager(private val service: RadioService) {

    fun cancelNotify() {

        service.stopForeground(true)
    }
}