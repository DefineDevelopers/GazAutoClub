package net.webdefine.gazautoclub.activities

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
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.beust.klaxon.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.mcxiaoke.koi.ext.fileName
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.activity_new_note.*
import kotlinx.coroutines.experimental.launch
import net.webdefine.gazautoclub.Android
import net.webdefine.gazautoclub.App
import net.webdefine.gazautoclub.R
import net.webdefine.gazautoclub.database.Client
import net.webdefine.gazautoclub.model.Brand
import net.webdefine.gazautoclub.model.Car
import okhttp3.*
import org.jetbrains.anko.custom.async
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class NewNoteActivity : AppCompatActivity(), Callback {
    private val PICK_IMAGE_REQUEST = 1
    private val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4
    private val PERMISSIONS_MULTIPLE_REQUEST = 123

    private          val ACCESS_TOKEN  = "access_token"
    private          var brands = ArrayList<Brand>()
    private          val BRANDS_EXTRA = "BRANDS_LIST"

    private          var cars = ArrayList<Car>()

    private lateinit var accessToken: String
    private          var userId: Int = -1

    private          var carId: Int = -1
    private          var tmpCar: Car? = null

    private          var coverImageFile: File? = null

    private lateinit var bm: Bitmap
    private lateinit var progressDialog: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        setSupportActionBar(new_post_toolbar)
        // TODO to resources
        supportActionBar!!.title = "Добавить запись"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        cleanTmpFile()

        accessToken = intent.extras.getString(ACCESS_TOKEN)
        getIdFromServerByToken(accessToken)

        brands = intent.extras.getSerializable(BRANDS_EXTRA) as ArrayList<Brand>

        progressDialog = MaterialDialog.Builder(this)
                // TODO to resources
                .title("Пожалуйста, подождите")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .build()

        new_note_brand_selector.onClick {
            MaterialDialog.Builder(this)
                    .title("Выберите фирму автомобиля")
                    .items(brands.map { brand -> brand.name })
                    .itemsCallbackSingleChoice(-1) { _, _, which, _ ->
                        loadCarsOfBrand(brands[which].id)
                        new_note_brand_selector.text = brands[which].name
                        tmpCar = null
                        carId = -1
                        Log.d("NewNote", "Выбрана фирма, carId = " + carId)
                        true
                    }
                    .show()
        }

        new_note_model_selector.onClick {
            MaterialDialog.Builder(this)
                    .title("Выберите модель автомобиля")
                    .items(cars.map { car -> car.name })
                    .itemsCallbackSingleChoice(-1) { _, _, which, _ ->
                        tmpCar = cars[which]
                        carId = tmpCar?.id ?: -1
                        new_note_model_selector.text = cars[which].name
                        Log.d("NewNote", "Выбрана модель, tmpCar.id = " + tmpCar!!.id)
                        Log.d("NewNote", "Выбрана модель, carId = " + carId)
                        true
                    }
                    .show()
        }

        new_note_upload_image_box.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                prepareToOpenGalleryIntent()
            }
            else {
                openGalleryIntent(PICK_IMAGE_REQUEST)
            }
        }

        new_note_post_button.onClick {
            it.isEnabled = false
            if (!validateSelectors()) {
                it.isEnabled = true
                return@onClick
            }

            if (!validateTextInPost()) {
                it.isEnabled = true
                return@onClick
            }

            if (!progressDialog.isShowing)
                progressDialog.show()

            val title = new_note_title.text.toString()
            val body = new_note_content.text.toString()

            // Initial value for photo url in case when user didn't provided their own image
            // default car image should be used as a topic image URL
            var photo = tmpCar?.photoUrl ?: ""
            val fileTooLargeDialog = MaterialDialog.Builder(this)
                    .title("Ошибка")
                    .content("Файл слишком большой, пожалуйста, выберите другой. Максимальный размер файла: 5 МБ")
                    .positiveText("Хорошо")
                    .onPositive({
                        _, _ -> prepareToOpenGalleryIntent()
                    })
            launch(Android) {
                if (hasUserUploadedOwnPhoto()) {
                    // user provided their own image
                    // need to upload it to server and set topic image to returned URL
                        try {
                            val result = Client.uploadImageFileOnServer(coverImageFile!!, coverImageFile!!.path.fileName())
                            val response = result.await()
                            if (response != "File is too large") {
                                photo = response
                            }
                            else {
                                fileTooLargeDialog.show()
                            }
                            Log.d("Coroutine", photo)
                        } catch (e: IOException) {
                            Log.e("IOException", "Internet connection problem or something")
                        }
                }
                // run post query and change activities
                try {
                    val result = Client.createNote(title, body, photo, tmpCar?.id ?: carId)
                    val parsed = result.await()
                    println(parsed.toJsonString(true))
                    if (parsed.containsKey("id")) {
                        // everything is fine. Returning back
                        if (progressDialog.isShowing)
                            progressDialog.dismiss()
                        it.isEnabled = true
                        onBackPressed()
                    }
                } catch (e: IOException) {
                    Log.e("IOException", "Internet connection problem or something")
                }
                it.isEnabled = true
            }
        }
    }

    private fun cleanTmpFile() {
        coverImageFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/" + "tmp.jpg")
        if (coverImageFile != null && coverImageFile!!.exists()) {
            coverImageFile!!.delete()
            coverImageFile = null
            Log.d("NewNoteActivity", "Old file deleted")
        }
    }

    private fun hasUserUploadedOwnPhoto(): Boolean {
        val testFile = File(new_note_image_path.text.toString())
        return testFile.exists()
    }

    private fun validateTextInPost(): Boolean {
        if (new_note_title.length() >= 100 || new_note_title.length() < 10) {
            MaterialDialog.Builder(this)
                    //TODO to resource
                    .title("Неправильная длина заголовка")
                    //TODO to resource
                    .content("Текст заголовка должен содержать от 10 до 100 символов включительно.")
                    //TODO to resource
                    .positiveText("Понятно")
                    .show()
            return false
        }
        if (new_note_content.length() >= 10000 || new_note_content.length() <= 10) {
            MaterialDialog.Builder(this)
                    //TODO to resource
                    .title("Неправильная длина содержания")
                    //TODO to resource
                    .content("Текст содержания должен содержать от 10 до 10000 символов включительно.")
                    //TODO to resource
                    .positiveText("Понятно")
                    .show()
            return false
        }
        return true
    }

    private fun validateSelectors(): Boolean {
        if (new_note_brand_selector.text == "Выбрать фирму") {
            MaterialDialog.Builder(this)
                    //TODO to resource
                    .title("Сначала выберите фирму")
                    //TODO to resource
                    .content("Необходимо выбрать фирму и модель автомобиля")
                    //TODO to resource
                    .positiveText("Понятно")
                    .show()
            return false
        }
        if (carId == -1) {
            MaterialDialog.Builder(this)
                    //TODO to resource
                    .title("Сначала выберите модель автомобиля")
                    //TODO to resource
                    .content("Необходимо выбрать фирму и модель автомобиля")
                    //TODO to resource
                    .positiveText("Понятно")
                    .show()
            return false
        }
        return true
    }

    private fun updateModelList(carsList: ArrayList<Car>) {
        cars = carsList
        new_note_model_selector.isEnabled = cars.size != 0
        new_note_model_selector.text = when (cars.size) {
        //TODO to resource
            0 -> "Нет автомобилей этой фирмы"
            1 -> cars[0].name
        //TODO to resource
            else -> "Выбрать модель"
        }
        if (cars.size == 1) {
            tmpCar = cars[0]
            carId = cars[0].id
        }
    }

    private fun loadCarsOfBrand(brandId: Int) {
        if (!progressDialog.isShowing)
            progressDialog.show()
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("https://gazautoclub.herokuapp.com/models/?brand=$brandId")
                .build()

        val getBrandCarsRequestCallback = GetBrandCarsRequestCallback(brandId, cars)

        async {
            client.newCall(request).enqueue(getBrandCarsRequestCallback)
        }
    }

    private fun changeModelSelectorVisibility(state: Int) {
        if (state == View.VISIBLE) {
            new_note_model_selector.visibility = View.VISIBLE
        }
        else if (state == View.GONE) {
            new_note_model_selector.visibility = View.GONE
        }
    }

    inner class GetBrandCarsRequestCallback(private val brandId: Int, private var cars: ArrayList<Car>) : Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            onFailure(null, e)
        }

        override fun onResponse(call: Call?, response: Response?) {
            Log.d("MainActivity", response.toString())
            if (!response!!.isSuccessful) {
                onFailure(call, null)
                return
            }
            val parser = Parser()
            val responseData = response.body()?.string()
            val parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
            Log.d("NewNoteActivity", parsed.toJsonString(true))
            val count = parsed.int("count") ?: 0

            cars = ArrayList(count)
            var i = 0
            while (i < count) {
                val id = parsed.array<JsonObject>("results")?.get(i)?.get("id") as Int
                val name = parsed.array<JsonObject>("results")?.get(i)?.get("name") as String
                val photoUrl = parsed.array<JsonObject>("results")?.get(i)?.get("image") as String?
                cars.add(Car(id, brandId, name, photoUrl))
                i++
            }

            if (cars.size == count) {
                Log.d("MainActivity", "Everything seems fine, load NewNoteIntent")
                runOnUiThread {

                    if (progressDialog.isShowing)
                        progressDialog.dismiss()
                    changeModelSelectorVisibility(View.VISIBLE)
                    updateModelList(cars)
                }
            }
        }
    }

    private fun getIdFromServerByToken(accessToken: String) {
        val parser = Parser()
        Fuel.get("https://gazautoclub.herokuapp.com/accounts/")
                .header("Authorization" to ("Bearer " + accessToken))
                .response { request, response, result ->
                    Log.d("API/Auth", request.toString())
                    val (bytes, error) = result
                    when (result) {
                        is Result.Success -> {
                            val parsed = parser.parse(StringBuilder(String(response.data))) as JsonObject
                            Log.d("API/Auth", parsed.toJsonString(true))
                            userId = parsed.array<JsonObject>("results")?.get(0)?.get("id") as Int
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

    private fun prepareToOpenGalleryIntent() {
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously*
                MaterialDialog.Builder(this)
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
                ActivityCompat.requestPermissions(this,
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

    private fun saveBitmap(bitmap: Bitmap, filename: String): File {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(root, "/" + filename)
        val outputStream: FileOutputStream? = FileOutputStream(file.absoluteFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream?.close()

        return file
    }

    override fun onFailure(call: Call?, e: IOException?) {
        onFailure(null, e)
    }

    override fun onResponse(call: Call?, response: Response?) {
        Log.d("Settings", response.toString())
        if (!response!!.isSuccessful) {
            onFailure(call, null)
            return
        }
        val parser = Parser()
        val responseData = response.body()?.string()
        var parsed = parser.parse(StringBuilder(String(responseData!!.toByteArray()))) as JsonObject
        val url = parsed.string("url") as String

        Fuel.put("https://gazautoclub.herokuapp.com/profiles/$userId/",
                listOf("user" to userId,
                        "photo" to url))
                .header("Authorization" to ("Bearer " + accessToken))
                .response { request, response1, result ->
                    Log.d("Settings", request.toString())
                    Log.d("Settings", response1.toString())
                    val (bytes, error) = result
                    when (result) {
                        is Result.Success -> {
                            parsed = parser.parse(StringBuilder(String(response1.data))) as JsonObject
                            Log.d("Settings", parsed.toJsonString(true))
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()
                        }
                        is Result.Failure -> {
                            if (bytes != null) {
                                Log.e("Settings", bytes.toString())
                            }
                            if (error != null) {
                                Log.e("Settings", error.toString())
                            }
                        }
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {
            if (!progressDialog.isShowing)
                progressDialog.show()

            val uri = data.data
            val inputStream = contentResolver.openInputStream(uri)
            bm = BitmapFactory.decodeStream(inputStream)
            //bm = Bitmap.createBitmap(bm, 0, 0, 800, 400)

            coverImageFile = saveBitmap(bm, "tmp.jpg")
            if (progressDialog.isShowing)
                progressDialog.dismiss()
            new_note_image_path.text = coverImageFile?.path
        }
    }

    override fun onBackPressed() {
        startActivity(
                Intent(App.instance, MainActivity::class.java)
                        .putExtra(PostDetailsActivity.ACCESS_TOKEN, accessToken)
        )
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }
}
