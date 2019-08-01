package com.yujin.weathercam

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import com.yujin.weathercam.Util.Log
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class MainActivity : AppCompatActivity() {
    init {
        System.loadLibrary("NativeImageProcessor")
    }

    companion object{
        private val TAG = "MainActivity"
        const val REQUEST_CAMERA_PERMISSION:Int = 200
        private lateinit var mSurfaceTextureListener:TextureView.SurfaceTextureListener
    }

    private val MAX_PREVIEW_WIDTH = 720
    private val MAX_PREVIEW_HEIGHT = 1280
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var backgroundHandler: Handler
    private lateinit var cameraDevice:CameraDevice

    private val cameraManager by lazy {
        this?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val deviceStateCallback = object: CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "Camera Device Opened")
            camera?.let {
                cameraDevice = it
                previewSession()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "Camera Device Disconnected")
            camera?.let { it.close()}
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "camera device error (code : $error)")
        }

    }

    /**
     * preview에 대한 세션을 요청하고 생성한다.
     */
    private fun previewSession(){
        val surfaceTexture = textureView.surfaceTexture
        surfaceTexture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val surface = Surface(surfaceTexture)

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(Arrays.asList(surface),
            object : CameraCaptureSession.StateCallback(){
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Create capture session failed!")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    session?.let {
                        captureSession = it
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                        captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    }
                }

            }, null)
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
            connectionCamera()
        }else{
            EasyPermissions.requestPermissions(this,
                getString(R.string.request_camera_permission),
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA)
        }
    }

    /**
     * 요청 키 값에 따라, 카메라 렌즈방향/지원 사진크기 를 반환하여 준다.
     */
    private fun <T> cameraCharacteristics(cameraId:String, key:CameraCharacteristics.Key<T>): T? {
        val characteristics= cameraManager.getCameraCharacteristics(cameraId)
        return when (key){
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw IllegalArgumentException("정상적인 키 값이 필요합니다.")
        }
    }

    /**
     * 카메라 ID값을 반환한다.
     */
    private fun cameraId(lens:Int) :String{
        var deviceId = listOf<String>()
        try{
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }
        } catch (e: CameraAccessException){
            Log.e(TAG, e.toString())
        }
        return deviceId[0]
    }

    /**
     * 카메라를 연결한다.
     */
    @SuppressLint("MissingPermission")
    private fun connectionCamera(){
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
        Log.d(TAG, "deviceId : $deviceId")
        try{
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        }catch (e:CameraAccessException){
            Log.e(TAG, e.toString())
        }catch (e:InterruptedException){
            Log.e(TAG, "Open camera device interrupted while opened")
        }
    }

    /**
     * 카메라 오픈을 요청한다.
     */
    private fun openCamera() {
        checkCameraPermission()
    }
}