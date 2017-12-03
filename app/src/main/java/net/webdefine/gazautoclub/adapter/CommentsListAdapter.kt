package net.webdefine.gazautoclub.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.webdefine.gazautoclub.databinding.CommentLayoutBinding
import net.webdefine.gazautoclub.model.Comment

class CommentsListAdapter(context: Context, private val comments: ArrayList<Comment>)
    : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    private          val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CommentsListAdapter.ViewHolder {
        val commentLayoutBinding = CommentLayoutBinding.inflate(layoutInflater, viewGroup, false)
        return ViewHolder(commentLayoutBinding.root, commentLayoutBinding)
    }

    override fun onBindViewHolder(pViewHolder: CommentsListAdapter.ViewHolder, position: Int) {
        val comment = comments[position]
        pViewHolder.setData(comment)
    }

    override fun getItemCount(): Int = comments.size

    class ViewHolder(itemView: View, private val recyclerPostItemBinding: CommentLayoutBinding) :
            RecyclerView.ViewHolder(itemView) {

        fun setData(comment: Comment) {
            recyclerPostItemBinding.comment = comment
        }
    }
}