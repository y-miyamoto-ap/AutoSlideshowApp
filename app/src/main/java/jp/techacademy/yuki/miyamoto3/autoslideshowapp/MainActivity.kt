package jp.techacademy.yuki.miyamoto3.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import jp.techacademy.yuki.miyamoto3.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    private var timer: Timer? = null
    private var cursor: Cursor? = null

    private var handler = Handler(Looper.getMainLooper())

    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.autoButton.isEnabled=false
        binding.prevButton.isEnabled=false
        binding.nextButton.isEnabled=false

        binding.autoButton.setOnClickListener {
            if (timer == null) {
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        handler.post {
                            moveToNextImage()
                        }
                    }
                }, 2000, 2000)
                binding.autoButton.text="停止"
                binding.prevButton.isEnabled=false
                binding.nextButton.isEnabled=false
            } else {
                stopTimer()
            }
        }

        binding.prevButton.setOnClickListener {
            moveToPrevImage()
        }

        binding.nextButton.setOnClickListener {
            moveToNextImage()
        }
    }

    override fun onStart() {
        super.onStart()

        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            getContentsInfo()
        } else {
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (cursor != null) {
            cursor!!.close()
            cursor = null
        }
        if (timer != null) {
            stopTimer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    binding.textView.text = "ファイルへのアクセス権限がありません"
                }
        }
    }

    private fun getContentsInfo() {
        val resolver = contentResolver
        if (cursor == null) {
            cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null,
            )
        }
        if(cursor!!.moveToFirst()){
            binding.textView.text=""
            binding.autoButton.isEnabled=true
            binding.prevButton.isEnabled=true
            binding.nextButton.isEnabled=true
            showImage()
        } else {
            binding.textView.text = "画像が取得できません"
        }
    }
    private fun showImage() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        binding.imageView.setImageURI(imageUri)
    }
    private fun moveToNextImage() {
        if (!cursor!!.moveToNext()) {
            cursor!!.moveToFirst()
        }
        showImage()
    }
    private fun moveToPrevImage() {
        if (!cursor!!.moveToPrevious()) {
            cursor!!.moveToLast()
        }
        showImage()
    }
    private fun stopTimer() {
        timer!!.cancel()
        timer = null
        binding.autoButton.text="再生"
        binding.prevButton.isEnabled=true
        binding.nextButton.isEnabled=true
    }
}