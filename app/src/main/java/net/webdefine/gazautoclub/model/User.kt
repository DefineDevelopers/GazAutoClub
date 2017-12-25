package net.webdefine.gazautoclub.model

import java.io.Serializable

data class User(val id: Int,
                val username: String,
                val email: String,
                val firstName: String?,
                val lastName: String?,
                val photo: String?): Serializable