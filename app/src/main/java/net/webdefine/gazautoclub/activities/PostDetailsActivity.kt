package net.webdefine.gazautoclub.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_post_details.*
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.adapter.CommentsListAdapter
import net.webdefine.gazautoclub.extensions.circularReveal
import net.webdefine.gazautoclub.extensions.generatePalette
import net.webdefine.gazautoclub.model.Comment
import net.webdefine.gazautoclub.model.Post

class PostDetailsActivity : AppCompatActivity() {

    private lateinit var ids: IntArray
    private lateinit var authorIds: IntArray
    private lateinit var authorNames: Array<String>
    private lateinit var times: Array<String>
    private lateinit var contents: Array<String>
    private          var comments: ArrayList<Comment>   = ArrayList()

    companion object {
        val POST_ID = "post_id"

        /*@JvmStatic fun getIntent(context: Context, post: Post): Intent {
            val args = Bundle()
            args.putSerializable(PostDetailsActivity.POST_ID, post as Serializable)
            val intent = Intent(context, PostDetailsActivity::class.java)
            intent.putExtras(args)
            return intent
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        val post = intent.extras.getSerializable(PostDetailsActivity.POST_ID) as Post

        setSupportActionBar(post_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val collapsingToolbarLayout = toolbar_layout
        collapsingToolbarLayout.title = "Запись"
        collapsingToolbarLayout.setExpandedTitleColor(getColor(android.R.color.transparent))

        post_title.text = post.name
        post_full_text.text = post.fullText
        post_image.setImageResource(post.imageResId)
        setToolbarColorFromImage()

        val resources = App.instance.resources
        ids = resources.getIntArray(R.array.comment_ids)
        authorIds = resources.getIntArray(R.array.comment_auth_ids)
        authorNames = resources.getStringArray(R.array.comment_auth_names)
        times = resources.getStringArray(R.array.comment_times)
        contents = resources.getStringArray(R.array.comment_content)

        for (i in 0 until ids.size) {
            comments.add(Comment(ids[i], authorIds[i], authorNames[i], contents[i], times[i]))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.post_comments_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CommentsListAdapter(this, comments)
    }

    /*private fun loadRepositoryImage(imageUrl: String) {
        post_image.loadUrl(imageUrl) {
            onSuccess {
                setToolbarColorFromImage()
            }
            onError {
                // TODO
                toolbar_layout.visibility = View.VISIBLE
            }
        }
    }*/

    private fun setToolbarColorFromImage() {
        post_image.generatePalette gen@ {
            val swatch = it.mutedSwatch ?: it.vibrantSwatch ?: it.lightMutedSwatch ?: it.lightVibrantSwatch ?: return@gen
            val backgroundColor = swatch.rgb
            val titleTextColor = swatch.titleTextColor

            post_toolbar.setTitleTextColor(titleTextColor)
            toolbar_layout.circularReveal(backgroundColor)
            toolbar_layout.setContentScrimColor(backgroundColor)
        }
    }

    override fun onBackPressed() = finish()

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}