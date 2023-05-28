package com.jms.makingsubtitle.util

import android.content.Context

object FuncUtils {

    fun spToPx(context: Context, sp: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f)
    }
}