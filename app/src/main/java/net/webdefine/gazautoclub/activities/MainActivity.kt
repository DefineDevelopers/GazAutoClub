package net.webdefine.gazautoclub.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.array
import com.beust.klaxon.int
import kotlinx.android.synthetic.main.activity_main.*
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.R.layout.activity_main
import net.webdefine.gazautoclub.activities.PostDetailsActivity.Companion.ACCESS_TOKEN
import net.webdefine.gazautoclub.activities.PostDetailsActivity.Companion.POST
import net.webdefine.gazautoclub.fragments.FeedFragment
import net.webdefine.gazautoclub.fragments.MainFeedFragment
import net.webdefine.gazautoclub.fragments.UserSettingsFragment
import net.webdefine.gazautoclub.fragments.UsersPostsFeedFragment
import net.webdefine.gazautoclub.model.Brand
import net.webdefine.gazautoclub.model.Post
import okhttp3.*
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.find
import java.io.IOException

class MainActivity : AppCompatActivity(),
                        FeedFragment.OnPostSelected,
                        AHBottomNavigation.OnTabSelectedListener {
    private var accessToken : String = ""
    val PREFS_FILENAME = "net.webdefine.gazautoclub.prefs"
    val REFRESH_TOKEN = "refresh_token"
    private lateinit var prefs: SharedPreferences

    private          var BRANDS_EXTRA = "BRANDS_LIST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        setSupportActionBar(main_toolbar)
        //TODO to resource
        supportActionBar!!.title = "Все Записи"

        prefs = getSharedPreferences(PREFS_FILENAME, 0)
        accessToken = intent.extras.getString("access_token")

        val navigation = find<AHBottomNavigation>(R.id.navigation)

        if (accessToken == "") {
            navigation.visibility = View.GONE
        }

        val navigationAdapter = AHBottomNavigationAdapter(this, R.menu.navigation)
        navigationAdapter.setupWithBottomNavigation(navigation)

        navigation.defaultBackgroundColor = ContextCompat.getColor(App.instance, R.color.colorDivider)
        navigation.accentColor = ContextCompat.getColor(App.instance, R.color.colorPrimaryDark)
        navigation.inactiveColor = ContextCompat.getColor(App.instance, R.color.colorTextSecondary)
        navigation.isBehaviorTranslationEnabled = false
        navigation.isColored = false
        navigation.currentItem = 1
        navigation.titleState = AHBottomNavigation.TitleState.ALWAYS_HIDE
        navigation.setOnTabSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.list_container, MainFeedFragment.newInstance(accessToken), "postsList")
                    .commit()
        }
    }

    override fun onPostSelected(pPost: Post) {
        startActivity(Intent(App.instance, PostDetailsActivity::class.java).apply {
            putExtra(POST, pPost).putExtra(ACCESS_TOKEN, accessToken)
        })
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.action_new_post -> {
                    prepareToStartNewNoteIntent()
                    true
                }
                R.id.action_sign_out -> {
                    signOut()
                    true
                }
                R.id.action_sign_in  -> {
                    signIn()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        if (accessToken == "") {
            menu.findItem(R.id.action_sign_out).isVisible = false
            menu.findItem(R.id.action_new_post).isVisible = false
            menu.findItem(R.id.action_sign_in) .isVisible = true
        }
        return true
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        when (position) {
            0 -> {
                if (!wasSelected) {
                    val usersPostsFeedFragment = UsersPostsFeedFragment.newInstance(accessToken)

                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.list_container, usersPostsFeedFragment, "usersPosts")
                            .commit()

                    //TODO to resource
                    supportActionBar!!.title = "Ваши Записи"
                }
            }
            1 -> {
                if (!wasSelected) {
                    val mainFeedFragment = MainFeedFragment.newInstance(accessToken)

                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.list_container, mainFeedFragment, "mainFeed")
                            .commit()

                    //TODO to resource
                    supportActionBar!!.title = "Все Записи"
                }
            }
            2 -> {
                if (!wasSelected) {
                    val usersSettingsFragment = UserSettingsFragment.newInstance(accessToken)

                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.list_container, usersSettingsFragment, "usersSettings")
                            .commit()

                    //TODO to resource
                    supportActionBar!!.title = "Настройки"
                }
            }
        }

        return true
    }

    fun startNewNoteIntent(brands: ArrayList<Brand>) {
        startActivity(
                Intent(App.instance, NewNoteActivity::class.java)
                        .putExtra(ACCESS_TOKEN, accessToken)
                        .putExtra(BRANDS_EXTRA, brands)
        )
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }

    private fun prepareToStartNewNoteIntent() {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("https://gazautoclub.herokuapp.com/brands/")
                .build()
        val getBrandsRequestCallback = GetBrandsRequestCallback()
        async {
            client.newCall(request).enqueue(getBrandsRequestCallback)
        }
    }

    inner class GetBrandsRequestCallback : Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            onFailure(null, e)
        }

        override fun onResponse(call: Call?, response: Response?) {
            Log.d("MainActivity", response.toString())
            if (!response!!.isSuccessful) {
                onFailure(call, null)
                return
            }
            val parser = Parser()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
            Log.d("MainActivity", parsed.toJsonString(true))
            val count = parsed.int("count") ?: 0
            val brands = ArrayList<Brand>(count)

            for (i in IntRange(0, count - 1)) {
                val id = parsed.array<JsonObject>("results")?.get(i)?.get("id") as Int
                val name = parsed.array<JsonObject>("results")?.get(i)?.get("name") as String
                val photoUrl = parsed.array<JsonObject>("results")?.get(i)?.get("image") as String?
                brands.add(Brand(id, name, photoUrl))
            }

            startNewNoteIntent(brands)
        }
    }

    private fun signOut() {
        prefs.edit().putString(REFRESH_TOKEN, "").apply()
        startActivity(Intent(App.instance, LoginActivity::class.java))
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }

    private fun signIn() {
        startActivity(Intent(App.instance, LoginActivity::class.java))
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }
}