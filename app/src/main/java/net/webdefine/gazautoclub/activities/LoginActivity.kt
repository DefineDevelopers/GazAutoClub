package net.webdefine.gazautoclub.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var emailText              : EditText
    private lateinit var passwordText           : EditText
    private lateinit var fullNameText           : EditText
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

    private val ZERO_INT_CONST      = 0
    private val ONE_INT_CONST       = 1
    private val TWO_INT_CONST       = 2
    private val PASSWORD_PATTERN    = "((?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,})"
    private val FULLNAME_PATTERN    = "^[\\p{L} .'-]+$"
    private val INTENT_USER_ID      = "user_id"

    private var errorAtLogin        = false
    private var errorAtRegister     = false
    private var statement           = ONE_INT_CONST


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailText                   = input_email
        passwordText                = input_password
        fullNameText                = input_fullname
        passwordTextLayout          = password_text_layout
        fullNameTextLayout          = fullname_text_layout
        emailTextLayout             = email_text_layout
        loginButton                 = login
        registerButton              = register
        loginNewButton              = login_new
        registerNewButton           = register_new
        googleAuthButton            = google_si
        vkAuthButton                = vk_si
        anonymousButton             = anonymous
        forgotPassButton            = forgot_password
        restorePasswordButton       = restore_new
        logoText                    = logo_text
        orText                      = or_text
        orFrameLayout               = or_frame_layout

        fullNameLayoutParams        = fullNameTextLayout.layoutParams    as ConstraintLayout.LayoutParams
        emailLayoutParams           = emailTextLayout.layoutParams       as ConstraintLayout.LayoutParams
        passwordLayoutParams        = passwordTextLayout.layoutParams    as ConstraintLayout.LayoutParams
        forgotPassButtonParams      = restorePasswordButton.layoutParams as ConstraintLayout.LayoutParams

        val elleyFont: Typeface? = Typeface.createFromAsset(assets,"fonts/Elley.otf")
        logoText.typeface = elleyFont

        googleAuthButton.onClick {
            startActivity(Intent(App.instance, MainActivity::class.java).apply {
                putExtra(INTENT_USER_ID, 1)
            })
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
            finish()
        }

        vkAuthButton.onClick {
            startActivity(Intent(App.instance, MainActivity::class.java).apply {
                putExtra(INTENT_USER_ID, 2)
            })
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
            finish()
        }

        anonymousButton.onClick {
            startActivity(Intent(App.instance, MainActivity::class.java).apply {
                putExtra(INTENT_USER_ID, 3)
            })
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
            finish()
        }

        registerNewButton.onClick {
            if (!registerValidate()) {
                errorAtRegister  = true

                return@onClick
            }

            val registerDialog   = indeterminateProgressDialog(getString(R.string.progress_dialog_register))
            registerDialog.show()

            val registerSnackbar = longSnackbar(coordinator_layout_content, getString(R.string.error_incorrect_email))
            registerSnackbar.show()
            registerDialog.dismiss()
        }

        loginButton.onClick {
            if (!loginValidate()) {
                errorAtLogin     = true

                return@onClick
            }

            val loginDialog = indeterminateProgressDialog(getString(R.string.progress_dialog_auth))
            loginDialog.show()

            val loginSnackbar = longSnackbar(coordinator_layout_content, getString(R.string.error_not_match_fields))
            loginSnackbar.show()
            loginDialog.dismiss()
        }

        registerButton.onClick {
            changeUi(ZERO_INT_CONST)
        }

        loginNewButton.onClick {
            changeUi(ONE_INT_CONST)
        }

        forgotPassButton.onClick {
            if (passwordTextLayout.visibility == View.VISIBLE) {
                changeUi(TWO_INT_CONST)
            }
            else {
                changeUi(ONE_INT_CONST)
            }
        }

        restorePasswordButton.onClick {
            val email = emailText.text.toString()

            if (!email.isEmpty()) {
                alert(getString(R.string.pass_restore_description), getString(R.string.pass_restore)) {
                    positiveButton(getString(R.string.ok)) { it.dismiss() }
                }.show()
            }
            else {
                emailTextLayout.error = getString(R.string.error_field_required)
            }
        }
    }

    private fun changeUi(task: Int) {
        when(task) {
            ZERO_INT_CONST -> {
                statement = ZERO_INT_CONST

                orFrameLayout.visibility            = View.GONE
                googleAuthButton.visibility         = View.GONE
                vkAuthButton.visibility             = View.GONE
                anonymousButton.visibility          = View.GONE
                registerButton.visibility           = View.GONE
                loginButton.visibility              = View.GONE
                forgotPassButton.visibility         = View.GONE

                registerNewButton.visibility        = View.VISIBLE
                loginNewButton.visibility           = View.VISIBLE
                fullNameTextLayout.visibility       = View.VISIBLE
            }
            ONE_INT_CONST -> {
                statement = ONE_INT_CONST

                val constraintSet = ConstraintSet()
                constraintSet.clone(container)
                constraintSet.connect(forgot_password.id, ConstraintSet.TOP, register.id, ConstraintSet.BOTTOM)
                forgotPassButton.text               = getString(R.string.forgot_your_password)
                constraintSet.applyTo(container)

                fullNameTextLayout.visibility       = View.GONE
                registerNewButton.visibility        = View.GONE
                loginNewButton.visibility           = View.GONE
                restorePasswordButton.visibility    = View.GONE

                orFrameLayout.visibility            = View.VISIBLE
                googleAuthButton.visibility         = View.VISIBLE
                vkAuthButton.visibility             = View.VISIBLE
                anonymousButton.visibility          = View.VISIBLE
                registerButton.visibility           = View.VISIBLE
                loginButton.visibility              = View.VISIBLE
                forgotPassButton.visibility         = View.VISIBLE
                passwordTextLayout.visibility       = View.VISIBLE
            }
            TWO_INT_CONST -> {
                statement = TWO_INT_CONST

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
        emailTextLayout.error = null
        passwordTextLayout.error = null
    }

    private fun loginValidate(): Boolean {
        val email = emailText.text.toString()
        val password = passwordText.text.toString()

        if (email.isEmpty()) {
            emailText.error = getString(R.string.error_field_required)
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
            emailText.error = null
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
                emailText.error = getString(R.string.error_field_required)
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
                emailText.error = null
            }

            return false
        }
        else {
            fullNameText.error = null
            if (email.isEmpty()) {
                emailText.error = getString(R.string.error_field_required)
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
                emailText.error = null
                emailText.error = getString(R.string.error_field_required)

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
                emailText.error = getString(R.string.error_incorrect_email)
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
                emailText.error = null
            }
        }
        else {
            fullNameText.error = getString(R.string.error_unavailable_fullname)
            return false
        }

        return true
    }

    override fun onBackPressed() {
        when(statement) {
            ZERO_INT_CONST, TWO_INT_CONST -> {
                changeUi(ONE_INT_CONST)
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}