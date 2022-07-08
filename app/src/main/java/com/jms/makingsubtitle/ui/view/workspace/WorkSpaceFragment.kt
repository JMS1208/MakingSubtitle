package com.jms.makingsubtitle.ui.view.workspace

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.Util
import com.google.android.material.snackbar.Snackbar
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.VideoTime
import com.jms.makingsubtitle.databinding.DialogAddLineBinding
import com.jms.makingsubtitle.databinding.DialogVideoUrlLinkBinding
import com.jms.makingsubtitle.databinding.FragmentWorkSpaceBinding
import com.jms.makingsubtitle.databinding.ItemLineListBinding
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.util.Contants.LAST_LINE_NUM
import com.jms.makingsubtitle.util.Contants.MakeToast
import com.jms.makingsubtitle.util.Contants.REQUEST_EXPORT_FILE
import com.jms.makingsubtitle.util.Contants.REQUEST_SUBTITLE_EUC_KR
import com.jms.makingsubtitle.util.Contants.REQUEST_SUBTITLE_UTF_8
import com.jms.makingsubtitle.util.Contants.REQUEST_VIDEO
import com.jms.makingsubtitle.util.Contants.YOUTUBE_BASE_URL
import com.jms.makingsubtitle.util.Contants.YOUTUBE_BASE_URL_MOBILE

import com.jms.makingsubtitle.data.datastore.LeafTimeMode
import com.jms.makingsubtitle.data.datastore.VibrationOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import kotlinx.coroutines.*
import java.io.FileOutputStream


class WorkSpaceFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        (activity as MainActivity).viewModel
    }
    private val args by navArgs<WorkSpaceFragmentArgs>()
    private var exoplayer: ExoPlayer? = null

    private var _binding: FragmentWorkSpaceBinding? = null
    private val binding get() = _binding!!


    private var copiedTimeLine: TimeLine? = null


    private var playbackPosition = 0L

    private var youtubePlayPosition = 0F

    private val tracker = YouTubePlayerTracker()

    private var recyclerViewState: Parcelable? = null


    private fun initializeExoPlayer() {
        exoplayer = ExoPlayer.Builder(requireContext()).build()
        binding.exoVv.hideController()
        hideSystemUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.exoVv.player?.apply {
            this.pause()
            this.release()
        }
        binding.exoVv.player = null
        exoplayer = null

        binding.pvYoutube.apply {
            getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.pause()
                }
            }
            )
            removeYouTubePlayerListener(tracker)
            release()
        }


        _binding = null
    }

    override fun onPause() { // 이때 플레이어 멈추고 재생시간 저장
        super.onPause()
        viewModel.updateSubtitleFile(args.subtitleJob)
        if (Util.SDK_INT < 24) {
            when (tracker.state) { // 유튜브 재생시간 저장
                PlayerConstants.PlayerState.PAUSED,
                PlayerConstants.PlayerState.BUFFERING,
                PlayerConstants.PlayerState.PLAYING -> {
                    youtubePlayPosition = tracker.currentSecond
                }
                else -> {}
            }

            binding.exoVv.player?.let { // 엑소 재생시간 저장
                playbackPosition = it.currentPosition
            }

        }
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT >= 24) { // 이때 플레이어 멈추고 재생시간 저장
            when (tracker.state) { // 유튜브 재생시간 저장
                PlayerConstants.PlayerState.PAUSED,
                PlayerConstants.PlayerState.BUFFERING,
                PlayerConstants.PlayerState.PLAYING -> {
                    youtubePlayPosition = tracker.currentSecond
                }
                else -> {}
            }

            binding.exoVv.player?.let { // 엑소 재생시간 저장
                playbackPosition = it.currentPosition
            }

        }
    }

    override fun onStart() { // 이때 seekTo로 저장된 position 으로 세팅
        super.onStart()
        if (Util.SDK_INT >= 24) {
            //initializePlayer()
            binding.pvYoutube.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.seekTo(youtubePlayPosition)
                }
            })

            binding.exoVv.player?.apply {
                this.seekTo(playbackPosition)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || exoplayer == null) { // 이때 seekTo로 저장된 position 으로 세팅
            initializeExoPlayer()

            binding.pvYoutube.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.seekTo(youtubePlayPosition)
                }
            })
            binding.exoVv.player?.apply {
                this.seekTo(playbackPosition)
            }
        }
    }


    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(activity?.window!!, false)
        WindowInsetsControllerCompat(activity?.window!!, binding.exoVv).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        }
    }


    private fun setExoPlayerVisible() {
        binding.exoVv.visibility = View.VISIBLE
    }

    private fun setYoutubePlayerVisible() {
        binding.pvYoutube.visibility = View.VISIBLE
    }

    private fun setExoPlayerInvisible() {
        binding.exoVv.visibility = View.GONE
    }

    private fun setYoutubePlayerInvisible() {
        binding.pvYoutube.visibility = View.GONE
    }

    private fun setExoBtnVisible() {
        setExoPlayerVisible()
        setBtnVisible()
    }

    private fun setYoutubeBtnVisible() {
        setYoutubePlayerVisible()
        setBtnVisible()
    }

    private fun setExoBtnInvisible() {
        setExoPlayerInvisible()
        setBtnInvisible()
    }

    private fun setYoutubeBtnInvisible() {
        setYoutubePlayerInvisible()
        setBtnInvisible()
    }

    private fun setBtnVisible() {

        binding.apply {
            btnsPlayers.visibility = View.VISIBLE
        }

    }

    private fun setBtnInvisible() {
        binding.apply {
            btnsPlayers.visibility = View.GONE
        }
    }

    private fun releaseExoPlayer() {
        binding.exoVv.player?.apply {
            this.pause()
            this.release()
        }
        binding.exoVv.player = null

        exoplayer?.apply {
            this.release()
        }
        exoplayer = null

    }

    private fun generateVibration() = lifecycleScope.launch {
        when (viewModel.getVibrationOptions()) {
            VibrationOptions.ACTIVATE.value -> {
                val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        activity?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                if (Util.SDK_INT >= 26) {
                    vib.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            30
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(100)
                }
            }
            else -> return@launch
        }

    }


    private fun attachExoPlayer() {
        exoplayer = ExoPlayer.Builder(requireContext()).build()
        binding.exoVv.hideController()
        binding.exoVv.player = exoplayer
    }


    private fun detachPlayers() {
        binding.pvYoutube.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youtubePlayPosition = tracker.currentSecond
                youTubePlayer.pause()
            }
        })
        binding.exoVv.player?.apply {
            playbackPosition = this.currentPosition
            pause()
        }
        setExoBtnInvisible()
        setYoutubeBtnInvisible()
    }


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

                    val position = viewHolder.absoluteAdapterPosition
                    val timeLine = viewHolder.timeLine.copy()

                    deleteTimeLine(position) // 0부터 시작

                    Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                        .apply {
                            setAction(getString(R.string.cancellation)) {
                                addTimeLine(position, timeLine)
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

    private inner class SubtitleLineAdapter :
        RecyclerView.Adapter<SubtitleLineAdapter.ViewHolder>() {

        private var oldTimeLineList = mutableListOf<TimeLine>()

        inner class ViewHolder(val itemBinding: ItemLineListBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            lateinit var timeLine: TimeLine

            val lineTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    charSequence?.let {

                        val timeLine = oldTimeLineList[absoluteAdapterPosition]
                        timeLine.lineContent = it.toString()
                        args.subtitleJob.contents.timeLines[absoluteAdapterPosition] = timeLine

                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            }
            val startTimeWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val startTimeET = itemBinding.startTimeEt
                    val timeLine = oldTimeLineList[absoluteAdapterPosition]

                    if (timeLine.startTime != startTimeET.getVideoTime()) {
                        timeLine.startTime = startTimeET.getVideoTime()
                        args.subtitleJob.contents.timeLines[absoluteAdapterPosition] = timeLine

                    }


                }

                override fun afterTextChanged(p0: Editable?) {}

            }
            val endTimeWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    val endTimeET = itemBinding.endTimeEt
                    val timeLine = oldTimeLineList[absoluteAdapterPosition]

                    if (timeLine.endTime != endTimeET.getVideoTime()) {
                        timeLine.endTime = endTimeET.getVideoTime()
                        args.subtitleJob.contents.timeLines[absoluteAdapterPosition] = timeLine

                    }

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
                                    MakeToast(requireContext(), getString(R.string.copied))

                                    true
                                }
                                R.id.menu_item_paste_line -> {
                                    copiedTimeLine?.let {
                                        args.subtitleJob.contents.timeLines[absoluteAdapterPosition] =
                                            it
                                        //viewModel.updateSubtitleFile(args.subtitleJob)

                                    } ?: MakeToast(
                                        requireContext(),
                                        getString(R.string.nothingCopied)
                                    )
                                    true
                                }
                                R.id.menu_item_add_upper_line -> {

                                    addTimeLine(this@ViewHolder.absoluteAdapterPosition, TimeLine())

                                    MakeToast(requireContext(), getString(R.string.added))
                                    true
                                }

                                R.id.menu_item_add_lower_line -> {
                                    addTimeLine(
                                        this@ViewHolder.absoluteAdapterPosition + 1,
                                        TimeLine()
                                    )

                                    MakeToast(requireContext(), getString(R.string.added))
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

                    val idx = this@ViewHolder.absoluteAdapterPosition + 1

                    indexTv.text = idx.toString()

                    startTimeEt.apply {
                        setVideoTime(timeLine.startTime)
                    }

                    endTimeEt.apply {
                        setVideoTime(timeLine.endTime)
                    }

                    startTimeAutoBtn.apply {
                        setOnClickListener {

                            generateVibration()

                            val anim = AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.btn_extensions
                            )
                            startTimeAutoBtn.startAnimation(anim)

                            binding.exoVv.player?.let {
                                args.subtitleJob.contents.timeLines[absoluteAdapterPosition].startTime =
                                    VideoTime(it.currentPosition)
                                startTimeEt.setVideoTime(VideoTime(it.currentPosition))
                                //viewModel.updateSubtitleFile(args.subtitleJob)
                            }
                            when (tracker.state) {
                                PlayerConstants.PlayerState.PAUSED,
                                PlayerConstants.PlayerState.PLAYING -> {
                                    val startTime = (tracker.currentSecond * 1000).toLong()
                                    args.subtitleJob.contents.timeLines[absoluteAdapterPosition].startTime =
                                        VideoTime(startTime)
                                    startTimeEt.setVideoTime(VideoTime(startTime))
                                    //viewModel.updateSubtitleFile(args.subtitleJob)
                                }
                                else -> return@setOnClickListener
                            }

                        }
                    }

                    endTimeAutoBtn.apply {
                        setOnClickListener {
                            generateVibration()
                            val anim = AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.btn_extensions
                            )

                            endTimeAutoBtn.startAnimation(anim)



                            binding.exoVv.player?.let {
                                args.subtitleJob.contents.timeLines[absoluteAdapterPosition].endTime =
                                    VideoTime(it.currentPosition)
                                endTimeEt.setVideoTime(VideoTime(it.currentPosition))
                                //viewModel.updateSubtitleFile(args.subtitleJob)
                            }
                            when (tracker.state) {
                                PlayerConstants.PlayerState.PAUSED,
                                PlayerConstants.PlayerState.PLAYING -> {
                                    val endTime = (tracker.currentSecond * 1000).toLong()

                                    args.subtitleJob.contents.timeLines[absoluteAdapterPosition].endTime =
                                        VideoTime(endTime)
                                    endTimeEt.setVideoTime(VideoTime(endTime))
                                    //viewModel.updateSubtitleFile(args.subtitleJob)
                                }
                                else -> return@setOnClickListener
                            }
                        }
                    }

                    lineContentEt.apply {
                        setText(timeLine.lineContent)
                    }

                }

            }
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            holder.enableListener()
            super.onViewAttachedToWindow(holder)
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            holder.disableListener()
            super.onViewDetachedFromWindow(holder)
        }


        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return oldTimeLineList.size
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemLineListBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val timeLine = oldTimeLineList[holder.absoluteAdapterPosition]
            holder.bind(timeLine)

        }

        fun setData(newTimeLineList: MutableList<TimeLine>) {
            val diffUtil = TimeLineDiffUtil(oldTimeLineList, newTimeLineList)
            val diffResults = DiffUtil.calculateDiff(diffUtil)
            oldTimeLineList = newTimeLineList
            diffResults.dispatchUpdatesTo(this)
        }


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkSpaceBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubtitleLineAdapter()
        binding.timelinesRv.adapter = adapter
        binding.timelinesRv.layoutManager = LinearLayoutManager(requireContext())


        //viewModel.setupSubtitleFile(args.subtitleJob)

        ItemTouchHelper(lineListItemHelper).attachToRecyclerView(binding.timelinesRv)

        lifecycle.addObserver(binding.pvYoutube)




        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_settings_work_space -> {
                    val action =
                        WorkSpaceFragmentDirections.actionFragmentWorkSpaceToSettingsFragment()
                    findNavController().navigate(action)

                    true
                }

                R.id.menu_item_remove_players -> {
                    detachPlayers()
                    true
                }

                R.id.menu_item_select_video_file -> { //영상 가져오기

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "video/*"
                    }

                    detachPlayers() // 플레이어들, 버튼 다 안 보이게
                    // 둘다 처리하는 이유는 가져오려다가 뒤로가기버튼해서 안가져오는 경우 있어서
                    releaseExoPlayer()

                    startActivityForResult(intent, REQUEST_VIDEO)


                    true
                }
                R.id.menu_item_select_video_youtube -> {

                    val dialogBinding = DialogVideoUrlLinkBinding.inflate(layoutInflater)
                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogBinding.root)
                        .create()

                    dialogBinding.apply {
                        tvAddUrlLink.setOnClickListener {
                            // 선택 버튼
                            val url = etUrlLink.text.toString()

                            if (url.isEmpty()) {
                                MakeToast(requireContext(), getString(R.string.requestLink))
                            } else {


                                setExoPlayerInvisible()
                                releaseExoPlayer()


                                binding.pvYoutube.getYouTubePlayerWhenReady(object :
                                    YouTubePlayerCallback {
                                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                        val videoId =
                                            url.removePrefix(YOUTUBE_BASE_URL).removePrefix(
                                                YOUTUBE_BASE_URL_MOBILE
                                            )


                                        val thumb = getString(R.string.thumbnailBaseUrl, videoId)
                                        args.subtitleJob.thumbnailUri = Uri.parse(thumb)
                                        viewModel.updateSubtitleFile(args.subtitleJob)


                                        youTubePlayer.cueVideo(videoId, 0F)
                                        youTubePlayer.addListener(tracker)
                                        setYoutubeBtnVisible()
                                    }

                                })





                                dialog.dismiss()
                            }

                        }

                        tvCloseDialog.setOnClickListener {
                            // 닫기 버튼
                            dialog.dismiss()
                        }

                    }

                    dialog.show()

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

                R.id.menu_item_add_line -> { //한줄 추가

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
                            val position = if (addLineNum == LAST_LINE_NUM)
                                LAST_LINE_NUM
                            else
                                addLineNum - 1
                            addTimeLine(position, TimeLine())
                            dialog.dismiss()
                        }
                    }

                    dialog.show()



                    true
                }

                R.id.menu_item_export_subtitle_file -> {
                    //자막 파일 내보내기
                    val subtitleFile = args.subtitleJob
                    val fileName = subtitleFile.fileName.replace(" ", "_") + "." + subtitleFile.type


                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        //addCategory(Intent.CATEGORY_OPENABLE)
                        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            "application/x-subrip"
                        else
                            "application/octet-stream"
                        putExtra(Intent.EXTRA_TITLE, fileName)
                    }

                    startActivityForResult(intent, REQUEST_EXPORT_FILE)

                    true
                }
                else -> false


            }

        }

        binding.resumeBtn.setOnClickListener {

            binding.exoVv.player?.apply {
                if (isPlaying) {
                    pause()

                    binding.resumeBtn.setImageResource(R.drawable.ic_play_24)
                } else {
                    seekTo(this.currentPosition)
                    play()
                    binding.resumeBtn.setImageResource(R.drawable.ic_pause_24)
                }
            }

            if (binding.exoVv.player == null) {
                binding.pvYoutube.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        when (tracker.state) {
                            PlayerConstants.PlayerState.PLAYING -> {
                                youTubePlayer.pause()
                                binding.resumeBtn.setImageResource(R.drawable.ic_play_24)
                            }
                            PlayerConstants.PlayerState.PAUSED,
                            PlayerConstants.PlayerState.VIDEO_CUED -> {
                                youTubePlayer.play()
                                binding.resumeBtn.setImageResource(R.drawable.ic_pause_24)
                            }
                            else -> {}

                        }


                    }


                })
            }

        }

        binding.moveLeftBtn.setOnClickListener {
            binding.exoVv.player?.apply {

                lifecycleScope.launch {

                    val movePosition =
                        if (currentPosition - getLeafTime() * 1000 < 0) 0 else currentPosition - getLeafTime() * 1000
                    seekTo(movePosition)

                }


            }

            if (binding.exoVv.player == null) {
                binding.pvYoutube.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        lifecycleScope.launch {
                            youTubePlayer.seekTo(tracker.currentSecond - getLeafTime())
                        }
                    }

                })
            }

        }

        binding.moveRightBtn.setOnClickListener {
            binding.exoVv.player?.apply {
                lifecycleScope.launch {
                    val movePosition = currentPosition + getLeafTime() * 1000

                    seekTo(movePosition)

                }

            }
            if (binding.exoVv.player == null) {
                binding.pvYoutube.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        lifecycleScope.launch {
                            youTubePlayer.seekTo(tracker.currentSecond + getLeafTime())
                        }

                    }

                })
            }

        }




        viewModel.videoUri.observe(viewLifecycleOwner)
        { videoUri ->

            videoUri?.let {


                args.subtitleJob.thumbnailUri = it
                viewModel.updateSubtitleFile(args.subtitleJob)


                val mediaItem = MediaItem.fromUri(it)

                exoplayer?.apply {
                    attachExoPlayer() // 엑소플레이어 빌드하고, 비디오뷰에 붙여줌
                    setExoBtnVisible()
                    initializeExoPlayer()
                }
                binding.exoVv.apply {
                    controllerAutoShow = false
                    player?.apply {
                        setMediaItem(mediaItem)
                        prepare()
                        seekTo(0)
                    }
                }
            }
        }


        viewModel.getTimeLinesByUUID(args.subtitleJob.uuid)
            .observe(viewLifecycleOwner) { timeLines ->
                timeLines?.let {

                    adapter.setData(timeLines.timeLines)

                }

            }

    }

    private suspend fun getLeafTime(): Int {
        return when (viewModel.getLeafTimeMode()) {
            LeafTimeMode.ONE_SECOND.value -> 1
            LeafTimeMode.THREE_SECOND.value -> 3
            LeafTimeMode.FIVE_SECOND.value -> 5
            LeafTimeMode.SEVEN_SECOND.value -> 7
            LeafTimeMode.TEN_SECOND.value -> 10
            else -> 5
        }
    }

    private fun addTimeLine(position: Int = LAST_LINE_NUM, timeLine: TimeLine = TimeLine()) {
        if (position == LAST_LINE_NUM) {
            args.subtitleJob.contents.timeLines.add(timeLine)
            viewModel.updateSubtitleFile(args.subtitleJob)
        } else {
            args.subtitleJob.contents.timeLines.add(position, timeLine)
            viewModel.updateSubtitleFile(args.subtitleJob)
        }

    }


    private fun deleteTimeLine(position: Int) {
        args.subtitleJob.contents.timeLines.removeAt(position)
        viewModel.updateSubtitleFile(args.subtitleJob)
    }


    private fun exportFile(uri: Uri?) {
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                activity?.contentResolver?.openFileDescriptor(uri, "w").use {

                    FileOutputStream(it!!.fileDescriptor).use { fs ->
                        writeSubtitleFile(fs)
                        it.close()
                    }

                }

                //TODO 애니메이션
            }
        }

    }

    private fun writeSubtitleFile(outputStream: FileOutputStream?) {
        outputStream?.use {

            val subtitle = args.subtitleJob.contents

            val timeLines = subtitle.timeLines

            for (i in timeLines.indices) {
                if (timeLines[i].lineContent.isNotBlank()) {
                    val timeLine = timeLines[i]

                    val line =
                        "${i + 1}\n${timeLine.startTime} --> ${timeLine.endTime}\n${timeLine.lineContent}\n\n"
                    outputStream.write(line.toByteArray())
                }
            }

            outputStream.close()

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


                    } ?: MakeToast(requireContext(), getString(R.string.getVideoFailed))
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

                REQUEST_EXPORT_FILE -> {
                    data?.let {
                        val uri = it.data
                        exportFile(uri)
                    }
                }

                else -> {}
            }
        }
    }


}