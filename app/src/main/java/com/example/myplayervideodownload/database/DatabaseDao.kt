package com.example.myplayervideodownload.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface DatabaseDao {
    @Insert
    fun insertUrl(urlEntity: UrlEntity)

    @Delete
    fun deleteUrl(urlEntity: UrlEntity)

    @Query("select*from urlentity")
    fun getAllUrl():Flowable<List<UrlEntity>>
}