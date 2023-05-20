package com.jms.makingsubtitle.ui.view.home

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.jms.makingsubtitle.data.model.SubtitleFile

class SubtitleFileDiffUtil(
    private val oldList: List<SubtitleFile>,
    private val newList: List<SubtitleFile>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uuid == newList[newItemPosition].uuid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val result =  when {
            oldList[oldItemPosition].fileName != newList[newItemPosition].fileName -> false
            oldList[oldItemPosition].jobName != newList[newItemPosition].jobName -> false
            oldList[oldItemPosition].bornDate != newList[newItemPosition].bornDate -> false
            oldList[oldItemPosition].thumbnailUri != newList[newItemPosition].thumbnailUri -> false
            oldList[oldItemPosition].contents != newList[newItemPosition].contents -> false
            oldList[oldItemPosition].lastUpdate != newList[newItemPosition].lastUpdate -> false
            oldList[oldItemPosition].type != newList[newItemPosition].type -> false
            else-> true
        }

        return result
    }


}