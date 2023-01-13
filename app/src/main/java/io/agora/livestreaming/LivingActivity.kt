package io.agora.livestreaming

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.TextureView
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import io.agora.livestreaming.base.BaseUiActivity
import io.agora.livestreaming.base.CommonFragmentAlertDialog
import io.agora.livestreaming.databinding.ActivityLivingBinding
import io.agora.livestreaming.tools.*
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

class LivingActivity : BaseUiActivity<ActivityLivingBinding>() {

    companion object {
        private const val TYPE_LIVING = 1
        private const val TYPE_HLS = 2
        private const val TYPE_FLV = 3

        private const val KEY_CHANNEL = "key_channel"
        private const val KEY_FLV_URL = "key_flv_url"
        private const val KEY_HLS_URL = "key_hls_url"

        fun start(activity: Activity, channel: String, flvUrl: String, hlsUrl: String) {
            val intent = Intent(activity, LivingActivity::class.java).apply {
                putExtra(KEY_CHANNEL, channel)
                putExtra(KEY_FLV_URL, flvUrl)
                putExtra(KEY_HLS_URL, hlsUrl)
            }
            activity.startActivity(intent)
        }
    }

    private var curEnlarge: Int = -1 // 当前放大的画面
    private var curBroadcastUid: Int = -1 // 主播uid,只展示最新上线的
    private val applyConstraintSet = ConstraintSet()
    private var livingMute = true // 直播默认静音
    private var hlsMute = true // hls 拉流默认静音
    private var flvMute = true // flv 拉流默认静音
    private var rtcEngine: RtcEngineEx? = null
    private var mediaPlayerHls: IMediaPlayer? = null
    private var mediaPlayerFlv: IMediaPlayer? = null

    private val channel: String by lazy {
        intent?.getStringExtra(KEY_CHANNEL) ?: ""
    }

    private val hlsUrl: String by lazy {
        intent?.getStringExtra(KEY_HLS_URL) ?: ""
    }

    private val flvUrl: String by lazy {
        intent?.getStringExtra(KEY_FLV_URL) ?: ""
    }

    override fun getViewBinding(inflater: LayoutInflater): ActivityLivingBinding {
        return ActivityLivingBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRtcPlayer()
    }

