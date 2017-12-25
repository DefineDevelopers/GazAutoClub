package net.webdefine.gazautoclub.model

import java.io.Serializable

data class Post(val id: Int,
                val authorId: Int,
                val authorName: String,
                val name: String,
                val description: String,
                var fullText: String,
                val image: String,
                val time: String,
                val car: Int,
                val carName: String,
                val comments: Int,
                val likes: Int,
                val userLiked: Boolean): Serializable