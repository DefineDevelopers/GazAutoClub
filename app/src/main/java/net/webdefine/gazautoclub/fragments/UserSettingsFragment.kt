package net.webdefine.gazautoclub.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.mcxiaoke.koi.ext.fileName
import com.mcxiaoke.koi.ext.onClick
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.database.Client
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

class UserSettingsFragment : Fragment()  {
    private lateinit var userPhotoImageView: ImageView
    private lateinit var accessToken: String
    private          var coverImageFile: File? = null
    private lateinit var bm: Bitmap
    private lateinit var progressDialog: MaterialDialog

    companion object {
        private const val ACCESS_TOKEN  = "access_token"
        private       var userId = -1
        private val PICK_IMAGE_REQUEST = 1
        private val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4
        private val PERMISSIONS_MULTIPLE_REQUEST = 123

        private val NAME_REGEX = "^(?i)(?:[а-яa-z]+(?: |\\. ?)?)+[а-яa-z]\$"
        private val EMAIL_REGEX = "^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))\$"
        private val USERNAME_REGEX = "^(?=.*[a-zA-Z]{1,})(?=.*[\\d]{0,})[a-zA-Z0-9]{1,15}\$"
        private val PASSWORD_REGEX = "^((?=\\S*?[A-Z])(?=\\S*?[a-z])(?=\\S*?[0-9]).{6,})\\S\$"

        fun newInstance(accessToken: String): UserSettingsFragment {
            val args = Bundle()
            args.putSerializable(ACCESS_TOKEN, accessToken)
            val fragment = UserSettingsFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View =  inflater.inflate(R.layout.fragment_users_settings, container,
                false)

        userPhotoImageView = view.findViewById(R.id.settings_user_image)

        val uploadImageButton = view.findViewById<Button>(R.id.settings_upload_image_button)
        val usernameButton = view.findViewById<Button>(R.id.usernameSettingsText)
        val emailButton = view.findViewById<Button>(R.id.emailSettingsText)
        val firstNameButton = view.findViewById<Button>(R.id.firstNameSettingsText)
        val secondNameButton = view.findViewById<Button>(R.id.secondNameSettingsText)
        val passwordButton = view.findViewById<Button>(R.id.passwordSettingsText)

        accessToken  = arguments!!.getSerializable(ACCESS_TOKEN).toString()

        progressDialog = MaterialDialog.Builder(activity!!)
                // TODO to resources
                .title("Пожалуйста, подождите")
                .content("Применяем внесённые изменения...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .build()

        launch(Android) {
            val response = Client.getUserInfo()
            val user = response.await()

            userId = user.id
            //TODO to resource
            usernameButton.text     = if (user.username   == "") "Указать логин"   else user.username
            //TODO to resource
            emailButton.text        = if (user.email      == "") "Указать email"   else user.email
            //TODO to resource
            firstNameButton.text    = if (user.firstName  == "") "Указать имя"     else user.firstName
            //TODO to resource
            secondNameButton.text   = if (user.lastName   == "") "Указать фамилию" else user.lastName

            if (user.photo != null) {
                Log.d("Settings/PhotoURL", user.photo)
                Picasso.with(this@UserSettingsFragment.activity).load(user.photo).into(userPhotoImageView)
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    userPhotoImageView.setImageDrawable(App.instance.getDrawable(R
                            .drawable.ic_account_box_white_128dp))
                }
                else {
                    userPhotoImageView.setImageDrawable(resources.getDrawable(
                            R.drawable.ic_account_box_white_128dp)
                    )
                }
                Log.e("Setting/PhotoURL", "Not Provided!")
            }
        }

        usernameButton.onClick {
            activity?.let { it1->
                MaterialDialog.Builder(it1)
                        //TODO to resource
                        .title("Изменение логина")
                        //TODO to resource
                        .content("Укажите новый логин")
                        .inputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                        //TODO to resource
                        .input("Логин", usernameButton.text, { dialog, input ->
                            if (validate(USERNAME_REGEX, input.toString())) {
                                if (!progressDialog.isShowing)
                                    progressDialog.show()
                                launch(Android) {
                                    val response = Client.updateUserInfo(userId, "username", input.toString())
                                    val data = response.await()
                                    if (data.isSuccessful) {
                                        if (progressDialog.isShowing)
                                            progressDialog.dismiss()
                                        usernameButton.text = input
                                    }
                                    else {
                                        Log.d("Settings/Error", data.body().toString())
                                    }
                                }
                                dialog.dismiss()
                            }
                            else {
                                dialog.dismiss()
                                activity?.let { it2 ->
                                    MaterialDialog.Builder(it2)
                                            //TODO to resource
                                            .title("Ошибка формата")
                                            //TODO to resource
                                            .content("Вы ввели неверный логин")
                                            //TODO to resource
                                            .positiveText("Хорошо")
                                            .show()
                                }
                            }
                        }).show()
            }
        }

        emailButton.onClick {
            activity?.let { it1->
                MaterialDialog.Builder(it1)
                        //TODO to resource
                        .title("Изменение Email")
                        //TODO to resource
                        .content("Укажите новый email.")
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input("Email", emailButton.text, { dialog, input ->
                            if (validate(EMAIL_REGEX, input.toString())) {
                                if (!progressDialog.isShowing)
                                    progressDialog.show()
                                launch(Android) {
                                    val response = Client.updateUserInfo(userId, "email", input.toString())
                                    val data = response.await()
                                    if (data.isSuccessful) {
                                        if (progressDialog.isShowing)
                                            progressDialog.dismiss()
                                        emailButton.text = input
                                    }
                                    else {
                                        Log.d("Settings/Error", data.body().toString())
                                    }
                                }
                                dialog.dismiss()
                           } else {
                               dialog.dismiss()
                               activity?.let { it2 ->
                                   MaterialDialog.Builder(it2)
                                           //TODO to resource
                                           .title("Ошибка формата")
                                           //TODO to resource
                                           .content("Вы ввели неверный email")
                                           //TODO to resource
                                           .positiveText("Хорошо")
                                           .show()
                               }
                           }
                        }).show()
            }
        }

        firstNameButton.onClick {
            activity?.let { it1->
                MaterialDialog.Builder(it1)
                        //TODO to resource
                        .title("Изменение имени")
                        //TODO to resource
                        .content("Укажите ваше имя")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        //TODO to resource
                        .input("Имя", firstNameButton.text, { dialog, input ->
                            Log.d("First Name", input.toString())
                            if (validate(NAME_REGEX, input.toString())) {
                                if (!progressDialog.isShowing)
                                    progressDialog.show()
                                launch(Android) {
                                    val response = Client.updateUserInfo(userId, "first_name", input.toString())
                                    val data = response.await()
                                    Log.d("Settings/Data", data.toString())
                                    if (data.isSuccessful) {
                                        if (progressDialog.isShowing)
                                            progressDialog.dismiss()
                                        firstNameButton.text = input
                                    }
                                    else {
                                        activity?.let { it2 ->
                                            MaterialDialog.Builder(it2)
                                                    //TODO to resource
                                                    .title("Ошибка")
                                                    //TODO to resource
                                                    .content(data.body()?.string() ?: "")
                                                    //TODO to resource
                                                    .show()
                                        }
                                        Log.d("Settings/Error", data.body()?.string())
                                    }
                                }
                                dialog.dismiss()
                            }
                            else {
                                dialog.dismiss()
                                activity?.let { it2 ->
                                    MaterialDialog.Builder(it2)
                                            //TODO to resource
                                            .title("Ошибка формата")
                                            //TODO to resource
                                            .content("Вы ввели неверное имя")
                                            //TODO to resource
                                            .positiveText("Хорошо")
                                            .show()
                                }
                            }
                        }).show()
            }
        }

        secondNameButton.onClick {
            activity?.let { it1->
                MaterialDialog.Builder(it1)
                        //TODO to resource
                        .title("Изменение фамилии")
                        //TODO to resource
                        .content("Укажите вашу фамилию")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        //TODO to resource
                        .input("Фамилия", secondNameButton.text, { dialog, input ->
                            if (validate(NAME_REGEX, input.toString())) {
                                if (!progressDialog.isShowing)
                                    progressDialog.show()
                                launch(Android) {
                                    val response = Client.updateUserInfo(userId, "last_name", input.toString())
                                    val data = response.await()
                                    if (data.isSuccessful) {
                                        if (progressDialog.isShowing)
                                            progressDialog.dismiss()
                                        secondNameButton.text = input
                                    }
                                    else {
                                        Log.d("Settings/Error", data.body().toString())
                                    }
                                }
                                dialog.dismiss()
                            }
                            else {
                                dialog.dismiss()
                                activity?.let { it2 ->
                                    MaterialDialog.Builder(it2)
                                            //TODO to resource
                                            .title("Ошибка формата")
                                            //TODO to resource
                                            .content("Вы ввели неверную фамилию")
                                            //TODO to resource
                                            .positiveText("Хорошо")
                                            .show()
                                }
                            }
                        }).show()
            }
        }

        passwordButton.onClick {
            activity?.let { it1 ->
                MaterialDialog.Builder(it1)
                        //TODO to resource
                        .title("Изменение пароля")
                        //TODO to resource
                        .content("Укажите новый пароль")
                        .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        //TODO to resource
                        .input("Пароль", "", { dialog, input ->
                            if (validate(PASSWORD_REGEX, input.toString())) {
                                if (!progressDialog.isShowing)
                                    progressDialog.show()
                                launch(Android) {
                                    val response = Client.updateUserInfo(userId, "password", input.toString())
                                    val data = response.await()
                                    if (data.isSuccessful) {
                                        if (progressDialog.isShowing)
                                            progressDialog.dismiss()
                                    }
                                    else {
                                        Log.d("Settings/Error", data.body().toString())
                                    }
                                }
                                dialog.dismiss()
                            }
                            else {
                                dialog.dismiss()
                                activity?.let { it2 ->
                                    MaterialDialog.Builder(it2)
                                            //TODO to resource
                                            .title("Ошибка формата")
                                            //TODO to resource
                                            .content("Пароль должен содержать минимум 6 символов, заглавные и " +
                                                    "прописные буквы и цифры и не содержать пробелов")
                                            //TODO to resource
                                            .positiveText("Хорошо")
                                            .show()
                                }
                            }
                        }).show()
            }
        }

        uploadImageButton.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                prepareToOpenGalleryIntent()
            }
            else {
                openGalleryIntent(PICK_IMAGE_REQUEST)
            }
        }

