package com.yujin.weathercam

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
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
    var imageList = arrayOf<ImageVO>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        setEventListener()
        updateGalleryView(LoadGalleryImgTask(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.run {
            if (getBooleanExtra("isDeletion", false)) {
                updateGalleryView(LoadGalleryImgTask(this@GalleryActivity))
            }
        }
    }

    fun updateGalleryView(loadGalleryImgTask: LoadGalleryImgTask) {
        showProgress()
        Handler().post {
            tableLayout.removeAllViews()

            val galleryPath = "${Environment.getExternalStorageDirectory().absolutePath}/${getString(R.string.app_name)}"
            val galleryDir = File(galleryPath)

            if (galleryDir.exists()) {
                loadGalleryImgTask.execute(galleryDir)
            } else {
                Toast.makeText(baseContext, "갤러리에 접근할 수 없습니다.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Gallery directory not found")
            }
        }
    }

    fun setEventListener() {
        back_btn.setOnClickListener {
            finish()
        }
    }

    fun showProgress() {
        progressbar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progressbar.visibility = View.INVISIBLE
    }


    class LoadGalleryImgTask(galleryActivity: GalleryActivity) : AsyncTask<File, ImageVO, Void?>() {
        val TAG = "LoadGalleryImgTask"
        var galleryActivity: GalleryActivity = galleryActivity

        override fun onPreExecute() {
            super.onPreExecute()
            galleryActivity.showProgress()
        }

        override fun doInBackground(vararg galleryDir: File): Void? {
            var imageCnt = 0
            var imageList = arrayOf<ImageVO>()
            galleryDir[0].listFiles().forEachIndexed { index, file ->
                if (file.exists() && file.isFile) {
                    imageCnt++
                    val imageVO = ImageVO(imageCnt, file)
                    imageList += imageVO
                }
            }
            galleryActivity.imageList = imageList
            publishProgress(*imageList)

            return null
        }

        override fun onProgressUpdate(vararg imageList: ImageVO) {
            super.onProgressUpdate(*imageList)

            val rowItemCount = 3
            val verticalMargin = 3
            val horizontalMargin = 5
            val sampleSize = 5
            val context = galleryActivity.baseContext
            val tableLayout = galleryActivity.tableLayout
            val listSize = (tableLayout.width - (horizontalMargin * (rowItemCount + 1))) / rowItemCount

            var tableRow = TableRow(context)
            imageList.forEachIndexed { index, imageVO ->
                val imageView = ImageView(context)

                val imageViewLayoutParams = TableRow.LayoutParams(listSize, listSize)
                imageViewLayoutParams.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin)

                val inputStream = FileInputStream(imageVO.image)
                val options = BitmapFactory.Options()
                options.inSampleSize = sampleSize
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)

                imageView.layoutParams = imageViewLayoutParams
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageBitmap(bitmap)
                imageView.setOnClickListener {
                    Log.d(TAG, "Show Gallery")
                    val intent = Intent(context, ImageActivity::class.java)
                    intent.putExtra("imagePath", imageVO.image.absolutePath)
                    galleryActivity.startActivityForResult(intent, 0)
                }
                tableRow.addView(imageView)

                if ((index % rowItemCount == (rowItemCount - 1)) || (index == imageList.count() - 1)) {
                    tableLayout.addView(tableRow)
                    tableRow = TableRow(context)
                }
            }

            onPostExecute(null)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            galleryActivity.hideProgress()
        }
    }
}