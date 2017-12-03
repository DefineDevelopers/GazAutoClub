package net.webdefine.gazautoclub.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import kotlinx.android.synthetic.main.activity_main.*
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.R.layout.activity_main
import net.webdefine.gazautoclub.activities.PostDetailsActivity.Companion.POST_ID
import net.webdefine.gazautoclub.fragments.FeedFragment
import net.webdefine.gazautoclub.fragments.MainFeedFragment
import net.webdefine.gazautoclub.fragments.UserSettingsFragment
import net.webdefine.gazautoclub.fragments.UsersPostsFeedFragment
import net.webdefine.gazautoclub.model.Post
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), FeedFragment.OnPostSelected, AHBottomNavigation.OnTabSelectedListener {
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        setSupportActionBar(main_toolbar)
        supportActionBar!!.title = "Все Записи"

        userId = intent.extras.getInt("user_id")

        val navigation = find<AHBottomNavigation>(R.id.navigation)
        val navigationAdapter = AHBottomNavigationAdapter(this, R.menu.navigation)
        navigationAdapter.setupWithBottomNavigation(navigation)

        navigation.defaultBackgroundColor = ContextCompat.getColor(App.instance, R.color.colorDivider)
        navigation.accentColor = ContextCompat.getColor(App.instance, R.color.colorPrimaryDark)
        navigation.inactiveColor = ContextCompat.getColor(App.instance, R.color.colorTextSecondary)
        navigation.isBehaviorTranslationEnabled = false
        navigation.isColored = false
        navigation.currentItem = 1
        navigation.titleState = AHBottomNavigation.TitleState.ALWAYS_HIDE
        navigation.setNotification("5", 1)
        navigation.setOnTabSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.list_container, MainFeedFragment.newInstance(), "postsList")
                    .commit()
        }
    }

    override fun onPostSelected(pPost: Post) {
        startActivity(Intent(App.instance, PostDetailsActivity::class.java).apply {
            putExtra(POST_ID, pPost)
        })
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.action_new_post -> {
                    toast("New Post")
                    true
                }
                R.id.action_sign_out-> {
                    signOut()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        when (position) {
            0 -> {
                if (!wasSelected) {
                    val usersPostsFeedFragment = UsersPostsFeedFragment.newInstance(userId)

                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.list_container, usersPostsFeedFragment, "usersPosts")
                            .commit()

                    supportActionBar!!.title = "Ваши Записи"
                }
            }
            1 -> {
                if (!wasSelected) {
                    val mainFeedFragment = MainFeedFragment.newInstance()

                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.list_container, mainFeedFragment, "mainFeed")
                            .commit()

                    supportActionBar!!.title = "Все Записи"
                }
            }
            2 -> {
                if (!wasSelected) {
                    val usersSettingsFragment = UserSettingsFragment.newInstance(userId)

                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.list_container, usersSettingsFragment, "usersSettings")
                            .commit()

                    supportActionBar!!.title = "Настройки"
                }
            }
        }

        return true
    }

    private fun signOut() {
        startActivity(Intent(App.instance, LoginActivity::class.java))
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }
}