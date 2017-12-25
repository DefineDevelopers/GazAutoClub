package net.webdefine.gazautoclub.model

import java.io.Serializable

data class Comment(val id: Int,
                   val authorId: Int,
                   val authorName: String,
                   val body: String,
                   val time: String,
                   val topic: Int,
                   val responseFor: Int?): Serializable