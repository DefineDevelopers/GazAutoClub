package net.webdefine.gazautoclub.fragments

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.beust.klaxon.JsonObject
import com.beust.klaxon.array
import com.beust.klaxon.int
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.adapter.PostsListAdapter
import net.webdefine.gazautoclub.database.Client
import net.webdefine.gazautoclub.model.Post
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*


class UsersPostsFeedFragment: FeedFragment() {
    companion object {
        private const val ACCESS_TOKEN  = "access_token"
        private const val TAG           = "UserPostsFeedFragment"

        fun newInstance(accessToken: String): UsersPostsFeedFragment {
            val args = Bundle()
            args.putSerializable(ACCESS_TOKEN, accessToken)
            val fragment = UsersPostsFeedFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_users_posts_feed, container,
                false)
        val activity = activity
        val recyclerView = view.findViewById<RecyclerView>(R.id.users_posts_recycler_view)
        val progress = view.findViewById<ProgressBar>(R.id.user_posts_fragment_progress)
        val placeholder = view.findViewById<ConstraintLayout>(R.id.user_posts_fragment_placeholder)
        val noNotesText = view.findViewById<TextView>(R.id.user_posts_fragment_no_posts)
        val noNotesButton = view.findViewById<Button>(R.id.user_posts_fragment_create)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val accessToken  = arguments!!.getSerializable(ACCESS_TOKEN)
        val posts: MutableList<Post> = ArrayList()
        launch(Android) {
            val result = Client.getTopics(Client.user.id)
            val response = result.await()
            Log.d(TAG, response.toJsonString(true))
            val count = response.int("count") as Int
            Log.d(TAG, count.toString())
            if (count == 0) {
                progress.visibility = View.GONE
                noNotesText.visibility = View.VISIBLE
                //noNotesButton.visibility = View.VISIBLE
            }
            else {
                progress.max = count

                for (i in IntRange(0, count - 1)) {
                    val id = response.array<JsonObject>("results")?.get(i)?.get("id") as Int
                    val comments = response.array<JsonObject>("results")?.get(i)?.get("comments_counter") as Int
                    val likes = response.array<JsonObject>("results")?.get(i)?.get("likes_counter") as Int
                    // TODO: parse description text from fullText
                    val fullText = response.array<JsonObject>("results")?.get(i)?.get("body") as String
                    val description = if (fullText.length < 100) fullText else fullText.substring(0, 98) + "..."
                    val title = response.array<JsonObject>("results")?.get(i)?.get("title") as String
                    val photo = response.array<JsonObject>("results")?.get(i)?.get("photo") as String?
                    val time = formatToYesterdayOrToday(
                            (response.array<JsonObject>("results")?.get(i)?.get
                            ("creation_time") as String)
                    )
                    val car = response.array<JsonObject>("results")?.get(i)?.get("car_model") as Int
                    val carNameResult = Client.getCarModelInfo(car)
                    val carName = carNameResult.await()
                    val author = response.array<JsonObject>("results")?.get(i)?.get("author") as Int
                    val userLiked = response.array<JsonObject>("results")?.get(i)?.get("is_liked") as Boolean
                    val authorName = Client.user.username
                    Log.d("MainFeed", authorName)
                    posts.add(Post(id, author, authorName,  title, description, fullText,
                            photo?: "", time, car, carName, comments, likes, userLiked))
                    progress.progress = i + 1
                }
                recyclerView.adapter = PostsListAdapter(activity!!, ArrayList(posts))
                placeholder.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        if (accessToken == "") {
            val param = recyclerView.layoutParams as CoordinatorLayout.LayoutParams
            param.bottomMargin = 0
            recyclerView.layoutParams = param
        }
        return view
    }

    private fun formatToYesterdayOrToday(date: String): String {
        val dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").parseDateTime(date)
        val today = DateTime()
        val yesterday = today.minusDays(1)
        val timeFormatter = DateTimeFormat.forPattern("HH:mm")

        return when {
            dateTime.toLocalDate() == today.toLocalDate() -> "Today " + timeFormatter.print(dateTime)
            dateTime.toLocalDate() == yesterday.toLocalDate() -> "Yesterday " + timeFormatter.print(dateTime)
            else -> dateTime.toString("dd MMMM yyyy HH:mm", Locale("ru"))
        }
    }
}