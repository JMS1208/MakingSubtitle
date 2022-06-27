package com.jms.makingsubtitle.data.model

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.jms.makingsubtitle.R

class TimeEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    private var HH_EditText: EditText
    private var MM_EditText: EditText
    private var SS_EditText: EditText
    private var MS_EditText: EditText

    init{
        val v = View.inflate(context, R.layout.cv_time_et,this)
        HH_EditText = v.findViewById(R.id.editText_hh)
        MM_EditText = v.findViewById(R.id.editText_mm)
        SS_EditText = v.findViewById(R.id.editText_ss)
        MS_EditText = v.findViewById(R.id.editText_ms)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomEditText,
            0,0
        )



        HH_EditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocus()
                MM_EditText.requestFocus()
            }
            true
        }
        MM_EditText.setOnEditorActionListener(
            object: TextView.OnEditorActionListener{
                override fun onEditorAction(p0: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        val mm_time: Int
                        try{
                            val tempNum = MM_EditText.text.toString().toInt()

                            when {
                                tempNum < 0 ->{
                                    mm_time = 0
                                    MM_EditText.setText(mm_time.toString())
                                    MM_EditText.setSelection(MM_EditText.text.length)

                                }
                                tempNum > 60 -> {
                                    mm_time = 60
                                    MM_EditText.setText(mm_time.toString())
                                    MM_EditText.setSelection(MM_EditText.text.length)
                                }
                                else -> {
                                    mm_time = tempNum
                                    MM_EditText.setText(mm_time.toString())
                                    clearFocus()
                                    SS_EditText.requestFocus()
                                }
                            }


                        } catch(E: Exception) { }


                    }
                    return true
                }
            }
        )
        SS_EditText.setOnEditorActionListener(
            object: TextView.OnEditorActionListener{
                override fun onEditorAction(p0: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        val ss_time: Int
                        try{
                            val tempNum = SS_EditText.text.toString().toInt()

                            when {
                                tempNum < 0 ->{
                                    ss_time = 0
                                    SS_EditText.setText(ss_time.toString())
                                    SS_EditText.setSelection(SS_EditText.text.length)

                                }
                                tempNum > 60 -> {
                                    ss_time = 60
                                    SS_EditText.setText(ss_time.toString())
                                    SS_EditText.setSelection(SS_EditText.text.length)
                                }
                                else -> {
                                    ss_time = tempNum
                                    SS_EditText.setText(ss_time.toString())
                                    clearFocus()
                                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputMethodManager.hideSoftInputFromWindow(SS_EditText.windowToken, 0)

                                }
                            }


                        } catch(E: Exception) { }


                    }
                    return true
                }
            }
        )

        MS_EditText.setOnEditorActionListener(
            object: TextView.OnEditorActionListener{
                override fun onEditorAction(p0: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        val ms_time: Int
                        try{
                            val tempNum = MS_EditText.text.toString().toInt()

                            when {
                                tempNum < 0 ->{
                                    ms_time = 0
                                    MS_EditText.setText(ms_time.toString())
                                    MS_EditText.setSelection(MS_EditText.text.length)

                                }
                                tempNum > 999 -> {
                                    ms_time = 999
                                    MS_EditText.setText(ms_time.toString())
                                    MS_EditText.setSelection(MS_EditText.text.length)
                                }
                                else -> {
                                    ms_time = tempNum
                                    MS_EditText.setText(ms_time.toString())
                                    clearFocus()
                                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputMethodManager.hideSoftInputFromWindow(MS_EditText.windowToken, 0)

                                }
                            }


                        } catch(E: Exception) { }


                    }
                    return true
                }
            }
        )
    }


    fun getHHText(): String? {
        return HH_EditText.text?.toString()
    }

    fun getMMText(): String? {
        return MM_EditText.text?.toString()
    }

    fun getSSText(): String? {
        return SS_EditText.text?.toString()
    }

    fun getMSText(): String? {
        return MS_EditText.text?.toString()
    }

    fun setVideoTime(videoTime: VideoTime) {
        val hh = String.format("%02d", videoTime.hh)
        val mm = String.format("%02d", videoTime.mm)
        val ss = String.format("%02d", videoTime.ss)
        val ms = String.format("%03d", videoTime.ms)
        HH_EditText.setText(hh)
        MM_EditText.setText(mm)
        SS_EditText.setText(ss)
        MS_EditText.setText(ms)

    }

    fun setVideoTime(totalTime: Long) {
        val videoTime = VideoTime(totalTime)
        val hh = String.format("%02d", videoTime.hh)
        val mm = String.format("%02d", videoTime.mm)
        val ss = String.format("%02d", videoTime.ss)
        val ms = String.format("%03d", videoTime.ms)
        HH_EditText.setText(hh)
        MM_EditText.setText(mm)
        SS_EditText.setText(ss)
        MS_EditText.setText(ms)

    }

    fun getTotalTime(): Long {
        val hh = HH_EditText.text?.let {
            HH_EditText.text.toString().toInt()

        } ?: 0
        val mm = MM_EditText.text?.let {
            MM_EditText.text.toString().toInt()

        } ?: 0
        val ss = SS_EditText.text?.let {
            SS_EditText.text.toString().toInt()

        } ?: 0
        val ms = MS_EditText.text?.let {
            it.toString().toInt()
        } ?: 0

        return VideoTime(hh,mm,ss, ms).totalTime

    }

    fun getVideoTime(): VideoTime { // 비어있으면 0 null이면 0
        val hh = HH_EditText.text?.let {
            if(it.isNotEmpty()) {
                it.toString().toInt()
            } else {
                0
            }
        } ?: 0
        val mm = MM_EditText.text?.let {
            if(it.isNotEmpty()) {
                it.toString().toInt()
            } else {
                0
            }

        } ?: 0
        val ss = SS_EditText.text?.let {
            if(it.isNotEmpty()) {
                it.toString().toInt()
            } else {
                0
            }

        } ?: 0
        val ms = MS_EditText.text?.let {
            if(it.isNotEmpty()) {
                it.toString().toInt()
            } else {
                0
            }
        } ?: 0

        return VideoTime(hh,mm,ss, ms)

    }

    fun setOnTextChangedListener(textWatcher: TextWatcher) {
        HH_EditText.addTextChangedListener(textWatcher)
        MM_EditText.addTextChangedListener(textWatcher)
        SS_EditText.addTextChangedListener(textWatcher)
        MS_EditText.addTextChangedListener(textWatcher)
    }

    fun removeTextChangedListener(textWatcher: TextWatcher) {
        HH_EditText.removeTextChangedListener(textWatcher)
        MM_EditText.removeTextChangedListener(textWatcher)
        SS_EditText.removeTextChangedListener(textWatcher)
        MS_EditText.removeTextChangedListener(textWatcher)
    }


}