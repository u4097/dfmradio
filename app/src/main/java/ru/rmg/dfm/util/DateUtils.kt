package ru.rmg.dfm.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 *  @author Arthur Korchagin on 13.06.17.
 */

private val MIN_TIME_STRING = "0:00"

/****************************************************************   Mon, 12 Jun 2017 22:09:22 GMT */
private val DISPLAY_TIME_FORMAT = AtomicReference(SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH))

/***************************************************************2017-06-13T01:41:31+03:00 */
private val PLAYLIST_FORMAT = AtomicReference(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH))

private val DURATION_FORMAT = AtomicReference(SimpleDateFormat("m:ss", Locale.ENGLISH))

private val TIME_FORMAT = AtomicReference(SimpleDateFormat("HH:mm", Locale.ENGLISH))

fun String.parseServerDate(): Date? = DISPLAY_TIME_FORMAT.get().parse(this)
fun String.parsePlaylistDate(): Date? = PLAYLIST_FORMAT.get().parse(this)
fun Date.formatServerDate(): String? = DISPLAY_TIME_FORMAT.get().format(this)
fun Date.formatTime(): String? = TIME_FORMAT.get().format(this)

fun Long.formatDuration(): String = DURATION_FORMAT.get().format(Date(this)) ?: MIN_TIME_STRING