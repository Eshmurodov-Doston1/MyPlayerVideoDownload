package com.example.myplayervideodownload

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import com.example.myplayervideodownload.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {
    lateinit var activityVideoBinding: ActivityVideoBinding
    lateinit var mediaController: MediaController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVideoBinding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(activityVideoBinding.root)
        activityVideoBinding.apply {
            val extras = intent.getStringExtra("path")

            mediaController = MediaController(this@VideoActivity)

            mediaController.setAnchorView(video )


            var uri = Uri.parse(extras)
            video.setMediaController(mediaController)
            video.setVideoURI(uri)

            video.start()
        }
    }
}