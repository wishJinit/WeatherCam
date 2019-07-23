package com.yujin.weathercam

import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView

class MainActivity : AppCompatActivity() {
    init {
        System.loadLibrary("NativeImageProcessor")
    }

    companion object{
        private lateinit var mTextureView:TextureView
        private lateinit var mSurfaceTextureListener:TextureView.SurfaceTextureListener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextureView = findViewById(R.id.textureView)
        mSurfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {}

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) = Unit

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean = true

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                openCamera();
            }

        }

        mTextureView.surfaceTextureListener = mSurfaceTextureListener
    }

    private fun openCamera() {

    }
}