package com.example.zxing

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.io.File

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1 //呼叫相簿

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            //intent 呼叫相簿
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*" //種類 image
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE) //呼叫回傳
        } catch (e:Exception) {
            Log.i("AA", "e = " + e)
        }
    }

    //處理回傳
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val uri: Uri? = data!!.data //讀取資料 uri
            val path = File( getOutputDirectory(), getAbsolutePath(this, uri!!)).getAbsolutePath(); //讀取相簿圖片位置
            val bitmap = BitmapFactory.decodeFile(path); //將圖片轉 bitmap
            val result = parseInfoFromBitmap(bitmap); //bitmap 轉成 parseintfo
            //如果回傳是空檔則報錯
            if(result != null){
                Log.i("AA", "result = " + result)
            }
        } catch (e:Exception) {
            Log.i("AA", "e = " + e)
        }
    }

    //獲得相簿位置
    @SuppressLint("Range")
    private fun getAbsolutePath(context: Context, uri: Uri): String? {
        try {
            val localContentResolver: ContentResolver = context.getContentResolver() //讀取位置
            val localCursor: Cursor? = localContentResolver.query(uri, null, null, null, null) //搜尋該位置使否有檔案
            localCursor!!.moveToFirst() //將指標指向第一位
            return localCursor!!.getString(localCursor!!.getColumnIndex("_display_name")) //回傳點取圖片的名稱
        } catch (e:Exception){
            Log.i("EE", "e2 = " + e)
            return null
        }
    }

    //取得資料夾位置
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    //bitmap 轉成 parseinfo 然後讀出 QRcode
    fun parseInfoFromBitmap(bitmap: Bitmap): com.google.zxing.Result? {
        try {
            val pixels = IntArray(bitmap.width * bitmap.height) //將圖片長寬轉成 array
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height) //指定圖片規格
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels) //將圖片轉成 RGB 光源
            val binarizer = GlobalHistogramBinarizer(source) //將 RGB 光源轉成二進制
            val image = BinaryBitmap(binarizer) //二進制轉成 bitmap
            var result: com.google.zxing.Result? = null
            result = QRCodeReader().decode(image) //透過 zxing 中的方法 bitmap 轉成 code
            return result
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
        return null
    }
}