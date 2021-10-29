package com.example.myplayervideodownload;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Util {

    public static String RootDirectoryFaceBook = "/My Story Saver /facebook/";
    public static String RootDirectoryShareChat = "/My Story Saver /sharechat/";
    public static String RootDirectoryInstagram = "/My Story Saver /instagram/";

    public static File RootDirectoryWhatsapp = new File(Environment.getExternalStorageState() + "/Download/MyStorySaver/Whatsapp");


    public static void createFileFolder(){
        if(!RootDirectoryWhatsapp.exists()) {
            RootDirectoryWhatsapp.mkdirs();
        }


    }

    public static void download(String downloadPath, String destinationPath, Context context)
    {
        Toast.makeText(context, "Downloading started", Toast.LENGTH_SHORT).show();
        Uri uri = Uri.parse(downloadPath);
        DownloadManager.Request request = new DownloadManager.Request(uri);

       // context.openFileInput("myvideo.mp4", Context.MODE_PRIVATE);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setTitle("Downloading...");

        request.setDestinationInExternalFilesDir(context, Environment.getExternalStorageState(), "salom");

        ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
    }
}