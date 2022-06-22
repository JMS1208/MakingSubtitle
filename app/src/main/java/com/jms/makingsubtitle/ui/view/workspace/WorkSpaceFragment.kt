package com.jms.makingsubtitle.ui.view.workspace

import android.app.Activity
import android.content.Intent
import android.media.browse.MediaBrowser
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavArgs
import androidx.navigation.NavDeepLinkRequest.Builder.Companion.fromUri
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.VideoTime
import com.jms.makingsubtitle.databinding.FragmentWorkSpaceBinding
import com.jms.makingsubtitle.databinding.ItemLineListBinding
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.util.Contants.MakeToast
import com.jms.makingsubtitle.util.Contants.REQUEST_SUBTITLE
import com.jms.makingsubtitle.util.Contants.REQUEST_VIDEO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import org.w3c.dom.Text


class WorkSpaceFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        (activity as MainActivity).viewModel
    }
    private val args by navArgs<WorkSpaceFragmentArgs>()
    private val exoplayer: ExoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build()
    }
    private var _binding: FragmentWorkSpaceBinding? = null
    private val binding get() = _binding!!

    private var stopPosition: Long = 0

    private val LEAF_TIME = 3000

    private lateinit var adapter: SubtitleLineAdapter

    private inner class SubtitleLineAdapter() :
        ListAdapter<TimeLine, SubtitleLineAdapter.ViewHolder>(DiffCallback) {

        inner class ViewHolder(val itemBinding: ItemLineListBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(timeLine: TimeLine) {
                itemBinding.apply {
                    indexTv.text = timeLine.lineNum.toString()
                    startTimeEt.apply {
                        setVideoTime(timeLine.startTime)
                        setOnTextChangedListener(object: TextWatcher{
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int
                            ) {}

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int
                            ) {
                                timeLine.startTime = startTimeEt.getVideoTime()
                                viewModel.updateSubtitleFile(args.subtitleJob)
                            }

                            override fun afterTextChanged(p0: Editable?) {}

                        })
                    }

                    endTimeEt.apply {
                        setVideoTime(timeLine.endTime)
                        setOnTextChangedListener(object: TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int
                            ) {

                            }

                            override fun onTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int
                            ) {
                                timeLine.endTime = endTimeEt.getVideoTime()
                                viewModel.updateSubtitleFile(args.subtitleJob)
                            }

                            override fun afterTextChanged(p0: Editable?) {

                            }

                        })

                    }


                    startTimeAutoBtn.apply {
                        setOnClickListener {
                            startTimeEt.setVideoTime(exoplayer.currentPosition)
                        }
                    }

                    endTimeAutoBtn.apply {
                        setOnClickListener {
                            endTimeEt.setVideoTime(exoplayer.currentPosition)
                        }
                    }

                    lineContentEt.apply {
                        setText(timeLine.lineContent)

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                p0: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int
                            ) {
                            }

                            override fun onTextChanged(
                                charSequence: CharSequence?,
                                p1: Int,
                                p2: Int,
                                p3: Int
                            ) {
                                charSequence?.let {
                                    timeLine.lineContent = it.toString()
                                    viewModel.updateSubtitleFile(args.subtitleJob)
                                }

                            }

                            override fun afterTextChanged(p0: Editable?) {}
                        })
                    }


                }


            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemLineListBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val timeLine = currentList[position]
            holder.bind(timeLine)
        }

    }


    private fun saveTimeLineContents(timeLine: TimeLine) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkSpaceBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubtitleLineAdapter()
        binding.timelinesRv.adapter = adapter
        binding.timelinesRv.layoutManager = LinearLayoutManager(requireContext())

        adapter.submitList(args.subtitleJob.contents)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_select_video -> { //영상 가져오기

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "video/*"
                    }

                    startActivityForResult(intent, REQUEST_VIDEO)

                    true
                }
                R.id.menu_item_select_subtitle -> { //기존 자막파일 가져오기
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            "application/x-subrip"
                        else
                            "application/octet-stream"
                    }


                    startActivityForResult(intent, REQUEST_SUBTITLE)

                    true
                }
                R.id.menu_item_set_subtitle_file_title -> { //자막파일명 지정하기

                    true
                }
                R.id.menu_item_set_thumbnail -> { //섬네일 지정하기

                    true
                }
                else -> {
                    false
                }

            }

        }

        binding.resumeBtn.setOnClickListener {

            binding.exoVv.player?.apply {
                if (isPlaying) {
                    viewModel.setStopPosition(currentPosition)
                    pause()
                } else {
                    seekTo(stopPosition)
                    play()
                }
            }

        }

        binding.moveLeftBtn.setOnClickListener {
            binding.exoVv.player?.apply {
                val movePosition =
                    if (currentPosition - LEAF_TIME < 0) 0 else currentPosition - LEAF_TIME
                viewModel.setStopPosition(movePosition)
                seekTo(movePosition)
                if (isPlaying) {
                    play()
                }

            }
        }

        binding.moveRightBtn.setOnClickListener {
            binding.exoVv.player?.apply {
                val movePosition = currentPosition + LEAF_TIME
                viewModel.setStopPosition(movePosition)
                seekTo(movePosition)
                if (isPlaying) {
                    play()
                }
            }
        }





        viewModel.videoUri.observe(viewLifecycleOwner) { videoUri ->

            videoUri?.let {

                val mediaItem = MediaItem.fromUri(it)
                binding.exoVv.apply {
                    player = exoplayer
                    controllerAutoShow = false
                    player?.apply {
                        setMediaItem(mediaItem)
                        prepare()
                        seekTo(stopPosition)
                    }
                }
            }
        }

        viewModel.stopPosition.observe(viewLifecycleOwner) {
            stopPosition = it
        }

        viewModel.loadedSubtitleFile.observe(viewLifecycleOwner) {

            adapter.submitList(it.contents)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_VIDEO -> {
                    data?.let {

                        val uri = it.data
                        viewModel.setVideoUri(uri)


                    } ?: MakeToast(requireContext(), "영상을 가져오는 데 실패하였습니다")
                }

                REQUEST_SUBTITLE -> {
                    data?.let {
                        val uri = it.data

                        viewModel.loadSubtitleFileUri(uri, args.subtitleJob)
                    }
                }

                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TimeLine>() {
            override fun areItemsTheSame(oldItem: TimeLine, newItem: TimeLine): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TimeLine, newItem: TimeLine): Boolean {
                return oldItem == newItem
            }

        }
    }
}