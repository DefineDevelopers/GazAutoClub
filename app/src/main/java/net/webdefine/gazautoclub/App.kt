package net.webdefine.gazautoclub

import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKAccessTokenTracker
import com.vk.sdk.VKSdk
import net.webdefine.gazautoclub.delegates.DelegatesExt
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class App : MultiDexApplication() {
    companion object {
        var instance: App by DelegatesExt.notNullSingleValue()
        internal var vkAccessTokenTracker: VKAccessTokenTracker = object : VKAccessTokenTracker() {
            override fun onVKAccessTokenChanged(oldToken: VKAccessToken?, newToken: VKAccessToken?) {
                if (newToken == null) {
                    TODO("")
                }
            }
        }

        fun formatToYesterdayOrToday(date: String): String {
            val dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").parseDateTime(date)
            val today = DateTime()
            val yesterday = today.minusDays(1)
            val timeFormatter = DateTimeFormat.forPattern("HH:mm")

            return when {
                dateTime.toLocalDate() == today.toLocalDate() -> "Today " + timeFormatter.print(dateTime)
                dateTime.toLocalDate() == yesterday.toLocalDate() -> "Yesterday " + timeFormatter.print(dateTime)
                else -> dateTime.toString("dd MMMM yyyy HH:mm", Locale("ru"))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        vkAccessTokenTracker.startTracking()
        VKSdk.initialize(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}