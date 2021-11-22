package com.example.photo2draw

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.photo2draw.databinding.ActivityChooseStyleBinding
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.provider.MediaStore
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import android.graphics.BitmapFactory
import java.io.IOException
import java.lang.Exception
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Button
import java.util.concurrent.TimeUnit
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File


class ChooseStyle : AppCompatActivity() {
    private lateinit var binding: ActivityChooseStyleBinding
    private lateinit var uriImage: Uri
    private lateinit var imageReal: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var responseBitmap: Bitmap
    private lateinit var monetButton: Button
    private lateinit var cezanneButton: Button
    private lateinit var ukiyoeButton: Button
    private lateinit var vangoghButton: Button
    private lateinit var revertButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var saveButton: ImageButton

    private var heightAndWidth: Int = 360
    private var styleMonet = "style_monet"
    private var styleCezanne = "style_cezanne"
    private var styleUkiyoe = "style_ukiyoe"
    private var styleVangogh = "style_vangogh"

    private var removeUri = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                var uri: Uri = data.getParcelableExtra("Image")!!
                val file = File(uri.path)
                file.delete()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChooseStyleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val policy = ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)

        uriImage = intent.getParcelableExtra("Image")!!

        imageView = findViewById(R.id.imageReal)
        imageReal = MediaStore.Images.Media.getBitmap(this.contentResolver, uriImage)
        imageReal = Bitmap.createScaledBitmap(imageReal, heightAndWidth, heightAndWidth, true)
        imageView.setImageBitmap(imageReal)

        monetButton = findViewById(R.id.style_monet)
        cezanneButton = findViewById(R.id.style_cezanne)
        ukiyoeButton = findViewById(R.id.style_ukiyoe)
        vangoghButton = findViewById(R.id.style_vangogh)
        revertButton = findViewById(R.id.revert)
        shareButton = findViewById(R.id.shareButton)
        saveButton = findViewById(R.id.saveButton)

        showButtons(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun chooseStyleMonet(v: View){
        sendImageToServer(styleMonet)
    }

    fun chooseStyleCezanne(v: View){
        sendImageToServer(styleCezanne)
    }

    fun chooseStyleUkiyoe(v: View){
        sendImageToServer(styleUkiyoe)
    }

    fun chooseStyleVanGogh(v: View){
        sendImageToServer(styleVangogh)
    }

    fun shareImage(v: View){
        var uriResponse = saveImage(this, responseBitmap, "image_saved.jpg")
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uriResponse)
            type = "image/jpeg"
        }

        var intent = Intent(Intent.createChooser(shareIntent, "Share image"))
        removeUri.launch(intent)
    }

    private fun saveImage(inContext: Context, inImage: Bitmap, name: String): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            name,
            null
        )
        return Uri.parse(path)
    }

    fun revert(v: View){
        imageView.setImageBitmap(imageReal)
        showButtons(false)
    }

    fun save(v: View){
        saveImage(this, responseBitmap,"image_saved.jpg")
        Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show()
    }

    private fun sendImageToServer(style: String){
        val stream = ByteArrayOutputStream()
        imageReal.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        val byteArray = stream.toByteArray()

        val multipartBodyBuilder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        multipartBodyBuilder.addFormDataPart("image" , "Android_image.jpg", RequestBody.create(
            "image/*jpg".toMediaTypeOrNull(),
            byteArray
        ));
        multipartBodyBuilder.addFormDataPart("style",style);
        val postBody: RequestBody = multipartBodyBuilder.build()
        postRequest(postBody)
    }

    private fun postRequest(postBody: RequestBody){
        enableButtons(false)
        val builder = OkHttpClient().newBuilder()
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40,TimeUnit.SECONDS)
            .connectTimeout(40,TimeUnit.SECONDS)
            .callTimeout(40,TimeUnit.SECONDS)
        val client = builder.build()

        val request: Request = Request.Builder()
            .url("https://flaskserver-gjcbyup5ka-uc.a.run.app")
            .post(postBody)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Cancel the post on failure.
                call.cancel()

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread {
                    enableButtons(true)
                    showButtons(false)
                    imageView.setImageBitmap(imageReal)
                    e.printStackTrace()
                    Toast.makeText(this@ChooseStyle, "Style transfer failed", Toast.LENGTH_LONG).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread {
                    try {
                        responseBitmap = BitmapFactory.decodeStream(response.body!!.byteStream())
                        imageView.setImageBitmap(responseBitmap)
                        enableButtons(true)
                        showButtons(true)
                        response.body!!.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun showButtons(show: Boolean){
        if (show){
            revertButton.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            shareButton.visibility = View.VISIBLE
        }else{
            revertButton.visibility = View.GONE
            saveButton.visibility = View.GONE
            shareButton.visibility = View.GONE
        }
    }

    private fun enableButtons(enable: Boolean){
        monetButton.isEnabled = enable
        cezanneButton.isEnabled = enable
        ukiyoeButton.isEnabled = enable
        vangoghButton.isEnabled = enable
    }
}

