package net.webdefine.gazautoclub.fragments

import android.support.v4.app.Fragment
import net.webdefine.gazautoclub.model.Post

open class FeedFragment : Fragment() {
    lateinit var positions: IntArray
    lateinit var authorIds: IntArray
    lateinit var imageResIds: IntArray
    lateinit var names: Array<String>
    lateinit var descriptions: Array<String>
             var posts: ArrayList<Post> = ArrayList()

    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }

    interface OnPostSelected {
        fun onPostSelected(pPost: Post)
    }
}