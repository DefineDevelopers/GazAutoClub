package net.webdefine.gazautoclub.database

import android.util.Log
import com.beust.klaxon.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.model.Comment
import net.webdefine.gazautoclub.model.User
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit

object Client {
    private var client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    private val parser = Parser()
    private var accessToken: String = ""
    var user: User = User(-1, "", "", "", "", "")

    fun registerClientWithAccessToken(accessToken: String) {
        this.accessToken = accessToken

        launch(Android) {
            val response = getUserInfo()
            user = response.await()
        }
    }

    fun regenerateRefreshToken(refreshToken: String): Deferred<JsonObject> {
        return async(CommonPool) {
            val requestBody = FormBody.Builder()
                    .add("client_id", App.instance.getString(R.string.client_id))
                    .add("client_secret", App.instance.getString(R.string.client_secret))
                    .add("grant_type", "refresh_token")
                    .add("refresh_token", refreshToken)
                    .build()

            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/auth/token/")
                    .post(requestBody)
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()

            parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
        }
    }

    fun getUserInfo(): Deferred<User> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/accounts/")
                    .header("Authorization", "Bearer $accessToken")
                    .get()
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            val id = parsed.array<JsonObject>("results")?.get(0)?.get("id") as Int
            val username = parsed.array<JsonObject>("results")?.get(0)?.get("username") as String
            val email = parsed.array<JsonObject>("results")?.get(0)?.get("email") as String
            val firstName = parsed.array<JsonObject>("results")?.get(0)?.get("first_name") as String
            val secondName = parsed.array<JsonObject>("results")?.get(0)?.get("last_name") as String

            val photoResponse = getUserPhoto(id)
            val photo = photoResponse.await()

            User(id, username, email, firstName, secondName, photo)
        }
    }

    fun getUserInfo(id: Int): Deferred<User> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/accounts/$id/")
                    .get()
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            val username = parsed.string("username") as String
            val email = parsed.string("email") as String
            val firstName = parsed.string("first_name") as String
            val secondName = parsed.string("last_name") as String

            val photoResponse = getUserPhoto(id)
            val photo = photoResponse.await()

            User(id, username, email, firstName, secondName, photo)
        }
    }

    fun getUserPhoto(id: Int): Deferred<String?> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/profiles/$id/")
                    .header("Authorization", "Bearer $accessToken")
                    .get()
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            parsed["photo"] as String?
        }
    }

    fun updateUserInfo(id: Int, field: String, value: String): Deferred<Response> {
        return async(CommonPool) {
            val requestBody = FormBody.Builder()
                    .add(field, value)
                    .build()
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/accounts/$id/")
                    .header("Authorization", "Bearer $accessToken")
                    .put(requestBody)
                    .build()

            client.newCall(request).execute()
        }
    }

    fun getTopics(): Deferred<JsonObject> {
        return async(CommonPool) {
            val requestBuilder  = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/topics/")
            if (accessToken != "") {
                requestBuilder
                        .addHeader("Authorization", "Bearer $accessToken")
                        .get()
            }
            else {
                requestBuilder.get()
            }
            val response = client.newCall(requestBuilder.build()).execute()

            val responseData = response.body()?.string()
            parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
        }
    }

    fun getTopics(author: Int): Deferred<JsonObject> {
        return async(CommonPool) {
            val requestBuilder  = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/topics/?author=$author")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()

            val response = client.newCall(requestBuilder.build()).execute()

            val responseData = response.body()?.string()
            parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
        }
    }

    fun getTopicFullText(id: Int) : Deferred<String> {
        return async(CommonPool) {
            val requestBuilder  = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/topics/$id")
            if (accessToken != "") {
                requestBuilder
                        .addHeader("Authorization", "Bearer $accessToken")
                        .get()
            }
            else {
                requestBuilder.get()
            }
            val response = client.newCall(requestBuilder.build()).execute()

            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            parsed.string("body") as String
        }
    }

    fun getAuthorInfo(author: Int): Deferred<String> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/accounts/$author/")
                    .get()
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            parsed.string("username") ?: ""
        }
    }

    fun getCarBrandInfo(brand: Int): Deferred<String> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/brands/$brand/")
                    .get()
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            parsed.string("name") ?: ""
        }
    }

    fun getCarModelInfo(model: Int): Deferred<String> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/models/$model/")
                    .get()
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
            val brand = parsed.int("brand")

            val brandName: String

            val brandResult = getCarBrandInfo(brand ?: 0)
            brandName = brandResult.await()

            brandName + " " + parsed.string("name")
        }
    }

    fun getComments(post: Int): Deferred<MutableList<Comment>> {
        return async(CommonPool) {
            val requestBuilder  = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/comments/?topic=$post")
                    .get()

            val response = client.newCall(requestBuilder.build()).execute()

            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
            val count = parsed.int("count") as Int

            val comments: MutableList<Comment> = ArrayList()
            for (i in IntRange(0, count - 1)) {
                val id = parsed.array<JsonObject>("results")?.get(i)?.get("id") as Int
                val body = parsed.array<JsonObject>("results")?.get(i)?.get("body") as String
                val time = App.formatToYesterdayOrToday(
                        (parsed.array<JsonObject>("results")?.get(i)?.get
                        ("creation_time") as String)
                )
                val topic = parsed.array<JsonObject>("results")?.get(i)?.get("topic") as Int
                val author = parsed.array<JsonObject>("results")?.get(i)?.get("author") as Int
                val responseFor = parsed.array<JsonObject>("results")?.get(i)?.get("response_for") as Int?

                val authorResponse = getUserInfo(author)
                val authorData = authorResponse.await()
                val authorName = authorData.username

                comments.add(Comment(id, author, authorName, body, time, topic, responseFor))
            }

            comments
        }
    }

    fun uploadImageFileOnServer(file: File, filename: String): Deferred<String> {
        return async(CommonPool) {
            val MEDIA_TYPE_IMAGE = MediaType.parse("application/octet-stream")
            val requestBody = RequestBody.create(MEDIA_TYPE_IMAGE, file)
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/upload/$filename/")
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

            val response = client.newCall(request).execute()

            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))).apply { Log.d("Client/Upload/Response", (this as JsonObject).toJsonString(true)) } as JsonObject

            if (parsed.containsKey("detail")) {
                parsed.string("detail") as String
            } else {
                parsed.string("url") as String
            }
        }
    }

    fun putProfileImage(userId: Int, imageUrl: String): Deferred<JsonObject> {
        return async(CommonPool) {
            val requestBody = FormBody.Builder()
                    .add("user", userId.toString())
                    .add("photo", imageUrl)
                    .build()

            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/profiles/$userId/")
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()

            parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
        }
    }

    fun createNote(title: String, body: String, photo: String, carModel: Int): Deferred<JsonObject> {
        return async(CommonPool) {
            val requestBody = FormBody.Builder()
                    .add("title", title)
                    .add("body", body)
                    .add("photo", photo)
                    .add("car_model", carModel.toString())
                    .build()

            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/topics/")
                    .header("Authorization", "Bearer $accessToken")
                    .post(requestBody)
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()

            parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
        }
    }

    fun likeNote(post: Int): Deferred<Pair<Boolean, Int>> {
        return async(CommonPool) {
            val request = Request.Builder()
                    .url("https://gazautoclub.herokuapp.com/topics/$post/like/")
                    .header("Authorization", "Bearer $accessToken")
                    .post(FormBody.Builder().build())
                    .build()

            val response = client.newCall(request).execute()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject

            Pair(parsed.boolean("is_liked") ?: false,
                    parsed.int("likes_counter") ?: 0)
        }
    }
}