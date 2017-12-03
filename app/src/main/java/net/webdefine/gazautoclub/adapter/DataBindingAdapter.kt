package net.webdefine.gazautoclub.adapter

import android.databinding.BindingAdapter
import android.widget.ImageView

object DataBindingAdapter {

  @BindingAdapter("android:src")
  fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
  }
}
