package com.jms.makingsubtitle.ui.view.tutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.datastore.ShowOptions

import com.jms.makingsubtitle.databinding.ActivityTutorialBinding
import com.jms.makingsubtitle.databinding.ItemSliderBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialActivity : AppCompatActivity() {
    private val binding: ActivityTutorialBinding by lazy {
        ActivityTutorialBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<TutorialViewModel>()

    private val imageList: List<Int> = listOf(
        R.drawable.tutorial_1,
        R.drawable.tutorial_2,
        R.drawable.tutorial_3,
        R.drawable.tutorial_4,
        R.drawable.tutorial_5,
        R.drawable.tutorial_6,
        R.drawable.tutorial_7,
        R.drawable.tutorial_8
    )

    private inner class ImageSliderAdapter(val imageList: List<Int>) :
        RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemSliderBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(imageId: Int) {

                Glide.with(this@TutorialActivity)
                    .load(imageId)
                    .centerInside()
                    .into(itemBinding.imageSlider)

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemSliderBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageId = imageList[position]
            holder.bind(imageId)
        }

        override fun getItemCount(): Int = imageList.size


    }

    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        for (i in 0 until count) {
            indicators[i] = ImageView(this).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@TutorialActivity,
                        R.drawable.bg_indicator_inactive
                    )
                )
                layoutParams = params
            }
            binding.llIndicators.addView(indicators[i])
        }

        setCurrentIndicator(0)


    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = binding.llIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = binding.llIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_activate
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_inactive
                    )
                )
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.btnSkip.setOnClickListener {
            viewModel.saveShowOptions(ShowOptions.DO_NOT_SHOW_AGAIN.value)
            Toast.makeText(this, getString(R.string.tutorialComment), Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.viewPager.apply {
            offscreenPageLimit = 1
            adapter = ImageSliderAdapter(imageList)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentIndicator(position)
                }
            })
        }

        setupIndicators(imageList.size)

    }
}