        return view
    }

    private fun cleanTmpFile() {
        coverImageFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/" + "tmp.jpg")
        if (coverImageFile != null && coverImageFile!!.exists()) {
            coverImageFile!!.delete()
            coverImageFile = null
            Log.d("NewNoteActivity", "Old file deleted")
        }
    }

    private fun prepareToOpenGalleryIntent() {
        if ((ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(activity!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously*
                MaterialDialog.Builder(activity!!)
                        //TODO to resource
                        .title("Предоставьте разрешения")
                        //TODO to resource
                        .content("Пожалуйста, предоставьте разрешение на выбор изображения для обложки темы")
                        //TODO to resource
                        .positiveText("Хорошо")
                        .onPositive({ dialog, which ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        PERMISSIONS_MULTIPLE_REQUEST)
                            }
                        })
                        .show()
            }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                        Array(2, { Manifest.permission.READ_EXTERNAL_STORAGE
                            Manifest.permission.WRITE_EXTERNAL_STORAGE }),
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                openGalleryIntent(PICK_IMAGE_REQUEST)
                return
            }
        }
        else {
            openGalleryIntent(PICK_IMAGE_REQUEST)
        }
    }

    private fun openGalleryIntent(reqCode: Int) {
        cleanTmpFile()
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        //TODO to resource
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), reqCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {
            if (!progressDialog.isShowing)
                progressDialog.show()

            val uri = data.data
            Log.d("Settings/URI/Data", uri.path)
            Log.d("Settings/URI/Data", uri.encodedPath)
            val inputStream = activity!!.contentResolver.openInputStream(uri)
            bm = BitmapFactory.decodeStream(inputStream)

            coverImageFile = saveBitmap(bm, "tmp.jpg")

            Log.d("Settings/SavedFile", (coverImageFile == null).toString())
            Log.d("Settings/SavedFile", coverImageFile?.path)
            Log.d("Settings/SavedFile", coverImageFile?.absolutePath)
            Log.d("Settings/SavedFile", coverImageFile?.canonicalPath)
            val fileTooLargeDialog = MaterialDialog.Builder(activity!!)
                    .title("Ошибка")
                    .content("Файл слишком большой, пожалуйста, выберите другой. Максимальный размер файла: 5 МБ")
                    .positiveText("Хорошо")
                    .onPositive({
                        _, _ -> prepareToOpenGalleryIntent()
                    })

            launch(Android) {
                val resultUrl = Client.uploadImageFileOnServer(coverImageFile!!, coverImageFile!!.path.fileName())
                val url = resultUrl.await()
                Log.d("Coroutine", url)

                if (url == "File is too large") {
                    if (progressDialog.isShowing)
                        progressDialog.dismiss()
                    fileTooLargeDialog.show()
                } else {
                    val result = Client.putProfileImage(userId, url)
                    val response = result.await()
                    Log.d("UserSettings/Response", response.toJsonString(true))
                    userPhotoImageView.setImageBitmap(bm)
                    if (progressDialog.isShowing)
                        progressDialog.dismiss()
                }
            }
        }
    }

    private fun validate(regex: String, input: String): Boolean {
        val wordPattern = Pattern.compile(regex)
        val wordMatcher = wordPattern.matcher(input)
        return wordMatcher.matches() && input.length > 2
    }

    private fun saveBitmap(bitmap: Bitmap, filename: String): File {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(root, "/" + filename)
        Log.d("SaveBitmap/Path", file.path)
        Log.d("SaveBitmap/Path", file.absolutePath)
        Log.d("SaveBitmap/Path", file.canonicalPath)
        val outputStream: FileOutputStream? = FileOutputStream(file.absoluteFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream?.close()
        return file
    }
}