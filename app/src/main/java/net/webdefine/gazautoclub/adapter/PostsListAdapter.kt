package net.webdefine.gazautoclub.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.webdefine.gazautoclub.databinding.PostLayoutBinding
import net.webdefine.gazautoclub.fragments.FeedFragment
import net.webdefine.gazautoclub.model.Post

class PostsListAdapter(context: Context, private val posts: ArrayList<Post>)
    : RecyclerView.Adapter<PostsListAdapter.ViewHolder>() {

    private lateinit var mListener: FeedFragment.OnPostSelected
    private          val mContext = context
    private          val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PostsListAdapter.ViewHolder {
        val postItemBinding = PostLayoutBinding.inflate(layoutInflater, viewGroup, false)
        return ViewHolder(postItemBinding.root, postItemBinding)
    }

    override fun onBindViewHolder(pViewHolder: PostsListAdapter.ViewHolder, position: Int) {
        if (mContext is FeedFragment.OnPostSelected) {
            mListener = mContext
        } else {
            throw ClassCastException(mContext.toString() + " must implement OnPostSelected.")
        }

        val post = posts[position]
        pViewHolder.setData(post)
        pViewHolder.itemView.setOnClickListener { mListener.onPostSelected(post) }
    }

    override fun getItemCount(): Int = posts.size

    class ViewHolder(itemView: View, private val recyclerPostItemBinding: PostLayoutBinding) :
            RecyclerView.ViewHolder(itemView) {

        fun setData(post: Post) {
            recyclerPostItemBinding.post = post
        }
    }
}