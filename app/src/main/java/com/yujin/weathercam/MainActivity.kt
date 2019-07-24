package com.yujin.weathercam

import android.Manifest
import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import com.yujin.weathercam.Util.Log
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {
    init {
        System.loadLibrary("NativeImageProcessor")
    }

    companion object{
        private val TAG = "MainActivity"
        const val REQUEST_CAMERA_PERMISSION:Int = 200
        private lateinit var mSurfaceTextureListener:TextureView.SurfaceTextureListener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSurfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {}

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) = Unit

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean = true

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                openCamera();
            }

        }

        textureView.surfaceTextureListener = mSurfaceTextureListener
    }


    /**
     * 카메라 퍼미션을 체크한다.
     */
    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private fun checkCameraPermission(){
        if(EasyPermissions.hasPermissions(this.applicationContext, Manifest.permission.CAMERA)){
            Log.d(TAG, "This App has the CAMERA permission")
        }else{
            EasyPermissions.requestPermissions(this,
                getString(R.string.request_camera_permission),
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        checkCameraPermission()
    }
}