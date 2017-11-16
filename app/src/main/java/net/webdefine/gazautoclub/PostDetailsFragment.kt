package net.webdefine.gazautoclub

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.webdefine.gazautoclub.databinding.FragmentPostDetailsBinding
import java.io.Serializable

class PostDetailsFragment : Fragment() {
  companion object {
      private const val POST = "post"

      fun newInstance(pPost: Post): PostDetailsFragment {
          val args = Bundle()
          args.putSerializable(POST, pPost as Serializable)
          val fragment = PostDetailsFragment()
          fragment.arguments = args
          return fragment
      }
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
      val fragmentPostDetailsBinding = FragmentPostDetailsBinding.inflate(inflater!!,
              container, false)

      val post = arguments.getSerializable(POST) as Post
      fragmentPostDetailsBinding.post = post
      post.text = post.description
      return fragmentPostDetailsBinding.root
  }
}