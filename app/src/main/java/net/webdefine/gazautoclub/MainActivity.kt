package net.webdefine.gazautoclub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.webdefine.gazautoclub.R.id.root_layout
import net.webdefine.gazautoclub.R.layout.activity_main

class MainActivity : AppCompatActivity(), MainFragment.OnPostSelected {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(root_layout, MainFragment.newInstance(1), "postsList")
                .commit()
        }
    }

    override fun onPostSelected(pPost: Post) {
        val detailsFragment = PostDetailsFragment.newInstance(pPost)

        supportFragmentManager.beginTransaction()
            .replace(root_layout, detailsFragment, "postDetails")
            .addToBackStack(null)
            .commit()
    }
}