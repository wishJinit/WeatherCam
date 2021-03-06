package com.yujin.weathercam.Camera

import android.content.ContentValues
import android.content.Context
import android.media.Image
import android.provider.MediaStore
import com.yujin.weathercam.Util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.yujin.weathercam.Data.ARGBParserInfo
import com.yujin.weathercam.Util.ARGBParser
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubFilter

/**
 * Saves a JPEG [Image] into the specified [File].
 */
internal class ImageSaver(
    /**
     * The JPEG image
     */
    private val image: Image,

    /**
     * The file we save the image into.
     */
    private val file: File,
    private val filterStr: String,
    private val context: Context
) : Runnable {

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file)

            val opts = BitmapFactory.Options()
            opts.inMutable = true
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.count(), opts)

            setFilter(bitmap)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)

            displayImageInGallery(file, context)
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    private fun displayImageInGallery(file: File, context: Context) {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.MediaColumns.DATA, file.absolutePath)

        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun setFilter(bitmap: Bitmap) {
        val argbParser = ARGBParser(filterStr)
        val alpha = argbParser.getHexPercentage(ARGBParserInfo.ALPHA)
        val red = argbParser.getHexPercentageDecimal(ARGBParserInfo.RED)
        val green = argbParser.getHexPercentageDecimal(ARGBParserInfo.GREEN)
        val blue = argbParser.getHexPercentageDecimal(ARGBParserInfo.BLUE)

        val filter = Filter()
        filter.addSubFilter(ColorOverlaySubFilter(alpha, red, green, blue))
        filter.processFilter(bitmap)
    }

    companion object {
        /**
         * Tag for the [Log].
         */
        private val TAG = "ImageSaver"
    }
}
