package com.example.myplayervideodownload.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UrlEntity::class],version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun appDao():DatabaseDao

    companion object{
        private var instance:AppDatabase?=null
        @Synchronized
        fun getInstance(context: Context):AppDatabase{
            if (instance==null){
                instance = Room.databaseBuilder(context,AppDatabase::class.java,"myDb.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }

    }
}