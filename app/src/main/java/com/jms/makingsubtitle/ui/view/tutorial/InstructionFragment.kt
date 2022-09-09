package com.jms.makingsubtitle.ui.view.tutorial

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.DialogFragment
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.datastore.ShowOptions
import com.jms.makingsubtitle.databinding.FragmentInstructionBinding
import com.jms.makingsubtitle.databinding.ItemSliderBinding
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel


class InstructionFragment : DialogFragment() {

    private var _binding: FragmentInstructionBinding? = null
    private val binding get() = _binding!!

    private val viewModel : MainViewModel by lazy {
        (activity as MainActivity).viewModel
    }

    private val imageList: List<Int> = listOf(
            R.drawable.ex_001,
        R.drawable.ex_002,
        R.drawable.ex_003,
        R.drawable.ex_004,
        R.drawable.ex_005,
        R.drawable.ex_006
    )

    private inner class ImageSliderAdapter(val imageList: List<Int>): RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemSliderBinding): RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(imageId: Int) {

                    Glide.with(requireContext())
                        .load("")
                        .apply(RequestOptions().placeholder(imageId))
                        //.transform(CenterCrop(), RoundedCorners(20))
                        .into(itemBinding.imageSlider)

                //itemBinding.imageSlider.setImageResource(imageId)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomAlertDialog)
    }

    override fun onResume() {
        //다이얼로그 기기 사이즈가져와서 보여주기
        super.onResume()

        val windowManager = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val deviceWidth = size.x
        params?.width = (deviceWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionBinding.inflate(inflater,container, false)
        // Inflate the layout for this fragment
        dialog?.apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                requestFeature(Window.FEATURE_NO_TITLE)
            }
            setCanceledOnTouchOutside(false)
        }



        return binding.root
    }


    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16,8,16,8)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        for(i in 0 until count) {
            indicators[i] = ImageView(requireContext()).apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bg_indicator_inactive))
                layoutParams = params
            }
            binding.llIndicators.addView(indicators[i])
        }

        setCurrentIndicator(0)


    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = binding.llIndicators.childCount
        for(i in 0 until childCount) {
            val imageView = binding.llIndicators.getChildAt(i) as ImageView
            if(i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_indicator_activate
                ))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_indicator_inactive
                ))
            }
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vp2.apply {
            offscreenPageLimit = 1
            adapter = ImageSliderAdapter(imageList)
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentIndicator(position)
                }
            })
        }

        setupIndicators(imageList.size)


        binding.btnCloseDialog.setOnClickListener {
            this.dismiss()
        }

        binding.btnDoNotShowMe.setOnClickListener {
            viewModel.saveShowOptions(ShowOptions.DO_NOT_SHOW_AGAIN.value)
            this.dismiss()
        }

    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}