package com.jms.makingsubtitle.ui.view.workspace

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.jms.makingsubtitle.data.model.TimeLine

class TimeLineDiffUtil(
    private val oldList: MutableList<TimeLine>,
    private val newList: MutableList<TimeLine>
)
    : DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition] !== newList[newItemPosition] -> false
            oldList[oldItemPosition].lineContent != newList[newItemPosition].lineContent -> false
            oldList[oldItemPosition].startTime != newList[newItemPosition].startTime -> false
            oldList[oldItemPosition].endTime != newList[newItemPosition].endTime -> false
            else -> true
        }
    }
}