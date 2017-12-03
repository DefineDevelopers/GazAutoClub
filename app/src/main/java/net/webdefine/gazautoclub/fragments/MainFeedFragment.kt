package net.webdefine.gazautoclub.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.adapter.PostsListAdapter
import net.webdefine.gazautoclub.model.Post

class MainFeedFragment : FeedFragment() {
    companion object {
        fun newInstance(): MainFeedFragment = MainFeedFragment()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        val resources = context!!.resources
        positions = resources.getIntArray(R.array.positions)
        names = resources.getStringArray(R.array.names)
        descriptions = resources.getStringArray(R.array.descriptions)
        authorIds = resources.getIntArray(R.array.auth_ids)

        val typedArray = resources.obtainTypedArray(R.array.images)
        val imageCount = names.size
        imageResIds = IntArray(imageCount)
        for (i in 0 until imageCount) {
            imageResIds[i] = typedArray.getResourceId(i, 0)
            posts.add(Post(i, authorIds[i], names[i], descriptions[i], descriptions[i], imageResIds[i]))
        }

        typedArray.recycle()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_main_feed, container,
                false)
        val activity = activity
        val recyclerView = view.findViewById<RecyclerView>(R.id.main_feed_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = PostsListAdapter(activity, posts)

        return view
    }
}