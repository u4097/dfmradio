package ru.rmg.dfm.util

/**
 *  @author Arthur Korchagin on 24.07.17.
 */
fun String.capWords() = indices
    .map { if (it == 0 || !this[it - 1].isLetter()) this[it].toUpperCase() else this[it].toLowerCase() }
    .joinToString("")
    .trim()