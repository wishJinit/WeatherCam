package com.yujin.weathercam

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File
import java.io.FileInputStream

class ImageActivity : AppCompatActivity() {
    lateinit var imageFile:File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val imagePath = intent.getStringExtra("imagePath")
        imageFile = File(imagePath)

        if (imageFile!!.exists()) {
            setImageView(imageFile!!)
        } else {
            notEditingImage()
            showToast("사진을 찾을 수 없습니다.")
            closeActivity(true)
        }

        setEventListener()
    }

    fun setEventListener(){
        back_btn.setOnClickListener {
            closeActivity(false)
        }
        share_btn.setOnClickListener {
            shareImage()
        }
        delete_btn.setOnClickListener {
            deleteImage()
        }
    }

    fun setImageView(file: File){
        val inputStream = FileInputStream(file)
        val options = BitmapFactory.Options()
        options.inSampleSize = 2
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        imageView.setImageBitmap(bitmap)
    }

    fun showToast(message:String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun closeActivity(isDeletion:Boolean){
        val intent = Intent()
        intent.putExtra("isDeletion", isDeletion)
        setResult(0, intent)
        finish()
    }

    fun shareImage(){
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpeg"

        val uri= Uri.fromFile(imageFile)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

        startActivity(Intent.createChooser(shareIntent, "${getString(R.string.app_name)} 이미지 공유"))
    }

    fun deleteImage(){
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage("정말 삭제하시겠습니까?")
            .setIcon(R.drawable.weathercam_logo)
            .setPositiveButton("삭제") { dialogInterface: DialogInterface, i: Int ->
                imageFile?.let {
                    if (it.exists() && it.delete()){
                        showToast("이미지를 정상적으로 삭제하였습니다.")
                        closeActivity(true)
                    }else{
                        showToast("이미지 삭제에 실패하였습니다.")
                    }
                }
            }
            .setNegativeButton("취소"){ dialogInterface: DialogInterface, i: Int ->
                showToast("취소하였습니다.")
            }
        dialog.create().show()
    }

    fun notEditingImage(){
        btnLayout.visibility = View.GONE
    }
}
