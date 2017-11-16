package net.webdefine.gazautoclub

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import net.webdefine.gazautoclub.databinding.RecyclerPostItemBinding
import org.jetbrains.anko.find

class MainFragment : Fragment() {
    private lateinit var authorIds: IntArray
    private lateinit var imageResIds: IntArray
    private lateinit var names: Array<String>
    private lateinit var descriptions: Array<String>
    private lateinit var mListener: OnPostSelected

    companion object {
        fun newInstance(index: Int): MainFragment {
            val fragment = MainFragment()
            fragment.arguments.putInt("index", index)
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnPostSelected) {
            mListener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnPostSelected.")
        }

        // Get posts names and descriptions.
        val resources = context.resources
        names = resources.getStringArray(R.array.names)
        descriptions = resources.getStringArray(R.array.descriptions)
        authorIds = resources.getIntArray(R.array.auth_ids)

        // Get posts images.
        val typedArray = resources.obtainTypedArray(R.array.images)
        val imageCount = names.size
        imageResIds = IntArray(imageCount)
        for (i in 0 until imageCount) {
            imageResIds[i] = typedArray.getResourceId(i, 0)
        }
        typedArray.recycle()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_post_list, container,
                false)
        val activity = activity
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = PostAdapter(activity)

        val navigation = view.find<AHBottomNavigation>(R.id.navigation)
        val navigationAdapter = AHBottomNavigationAdapter(activity, R.menu.navigation)
        navigationAdapter.setupWithBottomNavigation(navigation)

        navigation.defaultBackgroundColor = context.getColor(R.color.colorPrimary)
        navigation.accentColor = context.getColor(R.color.colorText)
        navigation.inactiveColor = context.getColor(R.color.colorPrimaryDark)
        navigation.isColored = true
        navigation.currentItem = 1
        navigation.titleState = AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE
        return view
    }

  internal inner class PostAdapter(context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
      val recyclerPostItemBinding = RecyclerPostItemBinding.inflate(layoutInflater,
          viewGroup, false)
      return ViewHolder(recyclerPostItemBinding.root, recyclerPostItemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val post = Post(authorIds[position], names[position], descriptions[position], imageResIds[position])
        viewHolder.setData(post)
        viewHolder.itemView.setOnClickListener { mListener.onPostSelected(post) }
    }

    override fun getItemCount(): Int = names.size
  }

  internal inner class ViewHolder constructor(itemView: View,
                                              private val recyclerPostItemBinding: RecyclerPostItemBinding) :
      RecyclerView.ViewHolder(itemView) {

    fun setData(post: Post) {
        recyclerPostItemBinding.post = post
    }
  }

    interface OnPostSelected {
        fun onPostSelected(pPost: Post)
    }
}