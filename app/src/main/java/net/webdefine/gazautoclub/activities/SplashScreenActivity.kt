package net.webdefine.gazautoclub.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.beust.klaxon.string
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.database.Client
import net.webdefine.gazautoclub.database.LocalPreferences

class SplashScreenActivity : AppCompatActivity() {
    private val ACCESS_TOKEN = "access_token"
    private val REFRESH_TOKEN = "refresh_token"
    private val LOGIN_ACTIVITY = 0
    private val MAIN_ACTIVITY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (LocalPreferences.checkIfContain(REFRESH_TOKEN) && LocalPreferences.getString(REFRESH_TOKEN, "") != "") {
            var refreshToken = LocalPreferences.getString(REFRESH_TOKEN, "")
            Log.d("SplashScreen", refreshToken)
            launch(Android) {
                val result = Client.regenerateRefreshToken(refreshToken)
                val parsed = result.await()

                Log.d("SplashScreen/Response", parsed.toJsonString(true))

                if (parsed.containsKey("error")) {
                    // revoke refresh token
                    LocalPreferences.delete(REFRESH_TOKEN)

                    goToActivity(LOGIN_ACTIVITY)
                }
                else {
                    var accessToken = ""
                    if (parsed.containsKey(ACCESS_TOKEN)) {
                        accessToken = parsed.string(ACCESS_TOKEN) ?: ""
                    }

                    if (parsed.containsKey(REFRESH_TOKEN)) {
                        refreshToken = parsed.string(REFRESH_TOKEN) ?: ""
                    }

                    Client.registerClientWithAccessToken(accessToken)

                    LocalPreferences.addString(REFRESH_TOKEN, refreshToken)
                    goToActivity(MAIN_ACTIVITY, accessToken)
                }
            }
        }
        else {
            goToActivity(LOGIN_ACTIVITY)
        }
    }

    private fun goToActivity(activity: Int, accessToken: String = "") {
        if (activity == LOGIN_ACTIVITY) {
            startActivity(Intent(App.instance, LoginActivity::class.java))
        }
        else if (activity == MAIN_ACTIVITY) {
            startActivity(Intent(App.instance, MainActivity::class.java)
                    .apply { putExtra(ACCESS_TOKEN, accessToken) })
        }

        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }
}
