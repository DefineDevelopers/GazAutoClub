package net.webdefine.gazautoclub.model

import java.io.Serializable

data class Comment(val id: Int,
                   val authorId: Int,
                   val authorName: String,
                   val content: String,
                   val time: String): Serializable