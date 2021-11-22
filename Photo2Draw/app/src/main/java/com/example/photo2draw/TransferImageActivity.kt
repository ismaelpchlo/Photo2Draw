package com.example.photo2draw

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
import android.os.Bundle
import com.example.photo2draw.databinding.ActivityTransferImageBinding


class TransferImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferImageBinding
    private lateinit var uriImage: Uri
    private lateinit var imageReal: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var responseBitmap: Bitmap
    private var heightAndWidth: Int = 360

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        imageReal = intent.getParcelableExtra("Image")!!
        imageView = findViewById(R.id.imageReal)
        imageReal = MediaStore.Images.Media.getBitmap(this.contentResolver, uriImage)
        imageReal = Bitmap.createScaledBitmap(imageReal, heightAndWidth, heightAndWidth, true)
        imageView.setImageBitmap(imageReal)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}