package com.jms.makingsubtitle.data.model

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
class VideoTime(
    var totalTime: Long = 0,
    var hh: Int = 0,
    var mm: Int = 0,
    var ss: Int = 0,
    var ms: Int = 0
) : Parcelable {


    constructor(hh: Int, mm: Int, ss: Int, ms: Int): this() {
        this.hh = hh
        this.mm = mm
        this.ss = ss
        this.ms = ms
        totalTime = (hh.toLong() * 3600 + mm.toLong() * 60 + ss.toLong()) * 1000 + ms
    }

    constructor(time: Long): this() {
        this.totalTime = time
        this.ms = (time % 1000).toInt()
        this.ss = (time / 1000 % 60).toInt()
        this.mm = (time / 1000 % 3600 / 60).toInt()
        this.hh = (time / 1000 / 3600).toInt()
    }

    constructor(time: String): this() { // 00:00:00,000 형태로 받은 경우

        try{
            val (_hh_mm_ss, _ms) = time.trim().split(",")
            val (_hh, _mm, _ss) = _hh_mm_ss.split(":").map{ it.toInt() }
            this.hh = _hh
            this.mm = _mm
            this.ss = _ss
            this.ms = _ms.toInt()
            this.totalTime = (hh.toLong() * 3600 + mm.toLong() * 60 + ss.toLong()) * 1000 + ms

        } catch(E: Exception) {
            this.hh = 0
            this.mm = 0
            this.ss = 0
            this.ms = 0
            this.totalTime = 0
        }



    }

    override fun toString(): String {
        return String.format("%02d:%02d:%02d,%03d",hh,mm,ss,ms)
    }
}