package me.weishu.kernelsu.ui.xy
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf
import java.util.Locale
object DdBgmPlayer {
    private const val DIR = "dd_music"
    private var player: MediaPlayer? = null
    private var list: MutableList<String> = mutableListOf()
    private var idx = -1
    private var ctx: Context? = null
    private val _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = _isPlaying.value
        set(v) { _isPlaying.value = v }
    private val _enabled = mutableStateOf(true)
    // 【重构】enabled 只保留 getter，避免 JVM 签名与 setEnabled 冲突
    val enabled: Boolean
        get() = _enabled.value
    private val _currentTrack = mutableStateOf("")
    var currentTrack: String
        get() = _currentTrack.value
        set(v) { _currentTrack.value = v }
    private val _trackCount = mutableStateOf(0)
    var trackCount: Int
        get() = _trackCount.value
        set(v) { _trackCount.value = v }
    private val _loopOne = mutableStateOf(false)
    var loopOne: Boolean
        get() = _loopOne.value
        set(v) { _loopOne.value = v }
    private val _volume = mutableStateOf(1f)
    var volume: Float
        get() = _volume.value
        set(v) { _volume.value = v; player?.setVolume(v, v) }
    fun init(c: Context) {
        ctx = c.applicationContext
        val l = try {
            c.assets.list(DIR)?.filter {
                val lc = it.lowercase(Locale.ROOT)
                lc.endsWith(".mp3") || lc.endsWith(".m4a") || lc.endsWith(".ogg") ||
                lc.endsWith(".flac") || lc.endsWith(".wav") || lc.endsWith(".aac")
            } ?: emptyList()
        } catch (_: Exception) { emptyList() }
        list = l.shuffled().toMutableList()
        trackCount = list.size
    }
    // 【重构】setEnabled 独立为普通方法，操作内部状态
    fun setEnabled(on: Boolean) {
        _enabled.value = on
        if (!on) stop() else play()
    }
    fun play() {
        if (!enabled || list.isEmpty()) { isPlaying = false; return }
        val c = ctx ?: return
        try {
            player?.release()
            idx = (idx + 1) % list.size
            val name = list[idx]; currentTrack = name
            val afd = c.assets.openFd("$DIR/$name")
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = false; setVolume(volume, volume)
                setOnCompletionListener { _ ->
                    afd.close()
                    if (loopOne) replay() else this@DdBgmPlayer.play()
                }
                setOnErrorListener { _, _, _ ->
                    afd.close(); this@DdBgmPlayer.isPlaying = false; true
                }
                prepare(); start()
            }
            afd.close(); isPlaying = true
        } catch (_: Exception) { isPlaying = false }
    }
    private fun replay() {
        try { player?.seekTo(0); player?.start(); isPlaying = true }
        catch (_: Exception) { play() }
    }
    fun prev() {
        if (list.isEmpty()) return
        idx = if (idx <= 0) list.size - 1 else idx - 1
        playCurrent()
    }
    private fun playCurrent() {
        if (!enabled) return
        val c = ctx ?: return
        try {
            player?.release()
            val name = list[idx]; currentTrack = name
            val afd = c.assets.openFd("$DIR/$name")
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = false; setVolume(volume, volume)
                setOnCompletionListener { _ ->
                    afd.close()
                    if (loopOne) replay() else this@DdBgmPlayer.play()
                }
                setOnErrorListener { _, _, _ ->
                    afd.close(); this@DdBgmPlayer.isPlaying = false; true
                }
                prepare(); start()
            }
            afd.close(); isPlaying = true
        } catch (_: Exception) { isPlaying = false }
    }
    fun stop() {
        try { player?.stop(); player?.release() } catch (_: Exception) {}
        player = null; isPlaying = false
    }
    fun pause() { try { player?.pause(); isPlaying = false } catch (_: Exception) {} }
    fun resume() {
        if (!enabled) return
        if (player == null) play()
        else try { player?.start(); isPlaying = true } catch (_: Exception) { play() }
    }
}
