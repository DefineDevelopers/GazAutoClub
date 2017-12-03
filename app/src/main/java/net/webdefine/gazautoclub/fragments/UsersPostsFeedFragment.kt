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

class UsersPostsFeedFragment: FeedFragment() {
    companion object {
        private const val USER_ID = "user_id"

        fun newInstance(id: Int): UsersPostsFeedFragment {
            val args = Bundle()
            args.putSerializable(USER_ID, id)
            val fragment = UsersPostsFeedFragment()
            fragment.arguments = args
            return fragment
        }
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
            if (authorIds[i] == this.arguments.getSerializable(USER_ID))
                posts.add(Post(i, authorIds[i], names[i], descriptions[i], descriptions[i], imageResIds[i]))
        }

        typedArray.recycle()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_users_posts_feed, container,
                false)
        val activity = activity
        val recyclerView = view.findViewById<RecyclerView>(R.id.users_posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = PostsListAdapter(activity, posts)

        return view
    }
}