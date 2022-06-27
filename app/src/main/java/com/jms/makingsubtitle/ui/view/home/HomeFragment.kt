package com.jms.makingsubtitle.ui.view.home

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.VideoTime
import com.jms.makingsubtitle.databinding.FragmentHomeBinding
import com.jms.makingsubtitle.databinding.ItemJobListBinding
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.util.Contants.DATE_FORMAT
import com.jms.makingsubtitle.util.Contants.MakeToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        (activity as MainActivity).viewModel
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JobListAdapter

    private lateinit var unFilteredList: List<SubtitleFile>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


    private inner class JobListAdapter :
        ListAdapter<SubtitleFile, JobListAdapter.ViewHolder>(DiffCallback), Filterable {


        inner class ViewHolder(val itemBinding: ItemJobListBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(subtitleFile: SubtitleFile) {
                itemBinding.apply {
                    jobNameTv.text = subtitleFile.jobName
                    val fileNameType = "${subtitleFile.fileName}.${subtitleFile.type}"
                    fileNameTv.text = fileNameType
                    jobBornDateTv.text = DateFormat.format(DATE_FORMAT, subtitleFile.bornDate)
                    jobLastDateTv.text = DateFormat.format(DATE_FORMAT, subtitleFile.lastUpdate)
                }

                itemView.setOnClickListener {
                    val action =
                        HomeFragmentDirections.actionFragmentHomeToFragmentWorkSpace(subtitleFile)
                    findNavController().navigate(action)

                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemJobListBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val subtitleFile = currentList[position]
            holder.bind(subtitleFile)
        }

        override fun getFilter(): Filter {
            return object : Filter() {

                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val inputText = charSequence.toString()
                    val filteredList = if (inputText.isEmpty()) {
                        unFilteredList
                    } else {
                        unFilteredList.filter { it.fileName.contains(inputText) }
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
                    submitList(filteredList)
                }

            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = JobListAdapter()
        binding.jobListRv.adapter = adapter
        binding.jobListRv.layoutManager = LinearLayoutManager(requireContext())

        adapter.submitList(emptyList())

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.subtitleFileList.collectLatest {
                adapter.submitList(it)
                unFilteredList = it
            }

        }


        val searchView =
            binding.toolbar.menu.findItem(R.id.menu_item_search).actionView as androidx.appcompat.widget.SearchView

        searchView.apply {
            queryHint = "작업 이름 입력"

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
                    MakeToast(requireContext(), "확인")
                    true
                }
                R.id.menu_item_add -> {
                    val subtitleFile = SubtitleFile(fileName = "임의로 지음")
                    viewModel.insertSubtitleFile(subtitleFile)
                    true
                }
                R.id.menu_item_delete_all -> {
                    viewModel.deleteAllSubtitleFiles()
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


    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SubtitleFile>() {
            override fun areContentsTheSame(oldItem: SubtitleFile, newItem: SubtitleFile): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areItemsTheSame(oldItem: SubtitleFile, newItem: SubtitleFile): Boolean {
                return oldItem == newItem
            }

        }
    }
}