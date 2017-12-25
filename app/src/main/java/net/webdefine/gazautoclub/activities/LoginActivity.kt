package net.webdefine.gazautoclub.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.array
import com.beust.klaxon.string
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VKApiUser
import com.vk.sdk.api.model.VKList
import kotlinx.android.synthetic.main.activity_login.*
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.database.Client
import net.webdefine.gazautoclub.database.LocalPreferences
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var loginText              : TextInputEditText
    private lateinit var emailText              : TextInputEditText
    private lateinit var passwordText           : TextInputEditText
    private lateinit var fullNameText           : TextInputEditText
    private lateinit var loginTextLayout        : TextInputLayout
    private lateinit var emailTextLayout        : TextInputLayout
    private lateinit var passwordTextLayout     : TextInputLayout
    private lateinit var fullNameTextLayout     : TextInputLayout
    private lateinit var loginButton            : Button
    private lateinit var registerButton         : Button
    private lateinit var loginNewButton         : Button
    private lateinit var registerNewButton      : Button
    private lateinit var googleAuthButton       : Button
    private lateinit var vkAuthButton           : Button
    private lateinit var anonymousButton        : Button
    private lateinit var forgotPassButton       : Button
    private lateinit var restorePasswordButton  : Button
    private lateinit var logoText               : TextView
    private lateinit var orText                 : TextView
    private lateinit var orFrameLayout          : FrameLayout
    private lateinit var fullNameLayoutParams   : ConstraintLayout.LayoutParams
    private lateinit var emailLayoutParams      : ConstraintLayout.LayoutParams
    private lateinit var passwordLayoutParams   : ConstraintLayout.LayoutParams
    private lateinit var forgotPassButtonParams : ConstraintLayout.LayoutParams

    private val REGISTER_STATE       = 0
    private val LOGIN_STATE          = 1
    private val RESTORE_STATE        = 2
    private val PASSWORD_PATTERN     = "((?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,})"
    private val FULLNAME_PATTERN     = "^[\\p{L} .'-]+$"
    private val ACCESS_TOKEN = "access_token"
    private val REFRESH_TOKEN = "refresh_token"

    private var errorAtLogin        = false
    private var errorAtRegister     = false
    private var statement           = LOGIN_STATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        loginText = input_login
        emailText = input_email
        passwordText = input_password
        fullNameText = input_fullname
        passwordTextLayout = password_text_layout
        fullNameTextLayout = fullname_text_layout
        loginTextLayout = login_text_layout
        emailTextLayout = email_text_layout
        loginButton = login
        registerButton = register
        loginNewButton = login_new
        registerNewButton = register_new
        googleAuthButton = google_si
        vkAuthButton = vk_si
        anonymousButton = anonymous
        forgotPassButton = forgot_password
        restorePasswordButton = restore_new
        logoText = logo_text
        orText = or_text
        orFrameLayout = or_frame_layout

        fullNameLayoutParams = fullNameTextLayout.layoutParams as ConstraintLayout.LayoutParams
        emailLayoutParams = loginTextLayout.layoutParams as ConstraintLayout.LayoutParams
        passwordLayoutParams = passwordTextLayout.layoutParams as ConstraintLayout.LayoutParams
        forgotPassButtonParams = restorePasswordButton.layoutParams as ConstraintLayout.LayoutParams

        val elleyFont: Typeface? = Typeface.createFromAsset(assets, "fonts/Elley.otf")
        logoText.typeface = elleyFont

        vkAuthButton.onClick {
            VKSdk.login(this@LoginActivity, "email")
        }

        anonymousButton.onClick {
            startActivity(Intent(App.instance, MainActivity::class.java).apply {
                putExtra(ACCESS_TOKEN, "")
                putExtra(REFRESH_TOKEN, "")
            })
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
            finish()
        }

        registerNewButton.onClick {
            if (!registerValidate()) {
                errorAtRegister = true

                return@onClick
            }
            val fullName = fullNameText.text.split(" ")
            Log.d("Fullname", fullName.toString())
            Log.d("FullnameSize", fullName.size.toString())
            when {
                fullName.size >= 2 -> register(loginText.text.toString().toLowerCase(),
                        emailText.text.toString().toLowerCase(),
                        passwordText.text.toString(),
                        fullName[0],
                        fullName[1])
                fullName.size == 1 -> register(loginText.text.toString().toLowerCase(),
                        emailText.text.toString().toLowerCase(),
                        passwordText.text.toString(),
                        fullName[0],
                        "")
                else -> register(loginText.text.toString().toLowerCase(),
                        emailText.text.toString().toLowerCase(),
                        passwordText.text.toString(),
                        "",
                        "")
            }
        }

        loginButton.onClick {
            if (!loginValidate()) {
                errorAtLogin = true

                return@onClick
            }

            login(loginText.text.toString().toLowerCase(), passwordText.text.toString())
        }

        registerButton.onClick {
            changeUi(REGISTER_STATE)
        }

        loginNewButton.onClick {
            changeUi(LOGIN_STATE)
        }

        /*forgotPassButton.onClick {
        if (passwordTextLayout.visibility == View.VISIBLE) {
            changeUi(RESTORE_STATE)
        }
        else {
            changeUi(LOGIN_STATE)
        }

        restorePasswordButton.onClick {
            val email = loginText.text.toString()

            if (!email.isEmpty()) {
                alert(getString(R.string.pass_restore_description), getString(R.string.pass_restore)) {
                    positiveButton(getString(R.string.ok)) { it.dismiss() }
                }.show()
            }
            else {
                loginTextLayout.error = getString(R.string.error_field_required)
            }
        }*/
    }

    private fun changeUi(task: Int) {
        when(task) {
            REGISTER_STATE -> {
                statement = REGISTER_STATE

                orFrameLayout.visibility            = View.GONE
                //googleAuthButton.visibility         = View.GONE
                vkAuthButton.visibility             = View.GONE
                anonymousButton.visibility          = View.GONE
                registerButton.visibility           = View.GONE
                loginButton.visibility              = View.GONE
                //forgotPassButton.visibility         = View.GONE

                registerNewButton.visibility        = View.VISIBLE
                loginNewButton.visibility           = View.VISIBLE
                fullNameTextLayout.visibility       = View.VISIBLE
                emailTextLayout.visibility          = View.VISIBLE
            }
            LOGIN_STATE -> {
                statement = LOGIN_STATE

                val constraintSet = ConstraintSet()
                constraintSet.clone(container)
                constraintSet.connect(forgot_password.id, ConstraintSet.TOP, register.id, ConstraintSet.BOTTOM)
                //forgotPassButton.text               = getString(R.string.forgot_your_password)
                constraintSet.applyTo(container)

                emailTextLayout.visibility          = View.GONE
                fullNameTextLayout.visibility       = View.GONE
                registerNewButton.visibility        = View.GONE
                loginNewButton.visibility           = View.GONE
                //restorePasswordButton.visibility    = View.GONE

                orFrameLayout.visibility            = View.VISIBLE
                //googleAuthButton.visibility         = View.VISIBLE
                vkAuthButton.visibility             = View.VISIBLE
                anonymousButton.visibility          = View.VISIBLE
                registerButton.visibility           = View.VISIBLE
                loginButton.visibility              = View.VISIBLE
                //forgotPassButton.visibility         = View.VISIBLE
                passwordTextLayout.visibility       = View.VISIBLE
            }
            RESTORE_STATE -> {
                statement = RESTORE_STATE

                val constraintSet = ConstraintSet()
                constraintSet.clone(container)
                constraintSet.connect(forgot_password.id, ConstraintSet.TOP, restore_new.id, ConstraintSet.BOTTOM)
                forgotPassButton.text               = getString(R.string.back_to_login)
                constraintSet.applyTo(container)

                fullNameTextLayout.visibility       = View.GONE
                registerNewButton.visibility        = View.GONE
                loginNewButton.visibility           = View.GONE
                orFrameLayout.visibility            = View.GONE
                googleAuthButton.visibility         = View.GONE
                vkAuthButton.visibility             = View.GONE
                anonymousButton.visibility          = View.GONE
                registerButton.visibility           = View.GONE
                loginButton.visibility              = View.GONE
                passwordTextLayout.visibility       = View.GONE

                restorePasswordButton.visibility    = View.VISIBLE
            }
        }

        fullNameTextLayout.error = null
        loginTextLayout.error = null
        passwordTextLayout.error = null
    }

    private fun loginValidate(): Boolean {
        val email = loginText.text.toString()
        val password = passwordText.text.toString()

        if (email.isEmpty()) {
            loginText.error = getString(R.string.error_field_required)
            if (password.isEmpty()) {
                passwordText.error = getString(R.string.error_field_required)
                return false
            }
            else {
                passwordText.error = null
            }
            return false
        }
        else {
            loginText.error = null
            if (password.isEmpty()) {
                passwordText.error = getString(R.string.error_field_required)
                return false
            }
            else {
                passwordText.error = null
            }
        }

        return true
    }

    private fun registerValidate(): Boolean {
        val fullName = fullNameText.text.toString()
        val email = emailText.text.toString()
        val password = passwordText.text.toString()

        val fullNamePattern = Pattern.compile(FULLNAME_PATTERN)
        var matcher = fullNamePattern.matcher(fullName)

        if (fullName.isEmpty()) {
            fullNameText.error = getString(R.string.error_field_required)
            if (email.isEmpty()) {
                loginText.error = getString(R.string.error_field_required)
                if (password.isEmpty()) {
                    passwordText.error = getString(R.string.error_field_required)

                    return false
                }
                else {
                    passwordText.error = null
                }

                return false
            }
            else {
                loginText.error = null
            }

            return false
        }
        else {
            fullNameText.error = null
            if (email.isEmpty()) {
                loginText.error = getString(R.string.error_field_required)
                if (password.isEmpty()) {
                    passwordText.error = getString(R.string.error_field_required)
                    return false
                }
                else {
                    passwordText.error = null
                }

                return false
            }
            else {
                loginText.error = null
                loginText.error = getString(R.string.error_field_required)

                if (password.isEmpty()) {
                    passwordText.error = getString(R.string.error_field_required)
                    return false
                }
                else {
                    passwordText.error = null
                }
            }
        }

        if (matcher.matches()) {
            fullNameText.error = null
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                loginText.error = getString(R.string.error_incorrect_email)
                val passwordPattern = Pattern.compile(PASSWORD_PATTERN)
                matcher = passwordPattern.matcher(password)

                if (matcher.matches()) {
                    passwordText.error = null
                }
                else {
                    passwordText.error = getString(R.string.error_weak_password)
                    return false
                }

                return false
            }
            else {
                loginText.error = null
            }
        }
        else {
            fullNameText.error = getString(R.string.error_unavailable_fullname)
            return false
        }

        return true
    }

    private fun login(username: String, password: String) {
        val loginDialog = MaterialDialog.Builder(this)
                .content(R.string.progress_dialog_auth)
                .progress(true, 0)
                .show()

        val parser = Parser()
        Fuel.post("https://gazautoclub.herokuapp.com/auth/token/",
                listOf( "client_id" to getString(R.string.client_id),
                        "client_secret" to getString(R.string.client_secret),
                        "grant_type" to "password",
                        "username" to username,
                        "password" to password
                )
        ).response { _, response, result ->
            run {
                Log.e("API/Auth", "Response: " + String(response.data))
                val parsed = parser.parse(StringBuilder(String(response.data))) as JsonObject
                when (result) {
                    is Result.Success -> {
                        Log.d("API/Auth", "Success.")
                        loginDialog.dismiss()

                        val accessToken = parsed.string(ACCESS_TOKEN)
                        val refreshToken = parsed.string(REFRESH_TOKEN)

                        Client.registerClientWithAccessToken(accessToken!!)

                        LocalPreferences.addString(REFRESH_TOKEN, refreshToken)
                        finishAndStartMainActivity(accessToken)
                    }
                    is Result.Failure -> {
                        loginDialog.dismiss()
                        Log.e("API/Auth", "Failure: " + result.error.toString())
                        MaterialDialog.Builder(this@LoginActivity)
                                //TODO to resource
                            .title("Ошибка аутентификации")
                            .content(R.string.error_not_match_fields)
                                //TODO to resource
                            .positiveText("ОК")
                            .show()
                    }
                }
            }
        }
    }

    private fun register(username: String, email: String, password: String, firstName: String, lastName: String) {
        Log.d("REGISTER", firstName)
        Log.d("REGISTER", lastName)
        val registerDialog = MaterialDialog.Builder(this@LoginActivity)
                .title(R.string.progress_dialog_register)
                .progress(true, 0)
                .show()

        val parser = Parser()
        Fuel.post("https://gazautoclub.herokuapp.com/accounts/",
                listOf( "username" to username,
                        "password" to password,
                        "email" to email,
                        "first_name" to firstName,
                        "last_name" to lastName)
        ).response { _, response, result ->
            run {
                var parsed = parser.parse(StringBuilder(String(response.data))) as JsonObject
                Log.d("Register/Response", parsed.toJsonString(true))
                Log.e("API/Auth", "Response: " + String(response.data))
                when (result) {
                    is Result.Failure -> {
                        Log.e("API/Auth", "Failure: " + result.error.toString())
                        registerDialog.dismiss()
                        MaterialDialog.Builder(this@LoginActivity)
                                //TODO to resource
                                .title("Ошибка регистрации")
                                .content(R.string.error_weak_password)
                                .positiveText("OK")
                                .show()
                    }
                    is Result.Success -> {
                        Log.d("API/Auth", "Success.")
                        registerDialog.dismiss()
                        login(username, password)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object
            : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken) {
                val parser = Parser()

                val loginDialog = MaterialDialog.Builder(this@LoginActivity)
                        .content(R.string.progress_dialog_auth)
                        .progress(true, 0)
                        .show()

                Log.d("API/Auth", "converting VK access token to GazAutoClub access token...")
                Fuel.post("https://gazautoclub.herokuapp.com/auth/convert-token/",
                        listOf( "client_id" to getString(R.string.client_id),
                                "client_secret" to getString(R.string.client_secret),
                                "grant_type" to "convert_token",
                                "backend" to "vk-oauth2",
                                "token" to res.accessToken)
                ).response { _, response, result ->
                    run {
                        var parsed = parser.parse(StringBuilder(String(response.data))) as JsonObject
                        val refreshToken = parsed.string(REFRESH_TOKEN)
                        val accessToken = parsed.string(ACCESS_TOKEN)
                        Client.registerClientWithAccessToken(accessToken!!)
                        Log.d("API/Auth", parsed.toJsonString(true))
                        when (result) {
                            is Result.Success -> {
                                Log.d("API/Auth", "Success.")
                                Log.d("API/Auth", "getting VK user profile pic...")
                                val request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_400_orig"))
                                request.executeWithListener(object : VKRequest.VKRequestListener() {
                                    override fun onComplete(response: VKResponse?) {
                                        val user = (response!!.parsedModel as VKList<VKApiUser>)[0]
                                        val urlImage = user.photo_400_orig
                                        Log.d("VK/API/PhotoUrl", urlImage)

                                        Log.d("API/Auth", "obtaining user's id in GazAutoClub system...")
                                        Fuel.get("https://gazautoclub.herokuapp.com/accounts/")
                                                .header("Authorization" to ("Bearer " + accessToken))
                                                .response { request1, response1, result1 ->
                                                    Log.d("API/Auth", request1.toString())
                                                    val (bytes, error) = result1
                                                    when (result1) {
                                                        is Result.Success -> {
                                                            parsed = parser.parse(StringBuilder(String(response1.data))) as JsonObject
                                                            Log.d("API/Auth", parsed.toJsonString(true))
                                                            val id = parsed.array<JsonObject>("results")
                                                                    ?.get(0)?.get("id") as Int

                                                            Log.d("API/Auth", "updating user profile pic " +
                                                                    "in GazAutoClub system...")
                                                            Fuel.put("https://gazautoclub.herokuapp.com/profiles/$id/",
                                                                    listOf("photo" to urlImage,
                                                                            "user" to id))
                                                                    .header("Authorization" to ("Bearer " + accessToken))
                                                                    .response { request2, response2, result2 ->
                                                                        Log.d("API/Auth", request2.toString())
                                                                        Log.d("API/Auth", response2.toString())
                                                                        val (inBytes, inError) = result
                                                                        when (result2) {
                                                                            is Result.Success -> {
                                                                                Log.d("API/Auth", "Success")
                                                                            }
                                                                            is Result.Failure -> {
                                                                                if (inBytes != null) {
                                                                                    Log.e("API/Auth", inBytes.toString())
                                                                                }
                                                                                if (inError != null) {
                                                                                    Log.e("API/Auth", inError.toString())
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                            Log.d("API/Auth", "updating VK user's email in " +
                                                                    "GazAutoClub system...")
                                                            Fuel.put("https://gazautoclub.herokuapp.com/accounts/$id/",
                                                                    listOf("email" to res.email))
                                                                    .header("Authorization" to ("Bearer " + accessToken))
                                                                    .response { request2, response2, result2 ->
                                                                        Log.d("API/Auth", request2.toString())
                                                                        Log.d("API/Auth", response2.toString())
                                                                        val (inBytes, inError) = result
                                                                        when (result2) {
                                                                            is Result.Success -> {
                                                                                Log.d("API/Auth", "Success")
                                                                            }
                                                                            is Result.Failure -> {
                                                                                if (inBytes != null) {
                                                                                    Log.e("API/Auth", inBytes.toString())
                                                                                }
                                                                                if (inError != null) {
                                                                                    Log.e("API/Auth", inError.toString())
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                        }
                                                        is Result.Failure -> {
                                                            if (bytes != null) {
                                                                Log.e("API/Auth", bytes.toString())
                                                            }
                                                            if (error != null) {
                                                                Log.e("API/Auth", error.toString())
                                                            }
                                                        }
                                                    }
                                                }
                                    }

                                    override fun onError(error: VKError?) {}
                                    override fun attemptFailed(request: VKRequest?, attemptNumber: Int, totalAttempts: Int) {}
                                })

                                loginDialog.dismiss()
                                LocalPreferences.addString(REFRESH_TOKEN, refreshToken)
                                finishAndStartMainActivity(accessToken)
                            }
                            is Result.Failure -> {
                                Log.e("API/Auth", "Failure: " + result.error.toString())
                            }
                        }
                    }
                }
            }

            override fun onError(error: VKError) {
                Log.e("VK/Auth", "Error: " + error.toString())
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun finishAndStartMainActivity(accessToken: String) {
        startActivity(Intent(App.instance, MainActivity::class.java).apply {
            putExtra(ACCESS_TOKEN, accessToken)
        })
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }

    override fun onBackPressed() {
        when(statement) {
            REGISTER_STATE, RESTORE_STATE -> {
                changeUi(LOGIN_STATE)
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}