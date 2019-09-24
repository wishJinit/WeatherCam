package com.yujin.weathercam

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import com.yujin.weathercam.Util.Log
import com.yujin.weathercam.VO.ImageVO
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File
import java.io.FileInputStream

class GalleryActivity : AppCompatActivity() {
    val TAG = "GalleryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
    }

    override fun onStart() {
        super.onStart()
        updateGalleryView()
    }

    override fun onStop() {
        super.onStop()
        hideProgress()
    }

    fun updateGalleryView() {
        showProgress()
        tableLayout.removeAllViews()

        val galleryPath = "${Environment.getExternalStorageDirectory().absolutePath}/${getString(R.string.app_name)}"
        val galleryDir = File(galleryPath)

        if (galleryDir.exists()) {
            var imageCnt = 0
            val imageList = arrayOf<ImageVO>()

            var tableRow = TableRow(baseContext)
            galleryDir.listFiles().forEachIndexed { index, file ->
                if (file.exists() && file.isFile) {
                    val imageView = ImageView(baseContext)
                    val imageViewLayoutParams = TableRow.LayoutParams(200, 200)
                    imageViewLayoutParams.setMargins(10, 5, 10, 5)
                    imageView.layoutParams = imageViewLayoutParams
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    val inputStream = FileInputStream(file)
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 2
                    val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                    imageView.setImageBitmap(bitmap)
                    tableRow.addView(imageView)

                    if (index % 3 == 2) {
                        tableLayout.addView(tableRow)
                        tableRow = TableRow(baseContext)
                    }

                }
            }
        } else {
            Toast.makeText(baseContext, "갤러리에 접근할 수 없습니다.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Gallery directory not found")
        }
        hideProgress()
    }

    fun showProgress() {
        progressbar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progressbar.visibility = View.INVISIBLE
    }
}