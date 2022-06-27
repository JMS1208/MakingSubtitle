package com.jms.makingsubtitle.ui.view.workspace

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.PopupMenuCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.snackbar.Snackbar
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.TimeEditText
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.databinding.DialogAddLineBinding
import com.jms.makingsubtitle.databinding.FragmentWorkSpaceBinding
import com.jms.makingsubtitle.databinding.ItemLineListBinding
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.util.Contants.LAST_LINE_NUM
import com.jms.makingsubtitle.util.Contants.MakeToast
import com.jms.makingsubtitle.util.Contants.REQUEST_SUBTITLE_EUC_KR
import com.jms.makingsubtitle.util.Contants.REQUEST_SUBTITLE_UTF_8
import com.jms.makingsubtitle.util.Contants.REQUEST_VIDEO
import com.jms.makingsubtitle.util.Contants.convertTimeLineListToPage
import com.jms.makingsubtitle.util.Contants.convertTimeLinesToPage
import kotlinx.coroutines.*


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

    private var copiedTimeLine: TimeLine? = null

    private var storedTime: Long = System.currentTimeMillis()


    private val lineListItemHelper: ItemTouchHelper.Callback by lazy {
        object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                )

            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as? SubtitleLineAdapter.ViewHolder)?.let {
                    val timeLine = viewHolder.timeLine

                    deleteTimeLine(viewHolder.timeLine.lineNum)

                    Snackbar.make(requireView(), "삭제 되었습니다", Snackbar.LENGTH_SHORT).apply{
                        setAction("취소") {
                            //TODO {이거 수정해야됨 첫번째 라인 삭제시 오류 }
                            addTimeLine(timeLine)
                        }
                    }.show()
                }
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return true
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

        }
    }

    private lateinit var adapter: SubtitleLineAdapter

    private inner class SubtitleLineAdapter() :
        ListAdapter<TimeLine, SubtitleLineAdapter.ViewHolder>(DiffCallback) {

        inner class ViewHolder(val itemBinding: ItemLineListBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            lateinit var timeLine: TimeLine

            val lineTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    charSequence?.let {

                        val timeLine = currentList[bindingAdapterPosition]
                        timeLine.lineContent = it.toString()
                        viewModel.updateSubtitleFile(args.subtitleJob)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            }

            val startTimeWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val startTimeET = itemBinding.startTimeEt
                    val timeLine = currentList[bindingAdapterPosition]
                    timeLine.startTime = startTimeET.getVideoTime()
                    viewModel.updateSubtitleFile(args.subtitleJob)
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            }
            val endTimeWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val endTimeET = itemBinding.endTimeEt
                    val timeLine = currentList[bindingAdapterPosition]
                    timeLine.startTime = endTimeET.getVideoTime()
                    viewModel.updateSubtitleFile(args.subtitleJob)
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            }

            fun enableListener() {
                itemBinding.apply {
                    lineContentEt.addTextChangedListener(lineTextWatcher)
                    startTimeEt.setOnTextChangedListener(startTimeWatcher)
                    endTimeEt.setOnTextChangedListener(endTimeWatcher)
                }
            }

            fun disableListener() {
                itemBinding.apply {
                    lineContentEt.removeTextChangedListener(lineTextWatcher)
                    startTimeEt.removeTextChangedListener(startTimeWatcher)
                    endTimeEt.removeTextChangedListener(endTimeWatcher)
                }
            }


            fun bind(timeLine: TimeLine) {
                this.timeLine = timeLine


                itemView.setOnLongClickListener {

                    val popup = PopupMenu(requireContext(), itemView)

                    popup.apply {
                        inflate(R.menu.menu_line_context)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.menu_item_copy_line -> {
                                    copiedTimeLine = timeLine
                                    MakeToast(requireContext(),"복사 되었습니다")
                                    true
                                }
                                R.id.menu_item_paste_line -> {
                                    copiedTimeLine?.let {
                                        it.lineNum = timeLine.lineNum
                                        bind(it)
                                    } ?: MakeToast(requireContext(),"복사한 내용이 없습니다")
                                    true
                                }
                                R.id.menu_item_add_upper_line-> {
                                    addTimeLine(timeLine.lineNum)
                                    MakeToast(requireContext(),"추가되었습니다")
                                    true
                                }

                                R.id.menu_item_add_lower_line-> {
                                    addTimeLine(timeLine.lineNum + 1)
                                    MakeToast(requireContext(), "추가되었습니다")
                                    true
                                }

                                else -> false
                            }

                        }
                        gravity = Gravity.END
                        show()
                    }
                    true

                }

                itemBinding.apply {

                    indexTv.text = timeLine.lineNum.toString()
                    startTimeEt.apply {
                        setVideoTime(timeLine.startTime)
                    }

                    endTimeEt.apply {
                        setVideoTime(timeLine.endTime)
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
                    }

                }

            }
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            (holder as ViewHolder).enableListener()
            super.onViewAttachedToWindow(holder)
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            (holder as ViewHolder).disableListener()
            super.onViewDetachedFromWindow(holder)
        }


        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return currentList.size
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemLineListBinding.inflate(layoutInflater, parent, false)


            return ViewHolder(itemBinding)
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val timeLine = currentList[holder.absoluteAdapterPosition]
            holder.bind(timeLine)


        }

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
        viewModel.setupSubtitleFile(args.subtitleJob)



        ItemTouchHelper(lineListItemHelper).attachToRecyclerView(binding.timelinesRv)

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
                R.id.menu_item_select_subtitle_euc_kr -> { //기존 자막파일 가져오기
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            "application/x-subrip"
                        else
                            "application/octet-stream"
                    }


                    startActivityForResult(intent, REQUEST_SUBTITLE_EUC_KR)

                    true
                }

                R.id.menu_item_select_subtitle_utf_8 -> { //기존 자막파일 가져오기
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            "application/x-subrip"
                        else
                            "application/octet-stream"

                    }


                    startActivityForResult(intent, REQUEST_SUBTITLE_UTF_8)

                    true
                }
                R.id.menu_item_set_subtitle_file_title -> { //자막파일명 지정하기

                    true
                }
                R.id.menu_item_set_thumbnail -> { //섬네일 지정하기

                    true
                }
                R.id.menu_item_add_line -> {

                    val dialogBinding = DialogAddLineBinding.inflate(layoutInflater)

                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogBinding.root)
                        .create()

                    dialogBinding.apply {
                        addLineLastTv.setOnClickListener {
                            addTimeLine()
                            dialog.dismiss()
                        }
                        addLineNumTv.setOnClickListener {
                            val addLineNum = if (lineNumEt.text.isNotEmpty())
                                lineNumEt.text.toString().toInt()
                            else
                                LAST_LINE_NUM
                            addTimeLine(addLineNum)
                            dialog.dismiss()
                        }
                    }

                    dialog.show()



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
        binding.nextPageBtn.setOnClickListener {

            val timeLineList: MutableList<TimeLine>? = viewModel.currentList.value
            timeLineList?.let {
                val page: Int = convertTimeLineListToPage(it)
                setCurrentPage(page + 1)
                //viewModel.setupCurrentList(page + 1)
            }

        }
        binding.prevPageBtn.setOnClickListener {
            val timeLineList: MutableList<TimeLine>? = viewModel.currentList.value
            timeLineList?.let {
                val page: Int = convertTimeLineListToPage(it)
                setCurrentPage(page - 1)
                //viewModel.setupCurrentList(page - 1)
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


        viewModel.currentList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.loadedSubtitleFile.observe(viewLifecycleOwner) {
            viewModel.setupCurrentList(1)
        }


    }

    private fun addTimeLine(timeLine: TimeLine) {
        viewModel.addTimeLine(timeLine)
    }

    private fun addTimeLine(lineNum: Int = LAST_LINE_NUM) {
        viewModel.addTimeLine(lineNum)
    }

    private fun deleteTimeLine(lineNum: Int) {
        viewModel.deleteTimeLine(lineNum)
    }

    private fun setCurrentPage(page: Int) {

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val subtitleFile = viewModel.getSubtitleFileByUUID(args.subtitleJob.uuid)

            val lastPage = convertTimeLinesToPage(subtitleFile.contents)

            withContext(Dispatchers.Main) {
                when {
                    page < 1 -> {
                        MakeToast(requireContext(), "첫번째 페이지입니다")
                    }
                    page > lastPage -> {
                        MakeToast(requireContext(), "마지막 페이지입니다")
                    }
                    else -> {
                        viewModel.setupCurrentList(page)
                    }
                }
            }

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

                REQUEST_SUBTITLE_UTF_8 -> {
                    data?.let {
                        val uri = it.data

                        viewModel.loadSubtitleFileUri(uri, args.subtitleJob, "UTF-8")
                    }
                }

                REQUEST_SUBTITLE_EUC_KR -> {
                    data?.let {
                        val uri = it.data

                        viewModel.loadSubtitleFileUri(uri, args.subtitleJob, "EUC-KR")
                    }
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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