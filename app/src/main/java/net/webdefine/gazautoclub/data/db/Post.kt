package net.webdefine.gazautoclub.data.db

import java.util.*

data class Post(val id: Long,
                val authorId: Long,
                val date: Date,
                val commentsCount: Int,
                val context: String,
                val coverPath: String
)