    override fun initView() {
        StatusBarCompat.setLightStatusBar(this, false)
        applyConstraintSet.clone(binding.layoutRoot)
        binding.containerLiving.setOnClickListener {
            if (FastClickTools.isFastClick(it)) return@setOnClickListener
            if (curEnlarge == TYPE_LIVING) return@setOnClickListener
            enlargeLiving()
        }
        binding.containerHls.setOnClickListener {
            if (FastClickTools.isFastClick(it)) return@setOnClickListener
            if (curEnlarge == TYPE_HLS) return@setOnClickListener
            enlargeHls()
        }
        binding.containerFlv.setOnClickListener {
            if (FastClickTools.isFastClick(it)) return@setOnClickListener
            if (curEnlarge == TYPE_FLV) return@setOnClickListener
            enlargeFlv()
        }
        binding.ivMuteLiving.setOnClickListener {
            if (FastClickTools.isFastClick(it)) return@setOnClickListener
            if (livingMute) {
                binding.ivMuteLiving.setImageResource(R.drawable.audio_high)
            } else {
                binding.ivMuteLiving.setImageResource(R.drawable.audio_mute)
            }
            rtcEngine?.muteAllRemoteAudioStreams(!livingMute)
            livingMute = !livingMute
        }
        binding.ivMuteHls.setOnClickListener {
            if (FastClickTools.isFastClick(it)) return@setOnClickListener
            if (hlsMute) {
                binding.ivMuteHls.setImageResource(R.drawable.audio_high)
            } else {
                binding.ivMuteHls.setImageResource(R.drawable.audio_mute)
            }
            mediaPlayerHls?.mute(!hlsMute)
            hlsMute = !hlsMute
        }
        binding.ivMuteFlv.setOnClickListener {
            if (FastClickTools.isFastClick(it)) return@setOnClickListener
            if (flvMute) {
                binding.ivMuteFlv.setImageResource(R.drawable.audio_high)
            } else {
                binding.ivMuteFlv.setImageResource(R.drawable.audio_mute)
            }
            mediaPlayerFlv?.mute(!flvMute)
            flvMute = !flvMute
        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun enlargeLiving() {
        val parentId = ConstraintLayout.LayoutParams.PARENT_ID
        TransitionManager.beginDelayedTransition(binding.layoutRoot)
        applyConstraintSet.centerHorizontally(R.id.container_living, R.id.layout_root)

        // 极速直播放大
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.TOP, parentId, ConstraintSet.TOP)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.START, R.id.vertical17, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.END, R.id.vertical83, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.BOTTOM, R.id.horizontal66, ConstraintSet.TOP)

        // 其他的缩小
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.TOP, R.id.horizontal66, ConstraintSet.BOTTOM)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.START, R.id.vertical34, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.END, R.id.vertical67, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM)

        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.TOP, R.id.horizontal66, ConstraintSet.BOTTOM)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.START, R.id.vertical67, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.END, parentId, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM)

        applyConstraintSet.applyTo(binding.layoutRoot)
    }

    private fun enlargeHls() {
        val parentId = ConstraintLayout.LayoutParams.PARENT_ID
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.TOP, parentId, ConstraintSet.TOP)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.START, R.id.vertical17, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.END, R.id.vertical83, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.BOTTOM, R.id.horizontal66, ConstraintSet.TOP)

        // 其他的缩小
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.TOP, R.id.horizontal66, ConstraintSet.BOTTOM)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.START, R.id.vertical34, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.END, R.id.vertical67, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM)

        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.TOP, R.id.horizontal66, ConstraintSet.BOTTOM)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.START, R.id.vertical67, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.END, parentId, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM)

        applyConstraintSet.applyTo(binding.layoutRoot)
    }

    private fun enlargeFlv() {
        val parentId = ConstraintLayout.LayoutParams.PARENT_ID
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.TOP, parentId, ConstraintSet.TOP)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.START, R.id.vertical17, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.END, R.id.vertical83, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_flv, ConstraintSet.BOTTOM, R.id.horizontal66, ConstraintSet.TOP)

        applyConstraintSet.connect(R.id.container_living, ConstraintSet.TOP, R.id.horizontal66, ConstraintSet.BOTTOM)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.START, R.id.vertical34, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.END, R.id.vertical67, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_living, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM)

        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.TOP, R.id.horizontal66, ConstraintSet.BOTTOM)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.START, R.id.vertical67, ConstraintSet.START)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.END, parentId, ConstraintSet.END)
        applyConstraintSet.connect(R.id.container_hls, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM)

        applyConstraintSet.applyTo(binding.layoutRoot)
    }

    private fun initRtcPlayer() {
        if (TextUtils.isEmpty(BuildConfig.AGORA_APP_ID)) {
            throw NullPointerException("please check \"local.properties\"")
        }
        if (rtcEngine != null) {
            loadVideo()
            return
        }

        // ------------------ 初始化RTC ------------------
        val config = RtcEngineConfig()
        config.mContext = this
        config.mAppId = BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {

            override fun onError(err: Int) {
                super.onError(err)
                LogTools.e("onError err:$err")
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                LogTools.d("onJoinChannelSuccess channel：$channel,uid:$uid")
            }

            override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
                LogTools.d("onRemoteAudioStateChanged uid:$uid,state:$state,reason:$reason")
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                LogTools.d("onUserJoined uid:$uid")
                setUpRtcVideo(uid)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                LogTools.d("onUserOffline uid:$uid")
                removeRtcVideo(uid)
            }

            // 显示成延迟，因为视频的延迟废弃了
            override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
                super.onRemoteAudioStats(stats)
                LogTools.d("onRemoteAudioStats jitterBufferDelay:${stats?.jitterBufferDelay}")
                ThreadTools.get().runOnMainThread {
                    binding.tvLatency.text = "延时:${stats?.jitterBufferDelay}ms"
                }
            }

            // 宽高 码率
            override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
                super.onRemoteVideoStats(stats)
                LogTools.d("onRemoteVideoStats width:${stats?.width},height:${stats?.height},receivedBitrate:${stats?.receivedBitrate}")
                ThreadTools.get().runOnMainThread {
                    binding.tvRatio.text = "分辨率:${stats?.width}*${stats?.height}"
                    binding.tvRate.text = "码率:${stats?.receivedBitrate}Kbps"
                }
            }
        }

        try {
            rtcEngine = RtcEngine.create(config) as RtcEngineEx?
        } catch (e: Exception) {
            LogTools.e("RtcEngine.create() called error: $e")
        }
        // 将用户角色设置为观众，将延时性设置为低延时。
        val clientRoleOptions = ClientRoleOptions()
        clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE, clientRoleOptions)
        rtcEngine?.enableVideo()
        // 直播场景下，设置频道场景为 BROADCASTING。
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        rtcEngine?.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD_STEREO)
        rtcEngine?.setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)

        loadVideo()
    }

    // 加载主播画面
    private fun setUpRtcVideo(uid: Int) {
        ThreadTools.get().runOnMainThread {
            if (binding.containerLiving.childCount > 0) binding.containerLiving.removeAllViews()
            val textureView = TextureView(this)
            binding.containerLiving.addView(
                textureView, ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            rtcEngine?.setupRemoteVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            curBroadcastUid = uid
            ToastTools.toastLong(this, "主播($uid)已上线")
        }
    }

    // 移除主播画面
    private fun removeRtcVideo(uid: Int) {
        ThreadTools.get().runOnMainThread {
            if (curBroadcastUid == uid) {
                if (binding.containerLiving.childCount > 0) {
                    binding.containerLiving.removeAllViews()
                    curBroadcastUid = -1
                    binding.tvLatency.text = "延时"
                    binding.tvRatio.text = "分辨率"
                    binding.tvRate.text = "码率"
                    ToastTools.toastLong(this, "主播($uid)已下线")
                }
            }
        }
    }

    private fun loadVideo() {
        joinRtcChannel()
        doPlayerHls()
        doPlayerFlv()
    }

    private fun joinRtcChannel() {
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = false
        mediaOptions.autoSubscribeVideo = true
        rtcEngine?.joinChannel(null, channel, 0, mediaOptions)
    }

    private fun doPlayerHls() {
        if (mediaPlayerHls != null) {
            mediaPlayerHls?.destroy()
            mediaPlayerHls = null
        }
        mediaPlayerHls = rtcEngine?.createMediaPlayer()
        mediaPlayerHls?.open(hlsUrl, 0)
        mediaPlayerHls?.mute(true)
        mediaPlayerHls?.registerPlayerObserver(object : MediaPlayerObserver() {

            override fun onPlayerStateChanged(
                state: io.agora.mediaplayer.Constants.MediaPlayerState?,
                error: io.agora.mediaplayer.Constants.MediaPlayerError?
            ) {
                super.onPlayerStateChanged(state, error)
                LogTools.d("IMediaPlayer HLS onPlayerStateChanged ,$state $error")
                when (state) {
                    io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        // 资源准备完成，可以播放
                        mediaPlayerHls?.play()
                    }
                    io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_FAILED -> {
                        // 播放失败
                        ThreadTools.get().runOnMainThread {
                            ToastTools.toastLong(this@LivingActivity,"HLS 拉流播放失败")
                        }
                    }
                    else -> {}
                }
            }
        })
        if (binding.containerHls.childCount > 0) binding.containerHls.removeAllViews()
        val textureView = TextureView(this)
        binding.containerHls.addView(textureView)
        mediaPlayerHls?.setView(textureView)
    }

    private fun doPlayerFlv() {
        if (mediaPlayerFlv != null) {
            mediaPlayerFlv?.destroy()
            mediaPlayerFlv = null
        }
        mediaPlayerFlv = rtcEngine?.createMediaPlayer()
        mediaPlayerFlv?.open(flvUrl, 0)
        mediaPlayerFlv?.mute(true)
        mediaPlayerFlv?.registerPlayerObserver(object : MediaPlayerObserver() {
            override fun onPlayerStateChanged(
                state: io.agora.mediaplayer.Constants.MediaPlayerState?,
                error: io.agora.mediaplayer.Constants.MediaPlayerError?
            ) {
                super.onPlayerStateChanged(state, error)
                LogTools.d("IMediaPlayer FLV onPlayerStateChanged ,$state $error")
                when (state) {
                    io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        // 资源准备完成，可以播放
                        mediaPlayerFlv?.play()
                    }
                    io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_FAILED -> {
                        // 播放失败
                        ThreadTools.get().runOnMainThread {
                            ToastTools.toastLong(this@LivingActivity,"FLV 拉流播放失败")
                        }
                    }
                    else -> {}
                }
            }
        })
        if (binding.containerFlv.childCount > 0) binding.containerFlv.removeAllViews()
        val textureView = TextureView(this)
        binding.containerFlv.addView(textureView)
        mediaPlayerFlv?.setView(textureView)
    }

    override fun onBackPressed() {
        CommonFragmentAlertDialog()
            .titleText("提示")
            .contentText("离开房间？")
            .leftText("取消")
            .rightText("确认")
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    finish()
                }
            }).show(supportFragmentManager, "mtCenterDialog")
    }

    override fun onDestroy() {
        // 退出销毁资源
        mediaPlayerHls?.stop()
        mediaPlayerHls?.destroy()
        mediaPlayerHls = null
        mediaPlayerFlv?.stop()
        mediaPlayerFlv?.destroy()
        mediaPlayerFlv = null
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null

        super.onDestroy()
    }
}