package com.jms.makingsubtitle.ui.view.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateFormat
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
import com.google.android.exoplayer2.util.Util
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.SubtitleFile
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
                            .transform(FitCenter(),RoundedCorners(20))
                            .into(thumbnailIv)

                    } ?: Glide.with(requireContext())
                        .load(R.drawable.ic_no_thumb)
                        .into(thumbnailIv)

                }


                itemView.setOnClickListener {
                    val action =
                        HomeFragmentDirections.actionFragmentHomeToFragmentWorkSpace(subtitleFile)
                    findNavController().navigate(action)

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
                                    dialogBinding.apply {

                                        etFileName.setText(subtitleFile.fileName)
                                        etJobName.setText(subtitleFile.jobName)
                                        tvSubType.text = subtitleFile.type

                                        tvCloseDialog.setOnClickListener {
                                            dialog.dismiss()
                                        }

                                        tvSelect.setOnClickListener {
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

                                    true
                                }
                                R.id.menu_item_set_jobName -> {
                                    // 작업명 지정하기
                                    val dialogBinding =
                                        DialogSetJobFileNameBinding.inflate(layoutInflater)

                                    val dialog = AlertDialog.Builder(requireContext())
                                        .setView(dialogBinding.root)
                                        .create()
                                    dialogBinding.apply {

                                        etFileName.setText(subtitleFile.fileName)
                                        etJobName.setText(subtitleFile.jobName)
                                        tvSubType.text = subtitleFile.type

                                        tvCloseDialog.setOnClickListener {
                                            dialog.dismiss()
                                        }

                                        tvSelect.setOnClickListener {

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
                            it.jobName.contains(inputText) }
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


        InstructionFragment().show(
            childFragmentManager, "instruction_fragment"
        )



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
                    viewModel.deleteAllSubtitleFiles()
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

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}