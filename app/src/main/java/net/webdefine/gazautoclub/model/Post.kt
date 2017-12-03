package net.webdefine.gazautoclub.model

import java.io.Serializable

data class Post(val id: Int,
                val authorId: Int,
                val name: String,
                val description: String,
                var fullText: String,
                val imageResId: Int): Serializable