package net.webdefine.gazautoclub.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.mcxiaoke.koi.ext.onClick
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_details.*
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.adapter.CommentsListAdapter
import net.webdefine.gazautoclub.database.Client
import net.webdefine.gazautoclub.model.Comment
import net.webdefine.gazautoclub.model.Post
import ru.noties.markwon.Markwon

class PostDetailsActivity : AppCompatActivity() {
    private          var comments: MutableList<Comment>   = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private          var liked: Boolean = false

    companion object {
        val POST = "post"
        val ACCESS_TOKEN = "access_token"
        val TAG = "PostDetails"
        var accessToken = ""
        var postId = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        val post = intent.extras.getSerializable(POST) as Post
        accessToken = intent.extras.getString("access_token")

        setSupportActionBar(post_toolbar)
        supportActionBar!!.title = "Запись"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        liked = post.userLiked
        postId = post.id

        post_details_likes_count.text = post.likes.toString()
        post_title.text = post.name
        post_meta .text = post.authorName + " " +  post.time
        post_details_car_info.text = post.carName
        Markwon.setMarkdown(post_full_text, post.fullText)

        if (!post.image.isEmpty()) {
            Picasso.with(this)
                    .load(post.image)
                    .into(post_image)
        }

        recyclerView = findViewById(R.id.post_comments_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fillCommentsWithData(post)

        note_view_send_icon.onClick {
            val progressDialog = MaterialDialog.Builder(this@PostDetailsActivity)
                    .content(R.string.progress_dialog_auth)
                    .progress(true, 0)
                    .show()
            if (note_view_your_message_text.text.isNotEmpty()) {
                Fuel.post("https://gazautoclub.herokuapp.com/comments/",
                        listOf("topic" to post.id,
                                "body" to note_view_your_message_text.text))
                        .header("Authorization" to ("Bearer " + accessToken))
                        .response { request, response, result ->
                            Log.d("API/Auth", request.toString())
                            Log.d("API/Auth", response.toString())
                            val (bytes, error) = result
                            when (result) {
                                is Result.Success -> {
                                    Log.d("API/Auth", "Success")
                                    note_view_your_message_text.text.clear()
                                    note_view_your_message_text.clearFocus()
                                    fillCommentsWithData(post)
                                    progressDialog.dismiss()
                                }
                                is Result.Failure -> {
                                    if (bytes != null) {
                                        Log.e("API/Auth", bytes.toString())
                                    }
                                    if (error != null) {
                                        Log.e("API/Auth", error.toString())
                                    }
                                }
                            }
                        }
            }
            else {
                progressDialog.dismiss()
                MaterialDialog.Builder(this)
                        //TODO to resource
                        .title("Поле должно быть не пустым")
                        //TODO to resource
                        .content("К сожалению, вы не можете оставить это поле пустым")
                        //TODO to resource
                        .positiveText("Понятно")
                        .show()
            }
        }
    }

    private fun fillCommentsWithData(post: Post) {
        launch(Android) {
            val commentsResponse = Client.getComments(post.id)
            comments = commentsResponse.await()

            comments_count_text_box.text = "Комментариев: " + comments.size.toString()
            recyclerView.adapter = CommentsListAdapter(this@PostDetailsActivity, ArrayList(comments))
        }
    }

    override fun onBackPressed() = finish()

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.action_like_post -> {
                    launch(Android) {
                        val likeResponse = Client.likeNote(postId)
                        val (userLiked, likes) = likeResponse.await()
                        liked = userLiked

                        post_details_likes_count.text = likes.toString()

                        if (liked) {
                            item.setIcon(R.drawable.ic_favorite_blue_24dp)
                        } else {
                            item.setIcon(R.drawable.ic_favorite_white_24dp)
                        }
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.post_details_toolbar_menu, menu)
        if (accessToken == "") {
            menu.findItem(R.id.action_like_post).isVisible = false
        }
        if (liked) {
            menu.findItem(R.id.action_like_post).setIcon(R.drawable.ic_favorite_blue_24dp)
        } else {
            menu.findItem(R.id.action_like_post).setIcon(R.drawable.ic_favorite_white_24dp)
        }
        return true
    }
}