package net.webdefine.gazautoclub

import java.io.Serializable

data class Post(val authorId: Int,
                val name: String,
                val description: String,
                val imageResId: Int,
                var text: String = ""): Serializable