package com.example.myplayervideodownload.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.example.myplayervideodownload.databinding.ItemRvBinding
import com.example.myplayervideodownload.models.VideoMy

class AdapterVideo(var context: Context,var onItemClikc: OnItemClikc,var list: List<VideoMy>):RecyclerView.Adapter<AdapterVideo.Vh>() {
    inner class Vh(var itemRvBinding: ItemRvBinding):RecyclerView.ViewHolder(itemRvBinding.root){
        fun onBind(videoMy: VideoMy,position: Int){
            itemRvBinding.card.setOnClickListener {
                onItemClikc.onItemClickListener(videoMy,position,itemRvBinding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemRvBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position],position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnItemClikc{
        fun onItemClickListener(my: VideoMy,position: Int,itemRvBinding: ItemRvBinding)
    }
}