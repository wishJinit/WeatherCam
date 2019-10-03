package com.yujin.weathercam

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File
import java.io.FileInputStream

class ImageActivity : AppCompatActivity() {
    var imageFile:File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val imagePath = intent.getStringExtra("imagePath")
        imagePath?.let {
            imageFile = File(it)

            if (imageFile!!.exists()) {
                setImageView(imageFile!!)
            }
        }

    fun setImageView(file: File){
        val inputStream = FileInputStream(file)
        val options = BitmapFactory.Options()
        options.inSampleSize = 2
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        imageView.setImageBitmap(bitmap)
    }
    }
}
