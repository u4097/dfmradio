package ru.rmg.dfm.util

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ru.rmg.dfm.MainActivity
import kotlinx.android.synthetic.main.list_item.*
import ru.rmg.dfm.R

class ShoutcastListAdapter(private val mainActivity: MainActivity, private val shoutcasts: List<Shoutcast>) :
    BaseAdapter() {

    override fun getCount(): Int {
        return shoutcasts.size
    }

    override fun getItem(position: Int): Any {
        return shoutcasts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view

        val inflater = mainActivity.layoutInflater

        val holder: ViewHolder

        if (view != null) {

            holder = view.tag as ViewHolder

        } else {

            view = inflater.inflate(R.layout.list_item, parent, false)

            holder = ViewHolder(view)

            view!!.tag = holder

        }

        val shoutcast = getItem(position) as Shoutcast ?: return view

        holder.text!!.text = shoutcast.getName()

        return view
    }


    internal class ViewHolder(view: View) {
        var text: TextView? = view.findViewById(R.id.text)
    }

}