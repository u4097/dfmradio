package ru.rmg.dfm.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.rmg.dfm.R
import java.io.InputStreamReader

class ShoutcastHelper {

    companion object {
        fun retrieveShoutcasts(context: Context): List<Shoutcast> {

            val reader = InputStreamReader(context.resources.openRawResource(R.raw.shoutcasts))

            return Gson().fromJson(reader, object : TypeToken<List<Shoutcast>>() {

            }.type)
        }

    }

}