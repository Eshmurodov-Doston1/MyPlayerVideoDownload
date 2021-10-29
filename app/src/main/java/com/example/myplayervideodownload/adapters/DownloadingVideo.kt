package com.example.myplayervideodownload.adapters

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.example.myplayervideodownload.database.UrlEntity
import com.example.myplayervideodownload.databinding.DownloadItemBinding
import com.github.abdularis.buttonprogress.DownloadButtonProgress

class DownloadingVideo(var onButtonClick: OnButtonClick, var context: Context):ListAdapter<UrlEntity,DownloadingVideo.Vh>(MyDoffUtill()) {
    var downloadID:Int = 0
    inner class Vh(var downloadItemBinding: DownloadItemBinding):RecyclerView.ViewHolder(downloadItemBinding.root){
        fun onBind(urlEntity: UrlEntity,position: Int){
            var config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).build()
            PRDownloader.initialize(context,config)
            if (position==0) {
                onButtonClick.downloadVideoID(downloadID.toLong())
                download(urlEntity,urlEntity.url, "", context, downloadItemBinding, position)
            }else{
                downloadItemBinding.downloading.setIdle()
                downloadItemBinding.cancel.setOnClickListener {
                    PRDownloader.cancel(downloadID)
                    onButtonClick.onButtonCancel(downloadItemBinding,urlEntity,position)
                }
            }
        }
    }

    class MyDoffUtill:DiffUtil.ItemCallback<UrlEntity>(){
        override fun areItemsTheSame(oldItem: UrlEntity, newItem: UrlEntity): Boolean {
            return oldItem.equals(newItem)
        }

        override fun areContentsTheSame(oldItem: UrlEntity, newItem: UrlEntity): Boolean {
            return oldItem.equals(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(DownloadItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position),position)
    }



    fun download(urlEntity: UrlEntity,downloadPath: String?, destinationPath: String?, context: Context,downloadItemBinding: DownloadItemBinding,position: Int) {

        downloadID = PRDownloader.download(downloadPath, direcTory(),fileName(downloadPath.toString()))
            .build()
            .setOnStartOrResumeListener {

            }.setOnPauseListener {
                downloadItemBinding.downloading.setIdle()
            }
            .setOnCancelListener{
                downloadID = 0;
                downloadItemBinding.downloading.setIdle()
            }.setOnProgressListener {
                var per = it.currentBytes*100/ it.totalBytes
                downloadItemBinding.downloadText.text = "Downloading... ${per}%"
                downloadItemBinding.downloading.progressIndeterminateSweepAngle = (per.toInt()*3.6).toInt()
            }.start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                  onButtonClick.onDownloadComplate(downloadItemBinding, urlEntity,position)
                }
                override fun onError(error: com.downloader.Error?) {
                    Toast.makeText(context, "Xatolik Yuz berdi", Toast.LENGTH_SHORT).show()
                }
            })

        downloadItemBinding.cancel.setOnClickListener {
            PRDownloader.cancel(downloadID)
          onButtonClick.onButtonCancel(downloadItemBinding,urlEntity,position)
        }
        downloadItemBinding.downloading.addOnClickListener(object : DownloadButtonProgress.OnClickListener {
            override fun onIdleButtonClick(view: View) {
                downloadItemBinding.downloading.setIndeterminate()
                Toast.makeText(context, "IDLE", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelButtonClick(view: View) {
                // called when cancel button/icon is clicked

                downloadItemBinding.downloading.setIdle()
                PRDownloader.pause(downloadID)
                //  PRDownloader.pause(downloadID)
                Toast.makeText(context, "Finish", Toast.LENGTH_SHORT).show()
            }

            override fun onFinishButtonClick(view: View) {
                Toast.makeText(context, "finidhsajhsahs", Toast.LENGTH_SHORT).show()
                downloadItemBinding.downloading.setFinish()
            }
        })
    }

    fun direcTory():String{
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    }

    fun fileName(uri:String):String{
        return URLUtil.guessFileName(uri,uri,context.contentResolver.getType(Uri.parse(uri)))
    }

    interface OnButtonClick{
        fun onItemClick(str:String,position: Int)
        fun onDownloadComplate(itemBinding: DownloadItemBinding,urlEntity: UrlEntity,position: Int)
        fun onButtonCancel(itemBinding: DownloadItemBinding,urlEntity: UrlEntity,position: Int)
        fun downloadVideoID(downloadId:Long)
    }
}