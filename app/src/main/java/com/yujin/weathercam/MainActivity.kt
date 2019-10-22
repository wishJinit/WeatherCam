package com.yujin.weathercam

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.yujin.weathercam.Camera.CompareSizesByArea
import com.yujin.weathercam.Camera.ImageSaver
import com.yujin.weathercam.Net.RetrofitClient
import com.yujin.weathercam.Util.Log
import com.yujin.weathercam.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import android.os.*
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.location.*
import com.yujin.weathercam.VO.LocationVO
import java.io.*


class MainActivity : AppCompatActivity() {
    init {
        System.loadLibrary("NativeImageProcessor")
    }

    companion object {
        private val TAG = "MainActivity"
        const val REQUEST_CAMERA_PERMISSION: Int = 200
        const val REQUEST_LOCATION_PERMISSION: Int = 300
        private lateinit var mSurfaceTextureListener: TextureView.SurfaceTextureListener
    }

    private val PICTURE_NAME = "Example.jpeg"
    private val MAX_PREVIEW_WIDTH = 720
    private val MAX_PREVIEW_HEIGHT = 1280
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var captureRequest: CaptureRequest
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private lateinit var cameraDevice: CameraDevice
    private lateinit var characteristics: CameraCharacteristics
    private lateinit var imageReader: ImageReader
    private lateinit var file: File
    private var flashSupported = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var weatherInfo: WeatherVO
    private lateinit var locationInfo: LocationVO

    private var ratio_flag = true
    private var lens_flag = CameraCharacteristics.LENS_FACING_BACK

    private val STATE_PREVIEW = 0
    private val STATE_WAITING_LOCK = 1
    private val STATE_WAITING_PRECAPTURE = 2
    private val STATE_WAITING_NON_PRECAPTURE = 3
    private val STATE_PICTURE_TAKEN = 4
    private var state = STATE_PREVIEW

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        val rootFilePath = "${Environment.getExternalStorageDirectory().absolutePath}/${getString(R.string.app_name)}"
        val rootFile = File(rootFilePath)
        if (!rootFile.exists()) {
            rootFile.mkdir()
        }

