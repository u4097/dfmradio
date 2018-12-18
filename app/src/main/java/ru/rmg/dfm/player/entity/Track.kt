package ru.rmg.dfm.player.entity

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
data class Track(val artist: String, val title: String, val isDefault : Boolean, val isProgram : Boolean = false)