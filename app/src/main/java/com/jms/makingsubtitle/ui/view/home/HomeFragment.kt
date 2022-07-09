package com.jms.makingsubtitle.ui.view.home

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.databinding.DialogDeleteAllJobsBinding
import com.jms.makingsubtitle.databinding.DialogSetJobFileNameBinding
import com.jms.makingsubtitle.databinding.FragmentHomeBinding
import com.jms.makingsubtitle.databinding.ItemJobListBinding
import com.jms.makingsubtitle.ui.view.instruction.InstructionFragment
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.util.Contants.DATE_FORMAT
import com.jms.makingsubtitle.util.Contants.MakeToast


class HomeFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        (activity as MainActivity).viewModel
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JobListAdapter

    private var unFilteredList: List<SubtitleFile> = listOf()


    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


    private inner class JobListAdapter :
        RecyclerView.Adapter<JobListAdapter.ViewHolder>(), Filterable {

        private var oldSubtitleFileList = listOf<SubtitleFile>()

        inner class ViewHolder(val itemBinding: ItemJobListBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(subtitleFile: SubtitleFile) {
                itemBinding.apply {
                    jobNameTv.text = subtitleFile.jobName
                    val fileNameType = "${subtitleFile.fileName}.${subtitleFile.type}"
                    fileNameTv.text = fileNameType
                    jobBornDateTv.text = DateFormat.format(DATE_FORMAT, subtitleFile.bornDate)
                    jobLastDateTv.text = DateFormat.format(DATE_FORMAT, subtitleFile.lastUpdate)

                    subtitleFile.thumbnailUri?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .error(R.drawable.thumbnails)
                            .transform(FitCenter(), RoundedCorners(20))
                            .into(thumbnailIv)

                    } ?: Glide.with(requireContext())
                        .load(R.drawable.thumbnails)
                        .into(thumbnailIv)

                }


                itemView.setOnClickListener {
                    val action =
                        HomeFragmentDirections.actionFragmentHomeToFragmentWorkSpace(subtitleFile)
                    findNavController().navigate(action)

                    mInterstitialAd?.show(activity as MainActivity)

                }
                itemView.setOnLongClickListener {
                    val popup = PopupMenu(requireContext(), itemView)

                    popup.apply {
                        inflate(R.menu.menu_home_context)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.menu_item_delete_job -> {

                                    //작업 삭제하기
                                    viewModel.deleteSubtitleFileByUUID(subtitleFile.uuid)
                                    MakeToast(requireContext(), getString(R.string.deleted))
                                    true
                                }
                                R.id.menu_item_set_fileName -> {
                                    // 파일명 지정하기
                                    val dialogBinding =
                                        DialogSetJobFileNameBinding.inflate(layoutInflater)

                                    val dialog = AlertDialog.Builder(requireContext())
                                        .setView(dialogBinding.root)
                                        .create()

                                    dialog.apply {
                                        val windowManager =
                                            activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                        val display = windowManager.defaultDisplay
                                        val size = Point()
                                        display.getSize(size)
                                        val params: ViewGroup.LayoutParams? = window?.attributes
                                        val deviceWidth = size.x
                                        params?.apply {
                                            width = (deviceWidth * 0.9).toInt()
                                        }

                                        window?.apply {
                                            attributes = params as WindowManager.LayoutParams
                                            attributes.y -= 200
                                            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                        }
                                    }


                                    dialogBinding.apply {

                                        etFileName.setText(subtitleFile.fileName)
                                        etJobName.setText(subtitleFile.jobName)
                                        tvTypeName.text = subtitleFile.type

                                        btnCancel.setOnClickListener {
                                            dialog.dismiss()
                                        }

                                        btnSelect.setOnClickListener {
                                            val tmpSubtitleFile = subtitleFile.copy()
                                            tmpSubtitleFile.apply {
                                                this.fileName = etFileName.text.toString()
                                                this.jobName = etJobName.text.toString()
                                            }

                                            viewModel.updateSubtitleFile(tmpSubtitleFile)
                                            dialog.dismiss()
                                            MakeToast(requireContext(), getString(R.string.updated))
                                        }



                                    }
                                    dialog.show()

                                    dialogBinding.iv.let {
                                        YoYo.with(Techniques.StandUp)
                                            .duration(500)
                                            .pivot(1.0F, 1.0F)
                                            .playOn(it)

                                    }
                                    true
                                }
                                R.id.menu_item_set_jobName -> {
                                    // 작업명 지정하기
                                    val dialogBinding =
                                        DialogSetJobFileNameBinding.inflate(layoutInflater)

                                    val dialog = AlertDialog.Builder(requireContext())
                                        .setView(dialogBinding.root)
                                        .create()

                                    dialog.apply {
                                        val windowManager =
                                            activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                        val display = windowManager.defaultDisplay
                                        val size = Point()
                                        display.getSize(size)
                                        val params: ViewGroup.LayoutParams? = window?.attributes
                                        val deviceWidth = size.x
                                        params?.apply {
                                            width = (deviceWidth * 0.9).toInt()
                                        }

                                        window?.apply {
                                            attributes = params as WindowManager.LayoutParams
                                            attributes.y -= 200
                                            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                        }
                                    }

                                    dialogBinding.apply {

                                        etFileName.setText(subtitleFile.fileName)
                                        etJobName.setText(subtitleFile.jobName)
                                        tvTypeName.text = subtitleFile.type

                                        btnCancel.setOnClickListener {
                                            dialog.dismiss()
                                        }

                                        btnSelect.setOnClickListener {

                                            val tmpSubtitleFile = subtitleFile.copy()
                                            tmpSubtitleFile.apply {
                                                this.fileName = etFileName.text.toString()
                                                this.jobName = etJobName.text.toString()
                                            }

                                            viewModel.updateSubtitleFile(tmpSubtitleFile)

                                            dialog.dismiss()
                                            MakeToast(requireContext(), getString(R.string.updated))
                                        }
                                    }
                                    dialog.show()

                                    dialogBinding.iv.let {
                                        YoYo.with(Techniques.StandUp)
                                            .duration(500)
                                            .pivot(1.0F, 1.0F)
                                            .playOn(it)

                                    }

                                    true
                                }

                                else -> false

                            }
                        }
                        if (Util.SDK_INT >= 23) {
                            gravity = Gravity.END
                        }
                        show()
                    }

                    true
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemJobListBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val subtitleFile = oldSubtitleFileList[position]
            holder.bind(subtitleFile)
        }

        override fun getFilter(): Filter {
            return object : Filter() {

                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val inputText = charSequence.toString()
                    val filteredList = if (inputText.isEmpty()) {
                        unFilteredList
                    } else {
                        unFilteredList.filter {
                            it.jobName.contains(inputText)
                        }
                    }

                    return FilterResults().apply {
                        values = filteredList
                    }
                }


                override fun publishResults(
                    charSequence: CharSequence?,
                    filteredResults: FilterResults?
                ) {
                    @Suppress("UNCHECKED_CAST")
                    val filteredList = filteredResults?.values as List<SubtitleFile>
                    setData(filteredList)

                }

            }
        }

        override fun getItemCount(): Int {
            return oldSubtitleFileList.size
        }

        fun setData(newSubtitleFileList: List<SubtitleFile>) {
            val diffUtil = SubtitleFileDiffUtil(oldSubtitleFileList, newSubtitleFileList)
            val diffResults = DiffUtil.calculateDiff(diffUtil)
            oldSubtitleFileList = newSubtitleFileList

            diffResults.dispatchUpdatesTo(this)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        adapter = JobListAdapter()
        binding.jobListRv.adapter = adapter
        binding.jobListRv.layoutManager = LinearLayoutManager(requireContext())


        viewModel.allSubtitleList.observe(viewLifecycleOwner) { subtitleFileList ->

            subtitleFileList?.let {
                adapter.setData(it)
                unFilteredList = it
            }

        }


        val searchView =
            binding.toolbar.menu.findItem(R.id.menu_item_search).actionView as androidx.appcompat.widget.SearchView

        searchView.apply {
            queryHint = getString(R.string.searchJobName)

            setOnQueryTextListener(
                object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(newText: String?): Boolean {


                        searchView.clearFocus()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        //작업명으로 검색해서 찾기

                        newText?.let {
                            if (it.isNotEmpty()) {
                                adapter.filter.filter(newText)
                            } else {
                                adapter.setData(unFilteredList)
                            }
                        }
                        return true
                    }

                }
            )


        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_search -> {
                    MakeToast(requireContext(), getString(R.string.ok))
                    true
                }
                R.id.menu_item_add -> {
                    val subtitleFile = SubtitleFile(fileName = getString(R.string.setupFileName))
                    viewModel.insertSubtitleFile(subtitleFile)
                    true
                }
                R.id.menu_item_delete_all -> {
                    val dialogBinding =
                        DialogDeleteAllJobsBinding.inflate(layoutInflater)

                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogBinding.root)
                        .create()

                    dialog.apply {
                        val windowManager =
                            activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        val display = windowManager.defaultDisplay
                        val size = Point()
                        display.getSize(size)
                        val params: ViewGroup.LayoutParams? = window?.attributes
                        val deviceWidth = size.x
                        params?.apply {
                            width = (deviceWidth * 0.9).toInt()
                        }

                        window?.apply {
                            attributes = params as WindowManager.LayoutParams
                            attributes.y -= 200
                            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        }
                    }

                    dialogBinding.apply {


                        btnCancel.setOnClickListener {
                            dialog.dismiss()
                        }

                        btnDelete.setOnClickListener {

                            val userInput = etDeleteUserInput.text.toString()

                            when {
                                userInput == getString(R.string.deleteComment)-> {
                                    viewModel.deleteAllSubtitleFiles()
                                    dialog.dismiss()
                                    MakeToast(requireContext(), getString(R.string.deleted))
                                }

                                userInput.isBlank() || userInput != getString(R.string.deleteComment)-> {
                                    MakeToast(requireContext(), getString(R.string.noticeDelete))
                                }

                                else-> return@setOnClickListener

                            }



                        }
                    }
                    dialog.show()

                    dialogBinding.iv.let {
                        YoYo.with(Techniques.StandUp)
                            .duration(500)
                            .pivot(1.0F, 1.0F)
                            .playOn(it)

                    }


                    true
                }
                R.id.menu_item_settings -> {
                    val action = HomeFragmentDirections.actionFragmentHomeToSettingsFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> {
                    false
                }
            }

        }

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(requireContext(),"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "2 onAdFailedToLoad: ")
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "2 Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}