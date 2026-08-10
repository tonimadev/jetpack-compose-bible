package digital.tonima.bibliadigital.ui.bible.tts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
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
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import androidx.media3.common.Player
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
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.R.drawable.ic_next
import digital.tonima.bibliadigital.R.drawable.ic_pause
import digital.tonima.bibliadigital.R.drawable.ic_play
import digital.tonima.bibliadigital.R.drawable.ic_prev
import digital.tonima.bibliadigital.ui.MainActivity
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
        const val CHANNEL_ID = "tts_channel_v16"
        const val NOTIFICATION_ID = 7007
        const val ACTION_PAUSE = "digital.tonima.ACTION_PAUSE"
        const val ACTION_PLAY = "digital.tonima.ACTION_PLAY"
        const val ACTION_NEXT = "digital.tonima.ACTION_NEXT"
        const val ACTION_PREV = "digital.tonima.ACTION_PREV"
    }

    private inner class TTSPlayer : SimpleBasePlayer(mainLooper) {
        override fun getState(): State {
            return State.Builder()
                .setAvailableCommands(Player.Commands.Builder().addAllCommands().build())
                .setPlayWhenReady(
                    this@TTSService.playWhenReady,
                    PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST,
                )
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
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("TTSService onCreate")
        tts = TextToSpeech(this, this)

        mediaSession =
            MediaSession.Builder(this, TTSPlayer())
                .setCallback(
                    object : Callback {
                        override fun onConnect(
                            session: MediaSession,
                            controller: ControllerInfo,
                        ): ConnectionResult {
                            val sessionCommands =
                                DEFAULT_SESSION_COMMANDS.buildUpon()
                                    .add(SessionCommand("SPEAK", Bundle.EMPTY))
                                    .build()
                            return accept(
                                sessionCommands,
                                DEFAULT_PLAYER_COMMANDS,
                            )
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
                )
                .build()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Leitura Bíblica",
                    IMPORTANCE_LOW,
                )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            "SPEAK" ->
                speak(
                    intent.getStringExtra("text") ?: "",
                    intent.getStringExtra("book") ?: "",
                    intent.getIntExtra("chapter", 0),
                )
            ACTION_PAUSE -> pauseSpeech()
            ACTION_PLAY -> resumeSpeech()
            ACTION_NEXT -> ttsManager.emitEvent(TTSEvent.NextChapter)
            ACTION_PREV -> ttsManager.emitEvent(TTSEvent.PreviousChapter)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Timber.d("TTS Start")
                    }

                    override fun onDone(utteranceId: String?) {
                        mainHandler.post {
                            this@TTSService.playWhenReady = false
                            updateNotification()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        Timber.e("TTS Error")
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

        this.playWhenReady = true
        this.playbackState = Player.STATE_READY

        updateNotification()

        if (isTtsReady) {
            tts?.stop()
            tts?.speak(text, QUEUE_FLUSH, null, "bible_tts")
        } else {
            pendingSpeak = {
                tts?.speak(
                    text,
                    QUEUE_FLUSH,
                    null,
                    "bible_tts",
                )
            }
        }
    }

    private fun updateNotification() {
        val session = mediaSession ?: return
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                FLAG_IMMUTABLE,
            )

        val playPauseAction =
            if (playWhenReady) {
                NotificationCompat.Action(ic_pause, "Pausar", getActionPendingIntent(ACTION_PAUSE))
            } else {
                NotificationCompat.Action(ic_play, "Ouvir", getActionPendingIntent(ACTION_PLAY))
            }

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(bookName)
                .setContentText("Capítulo $chapterNumber")
                .setContentIntent(pendingIntent)
                .setOngoing(playWhenReady)
                .addAction(ic_prev, "Anterior", getActionPendingIntent(ACTION_PREV))
                .addAction(playPauseAction)
                .addAction(ic_next, "Próximo", getActionPendingIntent(ACTION_NEXT))
                .setStyle(MediaStyleNotificationHelper.MediaStyle(session).setShowActionsInCompactView(1))
                .setPriority(PRIORITY_LOW)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun getActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TTSService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent, FLAG_IMMUTABLE)
    }

    fun pauseSpeech() {
        tts?.stop()
        this.playWhenReady = false
        updateNotification()
    }

    fun resumeSpeech() {
        if (currentText.isNotEmpty()) {
            this.playWhenReady = true
            tts?.speak(currentText, QUEUE_FLUSH, null, "bible_tts")
            updateNotification()
        }
    }

    fun stopSpeech() {
        tts?.stop()
        this.playWhenReady = false
        this.playbackState = Player.STATE_IDLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        tts?.shutdown()
        mediaSession?.release()
        super.onDestroy()
    }
}
