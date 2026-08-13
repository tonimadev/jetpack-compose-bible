package digital.tonima.bibliadigital.ui.bible.tts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.speech.tts.UtteranceProgressListener
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.Command
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.Callback
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
import androidx.media3.session.MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
import androidx.media3.session.MediaSession.ConnectionResult.accept
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.bibliadigital.ui.MainActivity
import digital.tonima.bibliadigital.ui.bible.tts.TTSEvent.NextChapter
import digital.tonima.bibliadigital.ui.bible.tts.TTSEvent.PreviousChapter
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class TTSService : MediaSessionService(), TextToSpeech.OnInitListener {
    @Inject
    lateinit var ttsManager: TTSManager

    private var tts: TextToSpeech? = null
    private var mediaSession: MediaSession? = null

    private var isTtsReady = false
    private var pendingSpeak: (() -> Unit)? = null

    private var currentText: String = ""
    private var bookName: String = "Bíblia"
    private var chapterNumber: Int = 0

    private var playWhenReady = false
    private var playbackState = Player.STATE_IDLE

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        const val CHANNEL_ID = "tts_channel_v21"
        const val NOTIFICATION_ID = 9999
        const val ACTION_PAUSE = "digital.tonima.ACTION_PAUSE"
        const val ACTION_PLAY = "digital.tonima.ACTION_PLAY"
        const val ACTION_NEXT = "digital.tonima.ACTION_NEXT"
        const val ACTION_PREV = "digital.tonima.ACTION_PREV"
    }

    private inner class TTSPlayer : SimpleBasePlayer(mainLooper) {
        override fun getState(): State {
            val metadata = MediaMetadata.Builder().setTitle(bookName).setArtist("Capítulo $chapterNumber").build()
            val itemData = MediaItemData.Builder("tts").setMediaMetadata(metadata).build()

            return State.Builder()
                .setAvailableCommands(
                    Player.Commands.Builder()
                        .add(COMMAND_PLAY_PAUSE)
                        .add(COMMAND_STOP)
                        .add(COMMAND_SEEK_TO_NEXT)
                        .add(COMMAND_SEEK_TO_PREVIOUS)
                        .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .add(COMMAND_GET_METADATA)
                        .add(COMMAND_PREPARE)
                        .build(),
                )
                .setPlaylist(listOf(itemData))
                .setPlayWhenReady(this@TTSService.playWhenReady, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                .setPlaybackState(this@TTSService.playbackState)
                .build()
        }

        override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
            if (playWhenReady) resumeSpeech() else pauseSpeech()
            return Futures.immediateVoidFuture()
        }

        override fun handleStop(): ListenableFuture<*> {
            stopSpeech()
            return Futures.immediateVoidFuture()
        }

        override fun handleSeek(
            mediaItemIndex: Int,
            positionMs: Long,
            @Command seekCommand: Int,
        ): ListenableFuture<*> {
            when (seekCommand) {
                COMMAND_SEEK_TO_NEXT, COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> ttsManager.emitEvent(NextChapter)
                COMMAND_SEEK_TO_PREVIOUS, COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> ttsManager.emitEvent(PreviousChapter)
            }
            return Futures.immediateVoidFuture()
        }

        fun requestInvalidate() {
            invalidateState()
        }
    }

    private val ttsPlayer: TTSPlayer by lazy { TTSPlayer() }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        mediaSession =
            MediaSession.Builder(this, ttsPlayer)
                .setCallback(
                    object : Callback {
                        override fun onConnect(
                            session: MediaSession,
                            controller: ControllerInfo,
                        ): ConnectionResult {
                            val commands =
                                DEFAULT_SESSION_COMMANDS
                                    .buildUpon()
                                    .add(SessionCommand("SPEAK", Bundle.EMPTY))
                                    .build()
                            return accept(commands, DEFAULT_PLAYER_COMMANDS)
                        }

                        override fun onCustomCommand(
                            session: MediaSession,
                            controller: ControllerInfo,
                            customCommand: SessionCommand,
                            args: Bundle,
                        ): ListenableFuture<SessionResult> {
                            if (customCommand.customAction == "SPEAK") {
                                speak(
                                    args.getString("text") ?: "",
                                    args.getString("book") ?: "",
                                    args.getInt("chapter", 0),
                                )
                                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                            }
                            return super.onCustomCommand(session, controller, customCommand, args)
                        }
                    },
                ).build()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Áudio Bíblia", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == "SPEAK") {
            val text = intent.getStringExtra("text") ?: ""
            val book = intent.getStringExtra("book") ?: ""
            val chapter = intent.getIntExtra("chapter", 0)
            currentText = text
            bookName = book
            chapterNumber = chapter
            showInitialNotification()
            speak(text, book, chapter)
        } else {
            when (intent?.action) {
                ACTION_PAUSE -> pauseSpeech()
                ACTION_PLAY -> resumeSpeech()
                ACTION_NEXT -> ttsManager.emitEvent(NextChapter)
                ACTION_PREV -> ttsManager.emitEvent(PreviousChapter)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showInitialNotification() {
        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Bíblia Digital")
                .setContentText("Iniciando áudio...")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(id: String?) = Unit

                    override fun onDone(id: String?) {
                        mainHandler.post {
                            playWhenReady = false
                            playbackState = Player.STATE_IDLE
                            ttsPlayer.requestInvalidate()
                            updateNotification()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(id: String?) {
                        mainHandler.post {
                            playWhenReady = false
                            playbackState = Player.STATE_IDLE
                            ttsPlayer.requestInvalidate()
                            updateNotification()
                        }
                    }
                },
            )
            isTtsReady = true
            pendingSpeak?.invoke()
            pendingSpeak = null
        }
    }

    fun speak(
        text: String,
        book: String,
        chapter: Int,
    ) {
        if (text.isEmpty()) return
        currentText = text
        bookName = book
        chapterNumber = chapter
        playWhenReady = true
        playbackState = Player.STATE_READY
        ttsPlayer.requestInvalidate()
        updateNotification()
        if (isTtsReady) {
            tts?.stop()
            tts?.speak(text, QUEUE_FLUSH, null, "bible_tts")
        } else {
            pendingSpeak = { tts?.speak(text, QUEUE_FLUSH, null, "bible_tts") }
        }
    }

    private fun updateNotification() {
        val session = mediaSession ?: return
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)

        val playPauseIcon = if (playWhenReady) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseAction =
            NotificationCompat.Action(
                playPauseIcon,
                if (playWhenReady) "Pausar" else "Ouvir",
                getActionPendingIntent(if (playWhenReady) ACTION_PAUSE else ACTION_PLAY),
            )

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(bookName)
                .setContentText("Capítulo $chapterNumber")
                .setContentIntent(pendingIntent)
                .setOngoing(playWhenReady)
                .addAction(android.R.drawable.ic_media_previous, "Anterior", getActionPendingIntent(ACTION_PREV))
                .addAction(playPauseAction)
                .addAction(android.R.drawable.ic_media_next, "Próximo", getActionPendingIntent(ACTION_NEXT))
                .setStyle(MediaStyleNotificationHelper.MediaStyle(session).setShowActionsInCompactView(1))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun getActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TTSService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent, FLAG_IMMUTABLE)
    }

    fun pauseSpeech() {
        tts?.stop()
        playWhenReady = false
        ttsPlayer.requestInvalidate()
        updateNotification()
    }

    fun resumeSpeech() {
        if (currentText.isNotEmpty()) {
            playWhenReady = true
            playbackState = Player.STATE_READY
            tts?.speak(currentText, QUEUE_FLUSH, null, "bible_tts")
            ttsPlayer.requestInvalidate()
            updateNotification()
        }
    }

    fun stopSpeech() {
        tts?.stop()
        playWhenReady = false
        playbackState = Player.STATE_IDLE
        ttsPlayer.requestInvalidate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onGetSession(info: ControllerInfo) = mediaSession

    override fun onDestroy() {
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            Timber.e(e, "TTS shutdown failed")
        }
        mediaSession?.release()
        super.onDestroy()
    }
}
