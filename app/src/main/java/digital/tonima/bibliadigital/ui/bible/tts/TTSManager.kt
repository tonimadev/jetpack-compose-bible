package digital.tonima.bibliadigital.ui.bible.tts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class TTSManager
    @Inject
    constructor() {
        private var controllerFuture: ListenableFuture<MediaController>? = null
        private val controller: MediaController?
            get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

        private val _events = MutableSharedFlow<TTSEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<TTSEvent> = _events

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        private val playerListener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    Timber.d("Controller state: $playbackState")
                }
            }

        fun bind(context: Context) {
            if (controllerFuture == null) {
                Timber.d("Binding TTS controller")
                val sessionToken = SessionToken(context, ComponentName(context, TTSService::class.java))
                controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
                controllerFuture?.addListener({
                    try {
                        val controller = controllerFuture?.get()
                        controller?.addListener(playerListener)
                        Timber.d("TTS Controller bound successfully")
                    } catch (e: Exception) {
                        Timber.e(e, "Error binding TTS Controller")
                    }
                }, MoreExecutors.directExecutor())
            }
        }

        fun unbind() {
            Timber.d("Unbinding TTS controller")
            controllerFuture?.let {
                MediaController.releaseFuture(it)
                controllerFuture = null
            }
        }

        fun startSpeaking(
            context: Context,
            text: String,
            bookName: String,
            chapter: Int,
        ) {
            bind(context)
            val controller = controller
            if (controller != null) {
                Timber.d("Starting speaking via custom command")
                val bundle =
                    Bundle().apply {
                        putString("text", text)
                        putString("book", bookName)
                        putInt("chapter", chapter)
                    }
                controller.sendCustomCommand(SessionCommand("SPEAK", Bundle.EMPTY), bundle)
            } else {
                Timber.d("Starting speaking via intent (controller not ready)")
                val intent =
                    Intent(context, TTSService::class.java).apply {
                        putExtra("text", text)
                        putExtra("book", bookName)
                        putExtra("chapter", chapter)
                        action = "SPEAK"
                    }
                context.startService(intent)
            }
        }

        fun pause() {
            Timber.d("Pause requested")
            controller?.pause()
        }

        fun resume() {
            Timber.d("Resume requested")
            controller?.play()
        }

        fun stop() {
            Timber.d("Stop requested")
            controller?.stop()
        }

        fun emitEvent(event: TTSEvent) {
            Timber.d("Emitting event: $event")
            scope.launch {
                _events.emit(event)
            }
        }
    }

sealed class TTSEvent {
    object NextChapter : TTSEvent()

    object PreviousChapter : TTSEvent()
}
