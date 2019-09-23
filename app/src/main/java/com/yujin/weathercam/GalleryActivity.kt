package com.yujin.weathercam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {
    val TAG = "GalleryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
    }

    override fun onStart() {
        super.onStart()
        showProgress()
    }

    override fun onStop() {
        super.onStop()
        hideProgress()
    }

    fun showProgress() {
        progressbar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progressbar.visibility = View.INVISIBLE
    }
}