        val pictureName = "${getString(R.string.app_name)}_${System.currentTimeMillis()}.jpeg"
        file = File(rootFile, pictureName)
        backgroundHandler?.post(
            ImageSaver(
                it.acquireNextImage(),
                file,
                weatherInfo.filterColor.get()!!,
                this.applicationContext
            )
        )

    }
    private val cameraManager by lazy {
        this?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "Camera Device Opened")
            camera?.let {
                cameraDevice = it
            }
            previewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "Camera Device Disconnected")
            camera?.let { it.close() }
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "camera device error (code : $error)")
        }

    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
            ) {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setLocationData()
        weatherInfo = WeatherVO()
        binding.weather = weatherInfo
        binding.executePendingBindings()

        locationInfo = LocationVO()

        setBtnOnClickListener()
        checkLocationPermission()
        mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {}

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) = Unit

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean = true

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                openCamera()
            }

        }

        initTextureView()
    }


    fun setBtnOnClickListener() {
        take_picture_btn.setOnClickListener {
            Log.d(TAG, "Take a picture")
            lockFocus()
        }
        changeLens.setOnClickListener {
            Log.d(TAG, "Change camera lens")
            changeCameraLens()
        }
        showGallery.setOnClickListener {
            Log.d(TAG, "Show Gallery")
            val intent = Intent(baseContext, GalleryActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 퍼미션에 대한 사용자의 응답을 처리한다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            initTextureView()
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun initTextureView() {
        textureView.surfaceTextureListener = mSurfaceTextureListener
    }

    private fun lockFocus() {
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            state = STATE_WAITING_LOCK
            captureSession?.capture(captureRequestBuilder.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun unlockFocus() {
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(captureRequestBuilder)
            captureSession?.capture(captureRequestBuilder.build(), captureCallback, backgroundHandler)
            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(captureRequest, captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    @SuppressLint("MissingPermission")
    private fun setLocationData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                it.let {
                    Log.d(TAG, "${it.latitude} , ${it.longitude}")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "location error is ${it.message}")
                it.printStackTrace()
            }

        locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_LOW_POWER
            interval = 30 * 60 * 1000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    val location = it.locations[0]
                    locationInfo.lat = location.latitude
                    locationInfo.lon = location.longitude
                    Log.d(TAG, "lat : ${location.latitude} , lon : ${location.longitude}")
                    RetrofitClient().bringWeatherData(weatherInfo, locationInfo)
                }
            }
        }
    }

    /**
     * preview에 대한 세션을 요청하고 생성한다.
     */
    private fun previewSession() {
        //lateinit property imageReader has not been initialized
        if(!::imageReader.isInitialized){
            setImageReader()
        }

        val surfaceTexture = textureView.surfaceTexture
        surfaceTexture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val surface = Surface(surfaceTexture)

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)
        cameraDevice?.createCaptureSession(
            Arrays.asList(surface, imageReader.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    cameraCaptureSession?.let {
                        captureSession = it
                        try {
                            captureRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            setAutoFlash(captureRequestBuilder)

                            captureRequest = captureRequestBuilder.build()
                            captureSession?.setRepeatingRequest(
                                captureRequest,
                                captureCallback, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, e.toString())
                        }
                    }

                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.d(TAG, "Create Capture Session Error")
                }
            }, null
        )
    }

    /**
     * 카메라 퍼미션을 체크한다.
     */
    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private fun checkCameraPermission() {
        if (EasyPermissions.hasPermissions(this.applicationContext, Manifest.permission.CAMERA)) {
            Log.d(TAG, "This App has the CAMERA permission")
            connectionCamera()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.request_camera_permission),
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
        }
    }


    /**
     * GPS 퍼미션을 체크한다.
     */
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private fun checkLocationPermission() {
        if (EasyPermissions.hasPermissions(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "This App has the GPS permission")
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.request_location_permission),
                REQUEST_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (EasyPermissions.hasPermissions(this.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "This App has the GPS permission")
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.request_location_permission),
                REQUEST_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    /**
     * 요청 키 값에 따라, 카메라 렌즈방향/지원 사진크기 를 반환하여 준다.
     */
    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>): T? {
        characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw IllegalArgumentException("정상적인 키 값이 필요합니다.")
        }
    }

    /**
     * 카메라 ID값을 반환한다.
     */
    private fun cameraId(): String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens_flag == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        return deviceId[0]
    }

    /**
     * 카메라를 연결한다.
     */
    @SuppressLint("MissingPermission")
    private fun connectionCamera() {
        val deviceId = cameraId()
        Log.d(TAG, "deviceId : $deviceId")
        try {
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            Log.e(TAG, "Open camera device interrupted while opened")
        }
    }

    /**
     * 카메라 오픈을 요청한다.
     */
    private fun openCamera() {
        checkCameraPermission()

        flashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        setImageReader()
    }

    /**
     * 카메라를 닫는다.
     */
    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    /**
     * 카메라에 대한 요청을 처리하기 위한 background thread를 생성한다.
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraHandlerThread").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    /**
     * 카메라에 대한 요청을 처리하던 background thread를 정지시킨다.
     */
    private fun stopBackgroundThread() {
        backgroundThread?.let {
            it.quitSafely()
            try {
                it.join()
            } catch (e: InterruptedException) {
                Log.d(TAG, e.toString())
            }

        }
    }

    private fun captureStillPicture() {
        try {
            cameraDevice?.let {
                val captureBuilder = it?.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE
                )?.apply {
                    addTarget(imageReader.surface)

                    val rotation = this@MainActivity.windowManager.defaultDisplay.rotation
                    set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation (rotation))
                    set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                }?.also { setAutoFlash(it) }

                val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        Toast.makeText(applicationContext, "Saved: $file", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, file.toString())
                        unlockFocus()
                    }
                }

                captureSession?.apply {
                    stopRepeating()
                    abortCaptures()
                    capture(captureBuilder?.build(), captureCallback, null)
                }
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun getJpegOrientation(_deviceOrientation:Int) : Int {
        var deviceOrientation = _deviceOrientation
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0
        val sensorOrientation =  characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        val facingFront = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        val jpegOrientation = ((sensorOrientation?.plus(deviceOrientation) ?: 0) + 360) % 360;

        return jpegOrientation;
    }

    fun setImageReader(){
        val map = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )
        val largest = Collections.max(
            Arrays.asList(*map?.getOutputSizes(ImageFormat.JPEG)),
            CompareSizesByArea()
        )
        imageReader = ImageReader.newInstance(
            largest.width, largest.height,
            ImageFormat.JPEG, /*maxImages*/ 2
        ).apply {
            setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
        }

        runOnUiThread {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(largest.width, largest.height)
            } else {
                textureView.setAspectRatio(largest.height, largest.width)
            }
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    private fun runPrecaptureSequence() {
        try {
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(
                captureRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun resizeTextureView(view: View) {
        var layoutParams: RelativeLayout.LayoutParams?
        print(ratio_flag)
        val height = if (ratio_flag) {
            (view.width * (4.0 / 3.0)).toInt()
        } else {
            view.width
        }
        ratio_flag = !ratio_flag
        layoutParams = RelativeLayout.LayoutParams(view.width, height)

        view.layoutParams = layoutParams
    }

    private fun changeCameraLens() {

        lens_flag = when (lens_flag) {
            CameraCharacteristics.LENS_FACING_BACK -> CameraCharacteristics.LENS_FACING_FRONT
            CameraCharacteristics.LENS_FACING_FRONT -> CameraCharacteristics.LENS_FACING_BACK
            else -> CameraCharacteristics.LENS_FACING_BACK
        }

        closeCamera()
        connectionCamera()
        openCamera()
    }
}