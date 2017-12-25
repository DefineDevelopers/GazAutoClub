package net.webdefine.gazautoclub.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_layout.view.*
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.database.Client
import net.webdefine.gazautoclub.databinding.PostLayoutBinding
import net.webdefine.gazautoclub.fragments.FeedFragment
import net.webdefine.gazautoclub.model.Post
import org.jetbrains.anko.sdk25.coroutines.onClick
import ru.noties.markwon.Markwon

class PostsListAdapter(context: Context, private var posts: ArrayList<Post>)
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

        Log.d("Posts", itemCount.toString())

        val post = posts[position]
        Log.d("PostListAdapter/Post", posts[position].toString())
        pViewHolder.setData(post)
        pViewHolder.itemView.onClick {
            mListener.onPostSelected(post)
        }

        pViewHolder.itemView.post_content.onClick {
            val requestPostFulltext = Client.getTopicFullText(post.id)
            post.fullText = requestPostFulltext.await()
            mListener.onPostSelected(post)
        }

        pViewHolder.itemView.like_block.onClick {
            launch(Android) {
                val likeResponse = Client.likeNote(post.id)
                val (userLiked, likes) = likeResponse.await()

                updateNoteAfterLike(likes, userLiked, pViewHolder.itemView)
            }
        }

        Markwon.setMarkdown(pViewHolder.itemView.post_content, post.description)

        pViewHolder.itemView.comment_block.onClick {
            mListener.onPostSelected(post)
        }

        if (!post.image.isEmpty()) {
            Picasso.with(App.instance)
                    .load(post.image)
                    .into(pViewHolder.itemView.post_image)
        }

        updateNoteAfterLike(post.likes, post.userLiked, pViewHolder.itemView)
    }

    private fun updateNoteAfterLike(likes: Int?, isLiked: Boolean, view: View) {
        view.likes_count.text = likes.toString()
        if (isLiked) {
            view.like_icon.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_red_24dp)
            )
        }
        else {
            view.like_icon.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_blue_24dp)
            )
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isLiked) {
                view.like_icon.setImageDrawable(
                        App.instance.getDrawable(
                                R.drawable.ic_favorite_red_24dp
                        )
                )
            }
            else {
                view.like_icon.setImageDrawable(
                        App.instance.getDrawable(
                                R.drawable.ic_favorite_blue_24dp
                        )
                )
            }
        } else {
            if (isLiked) {
                view.like_icon.setImageDrawable(
                        ResourcesCompat.getDrawable(App.instance.resources, R.drawable.ic_favorite_red_24dp, null)
                )
            }
            else {
                view.like_icon.setImageDrawable(
                        ResourcesCompat.getDrawable(App.instance.resources, R.drawable.ic_favorite_blue_24dp, null)
                )
            }
        }*/
    }

    override fun getItemCount(): Int = posts.size

    class ViewHolder(itemView: View, private val recyclerPostItemBinding: PostLayoutBinding) :
            RecyclerView.ViewHolder(itemView) {

        fun setData(post: Post) {
            recyclerPostItemBinding.post = post
            recyclerPostItemBinding.executePendingBindings()
        }
    }
}