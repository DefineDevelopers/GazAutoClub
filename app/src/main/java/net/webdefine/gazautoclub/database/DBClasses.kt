package net.webdefine.gazautoclub.database

class Post(map: MutableMap<String, Any?>) {
    var id: Long by map
    var authorId: Long by map
    var date: Long by map
    var title: String by map
    var description: String by map
    var fullText: String by map
    var imageResourceId: String by map
    var likes: Int by map
    var comments: Int by map

    constructor(authorId: Long,
                date: Long,
                title: String,
                description: String,
                fullText: String,
                imageResourceId: String,
                likes: Int,
                comments: Int
    ) : this(HashMap()) {
        this.authorId = authorId
        this.date = date
        this.title = title
        this.description = description
        this.fullText = fullText
        this.imageResourceId = imageResourceId
        this.likes = likes
        this.comments = comments
    }
}

class Person(map: MutableMap<String, Any?>, val posts: List<Post>) {
    var id: Long by map
    var firstName: String by map
    var secondName: String by map
    var email: String by map
    var password: String by map

    constructor(firstName: String,
                secondName: String,
                email: String,
                password: String,
                posts: List<Post>
    ) : this(HashMap(), posts) {
        this.firstName = firstName
        this.secondName = secondName
        this.email = email
        this.password = password
    }
}