package net.webdefine.gazautoclub.recycler

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import net.webdefine.gazautoclub.data.db.Post

class PostsListAdapter(private val items: List<Post>) : RecyclerView.Adapter<PostsListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.textView.text = items[position].context.subSequence(0, 100)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(TextView(parent!!.context))
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}