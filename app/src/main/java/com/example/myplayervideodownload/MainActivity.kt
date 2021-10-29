package com.example.myplayervideodownload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.example.myplayervideodownload.adapters.AdapterVideo
import com.example.myplayervideodownload.databinding.ActivityMainBinding
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Intent

import android.view.LayoutInflater
import android.webkit.URLUtil
import com.downloader.*
import com.karumi.dexter.PermissionToken

import com.karumi.dexter.MultiplePermissionsReport

import com.karumi.dexter.listener.multi.MultiplePermissionsListener

import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest

import com.example.myplayervideodownload.databinding.DialogBinding
import com.downloader.PRDownloader

import com.example.myplayervideodownload.adapters.DownloadingVideo
import com.example.myplayervideodownload.database.AppDatabase
import com.example.myplayervideodownload.database.UrlEntity
import com.example.myplayervideodownload.databinding.DownloadItemBinding
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import android.provider.MediaStore

import android.content.ContentResolver
import android.database.Cursor
import android.provider.Settings
import android.util.Log
import com.example.myplayervideodownload.databinding.ItemRvBinding
import com.example.myplayervideodownload.models.VideoMy


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var adapterVideo:AdapterVideo
    lateinit var listUrl:ArrayList<UrlEntity>
    lateinit var listDownloadVideo:ArrayList<VideoMy>
    var videoID:Long = 0
    lateinit var appDatabase:AppDatabase
    lateinit var downLoadVideo:DownloadingVideo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       appDatabase = AppDatabase.getInstance(this@MainActivity)
        binding.apply {

            Dexter.withContext(this@MainActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()){
                            loadView()
                        }else{
                            startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) {

                    }
                }).check()



        }

    }

    private fun loadView() {
        binding.apply {

            listUrl = ArrayList()
            getDownload()



            downLoadVideo = DownloadingVideo(object:DownloadingVideo.OnButtonClick{
                override fun onItemClick(str: String, position: Int) {

                }

                override fun onDownloadComplate(
                    itemBinding: DownloadItemBinding,
                    urlEntity: UrlEntity,
                    position: Int
                ) {
                    listUrl.remove(urlEntity)
                    appDatabase.appDao().deleteUrl(urlEntity)
                    downLoadVideo.notifyItemRemoved(position)
                    downLoadVideo.notifyItemRangeChanged(position,listUrl.size)


                    listDownloadVideo = ArrayList()
                    val path = Environment.getExternalStorageDirectory().toString() + "/Download"
                    Log.d("Files", "Path: $path")
                    val directory = File(path)
                    val files = directory.listFiles()
                    Log.d("Files", "Size: " + files.size)
                    for (i in files.indices) {
                        if (files[i].name.substring(files[i].name.length-4).lowercase(Locale.getDefault())==".mp4".lowercase(Locale.getDefault())) {
                            listDownloadVideo.add(VideoMy(files[i].absolutePath))
                            Log.d("Files", "FileName:" + files[i].name)
                        }
                    }
                    adapterVideo.notifyItemInserted(listDownloadVideo.size)
                }

                override fun onButtonCancel(
                    itemBinding: DownloadItemBinding,
                    urlEntity: UrlEntity,
                    position: Int
                ) {
                    listUrl.remove(urlEntity)
                    appDatabase.appDao().deleteUrl(urlEntity)
                    downLoadVideo.notifyItemRemoved(position)
                    downLoadVideo.notifyItemRangeChanged(position,listUrl.size)
                }

                override fun downloadVideoID(downloadId: Long) {
                    videoID = downloadId
                }

            },this@MainActivity)
            binding.loading.adapter = downLoadVideo

            val allUrl = appDatabase.appDao().getAllUrl()
            allUrl.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Consumer<List<UrlEntity>> {
                    override fun accept(t: List<UrlEntity>?) {
                        downLoadVideo.submitList(t)
                        t?.let { listUrl.addAll(it) }
                    }
                })


            adapterVideo = AdapterVideo(this@MainActivity,object:AdapterVideo.OnItemClikc{
                override fun onItemClickListener(
                    my: VideoMy,
                    position: Int,
                    itemRvBinding: ItemRvBinding
                ) {
                    var intent = Intent(this@MainActivity,VideoActivity::class.java)
                    intent.putExtra("path",my.videoUrl)
                    startActivity(intent)
                }
            },listDownloadVideo)
            saving.adapter = adapterVideo





            save.setOnClickListener {
                val urlLink = link.text.toString().trim()

                if (urlLink.isNotEmpty()){

                    val substring = urlLink.substring(urlLink.length - 4)

                    if (substring.lowercase(Locale.getDefault())==".mp4".lowercase(Locale.getDefault())){
                        var urlEntity = UrlEntity()
                        urlEntity.url = urlLink
                        appDatabase.appDao().insertUrl(urlEntity)
                        binding.link.setText("")
                        downLoadVideo.notifyItemInserted(listUrl.size)


                    }else{
                        Toast.makeText(this@MainActivity, "Bu .mp4 file emas", Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this@MainActivity, "No URL", Toast.LENGTH_SHORT).show()
                }

            }


            var brodcasReciver = object:BroadcastReceiver(){
                override fun onReceive(p0: Context?, p1: Intent?) {
                    var action = intent.action
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){
                        val query = DownloadManager.Query()
                        query.setFilterById(videoID)
                    }
                }
            }



//            downloading.addOnClickListener(object : DownloadButtonProgress.OnClickListener {
//                override fun onIdleButtonClick(view: View) {
//                    // called when download button/icon is clicked
//                    Toast.makeText(this@MainActivity, "old", Toast.LENGTH_SHORT).show()
//                    downloading.setIndeterminate()
//                }
//
//                override fun onCancelButtonClick(view: View) {
//                    // called when cancel button/icon is clicked
//                    downloading.setIdle()
//                    Toast.makeText(this@MainActivity, "Finish", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onFinishButtonClick(view: View) {
//                    // called when finish button/icon is clicked
//                    Toast.makeText(this@MainActivity, "finidhsajhsahs", Toast.LENGTH_SHORT).show()
//                    downloading.setFinish()
//                }
//            })
        }
    }

    @SuppressLint("Range")
    private fun getDownload() {
        listDownloadVideo = ArrayList()
        val path = Environment.getExternalStorageDirectory().toString() + "/Download"
        Log.d("Files", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        Log.d("Files", "Size: " + files.size)
        for (i in files.indices) {
            if (files[i].name.substring(files[i].name.length-4).lowercase(Locale.getDefault())==".mp4".lowercase(Locale.getDefault())) {
                listDownloadVideo.add(VideoMy(files[i].absolutePath))
                Log.d("Files", "FileName:" + files[i].name)
            }
        }


//        val contentResolver = contentResolver
//        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
//
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                val title: String =
//                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
//                val duration: String =
//                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
//                val data: String =
//                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
//
////                val videoModel = VideoModel()
////                videoModel.setVideoTitle(title)
////                videoModel.setVideoUri(Uri.parse(data))
////                videoModel.setVideoDuration(timeConversion(duration.toLong()))
////                videoArrayList.add(videoModel)
//            } while (cursor.moveToNext())
//        }

//        val adapter = VideoAdapter(this, videoArrayList)
//        recyclerView.setAdapter(adapter)
    }

//    fun download(downloadPath: String?, destinationPath: String?, context: Context) {
//        var downloadID:Int = 0
//        var alertDialog = AlertDialog.Builder(this)
//        val create = alertDialog.create()
//        var dialogBinding = DialogBinding.inflate(LayoutInflater.from(this),null,false)
//
//        downloadID = PRDownloader.download(downloadPath, direcTory(),fileName(downloadPath.toString()))
//            .build()
//            .setOnStartOrResumeListener {
//
//            }.setOnPauseListener {
//                dialogBinding.progress.isIndeterminate = false
//            }
//            .setOnCancelListener{
//                downloadID = 0;
//                dialogBinding.progress.isIndeterminate = false;
//            }
//            .setOnProgressListener {
//                var per = it.currentBytes*100/ it.totalBytes
//
//                dialogBinding.downloadTxt.text = "Downloading:${per}%"
//                dialogBinding.downloading.progressIndeterminateSweepAngle = (per.toInt()*3.6).toInt()
//            }
//            .start(object : OnDownloadListener {
//                override fun onDownloadComplete() {
//                    create.dismiss()
//                    Toast.makeText(this@MainActivity, "Downloading Complete", Toast.LENGTH_SHORT).show()
//                }
//                override fun onError(error: com.downloader.Error?) {
//                    Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
//                }
//            })
//
//
//
//
//
//        dialogBinding.pauce.setOnClickListener {
//            PRDownloader.pause(downloadID)
//
//            dialogBinding.paly.visibility = View.VISIBLE
//        }
//
//        dialogBinding.paly.setOnClickListener {
//            PRDownloader.resume(downloadID)
//            dialogBinding.paly.visibility = View.INVISIBLE
//        }
//        dialogBinding.cancle.setOnClickListener {
//            PRDownloader.cancel(downloadID)
//            create.dismiss()
//        }
//        create.setView(dialogBinding.root)
//        create.setCancelable(false)
//        create.show()
//
//
//        Toast.makeText(context, "Downloading started", Toast.LENGTH_SHORT).show()
//
//
//        // context.openFileInput("myvideo.mp4", Context.MODE_PRIVATE);
//
//    }
//
//
//    fun direcTory():String{
//        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
//    }
//
//    fun fileName(uri:String):String{
//        return URLUtil.guessFileName(uri,uri,contentResolver.getType(Uri.parse(uri)))
//    }
//
//    fun privetDirectory():String{
//        return filesDir.absolutePath
//    }
}