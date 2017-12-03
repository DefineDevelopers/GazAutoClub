package net.webdefine.gazautoclub.database

object PostTable {
    val NAME              = "Post"
    val ID                = "_id"
    val AUTHOR_ID         = "author_id"
    val DATE              = "date"
    val TITLE             = "title"
    val DESCRIPTION       = "description"
    val FULL_TEXT         = "full_text"
    val IMAGE_RESOURCE_ID = "image_resource_id"
    val LIKES             = "likes"
    val COMMENTS          = "comments"
}

object PersonTable {
    val NAME              = "Person"
    val ID                = "_id"
    val FIRST_NAME        = "first_name"
    val SECOND_NAME       = "second_name"
    val EMAIL             = "email"
    val PASSWORD          = "password"
    val POSTS             = "posts"
}