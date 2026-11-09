package pl.powernap.studio

import android.app.Service
import android.app.NotificationManager
import android.content.Intent
import android.media.*
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.*
import androidx.core.app.NotificationManagerCompat

class NapService : Service() {
    companion object { var alive = false; private set }
    private lateinit var store: Store
    private val handler = Handler(Looper.getMainLooper())
    private val audio = AudioEngine()
    private lateinit var manager: AudioManager
    private lateinit var media: MediaSession
    private var focus: AudioFocusRequest? = null
    private var lock: PowerManager.WakeLock? = null
    private var focusGain = 1f
    private var generation = 0
    private val vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    private val ticker = object : Runnable {
        override fun run() {
            val s = store.session
            val now = System.currentTimeMillis()
            if (s.phase == Phase.NAPPING) {
                if (now >= s.end) wake(false)
                else {
                    audio.scene = store.scene
                    val left = ((s.end-now)/60000f).coerceIn(0f,1f)
                    audio.volume = store.volume * (.3f + .7f*left) * focusGain
                }
            } else if (s.phase == Phase.WAKING || s.phase == Phase.TESTING) {
                if(now >= s.end) finish() else audio.volume = store.alarmVolume * focusGain
            }
            if (store.session.phase != Phase.IDLE) handler.postDelayed(this,500)
        }
    }
    override fun onCreate() {
        super.onCreate()
        alive = true
        store = Store(this)
        manager = getSystemService(AudioManager::class.java)
        media = MediaSession(this,"Drzemka").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() { startNap() }
                override fun onPause() { pauseNap() }
                override fun onStop() { finish() }
            })
            isActive = true
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(Notices.SESSION,Notices.session(this,store.session))
        when (intent?.action) {
            "START" -> startNap()
            "PAUSE" -> pauseNap()
            "STOP" -> finish()
            "SNOOZE" -> snooze()
            "WAKE" -> if(store.session.phase == Phase.NAPPING && System.currentTimeMillis() >= store.session.end-1000) wake(false) else restore()
            "TEST" -> if(store.session.phase==Phase.IDLE) wake(true)
            "UPDATE" -> { audio.scene = store.scene; updateNotification() }
            else -> restore()
        }
        handler.removeCallbacks(ticker)
        if (store.session.phase != Phase.IDLE) handler.post(ticker)
        return START_NOT_STICKY
    }
    private fun restore() {
        val s = store.session
        when(s.phase) {
            Phase.NAPPING -> if(System.currentTimeMillis() >= s.end) wake(false) else beginAudio(false)
            Phase.WAKING, Phase.TESTING -> if(System.currentTimeMillis() < s.end) beginAudio(true) else finish()
            Phase.PAUSED -> updateNotification()
            else -> finish()
        }
    }
    private fun startNap() {
        val old = store.session
        if (old.phase !in listOf(Phase.IDLE,Phase.PAUSED)) return
        if (!Alarms.allowed(this) || !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            store.prefs.edit().putInt("error",R.string.permission_error).apply(); finish(); return
        }
        val now = System.currentTimeMillis()
        val next = if(old.phase==Phase.PAUSED) old.resume(now) else Session(Phase.NAPPING, now+store.minutes*60000L,token=now)
        store.session = next
        try { Alarms.schedule(this,next) } catch (_: SecurityException) {
            store.prefs.edit().putInt("error",R.string.permission_error).apply(); finish(); return
        }
        getSystemService(NotificationManager::class.java).cancel(Notices.REMINDER)
        beginAudio(false)
        updateNotification()
    }
    private fun pauseNap() {
        if(store.session.phase != Phase.NAPPING) return
        if(System.currentTimeMillis() >= store.session.end) { wake(false); return }
        store.session = store.session.pause(System.currentTimeMillis())
        Alarms.cancel(this)
        val ticket = ++generation
        audio.volume = 0f
        handler.postDelayed({ if(ticket==generation) releaseAudio() },1500)
        updateNotification()
    }
    private fun wake(test: Boolean) {
        generation++
        Alarms.cancel(this)
        val now = System.currentTimeMillis()
        store.session = store.session.copy(phase=if(test) Phase.TESTING else Phase.WAKING,end=now+if(test) 5000 else 600000)
        beginAudio(true)
        if(store.heavy && store.session.phase != Phase.IDLE) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0,600,400,600,1000),0),AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
        }
        updateNotification()
    }
    private fun snooze() {
        val old = store.session
        if(old.phase != Phase.WAKING || old.snoozed) return
        if(!Alarms.allowed(this)) return
        val now = System.currentTimeMillis()
        val next = old.copy(phase=Phase.NAPPING,end=now+120000,snoozed=true,token=now)
        try { Alarms.schedule(this,next) } catch (_: SecurityException) { return }
        store.session = next
        beginAudio(false)
        updateNotification()
    }
    private fun beginAudio(alarm: Boolean) {
        generation++
        releaseAudio()
        // Start foreground first (onStartCommand) before requesting audio focus.
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(if(alarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setOnAudioFocusChangeListener { change ->
                if(change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    if(store.session.phase==Phase.NAPPING) pauseNap() else { focusGain=0f; audio.volume = 0f; vibrator.cancel() }
                } else if(change == AudioManager.AUDIOFOCUS_GAIN) {
                    focusGain=1f
                } else if(change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) focusGain=.2f
            }.build()
        if(manager.requestAudioFocus(focus!!) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            store.prefs.edit().putInt("error",R.string.audio_error).apply(); finish(); return
        }
        lock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"drzemka:audio").apply { acquire(45*60*1000L) }
        audio.scene = store.scene
        audio.volume = if(alarm) store.alarmVolume else store.volume
        focusGain=1f
        val ticket=generation
        audio.start(alarm,store.heavy) { handler.post { if(ticket==generation) {store.prefs.edit().putInt("error",R.string.audio_error).apply(); finish()} } }
    }
    private fun releaseAudio() {
        audio.close()
        vibrator.cancel()
        focus?.let { manager.abandonAudioFocusRequest(it) }; focus=null
        if(lock?.isHeld==true) lock?.release(); lock=null
    }
    private fun updateNotification() {
        val s = store.session
        if(s.phase==Phase.IDLE) return
        getSystemService(NotificationManager::class.java).notify(Notices.SESSION,Notices.session(this,s))
        media.setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,localized(this).getString(R.string.app_name)).putString(MediaMetadata.METADATA_KEY_ARTIST,localized(this).getString(store.scene.label)).build())
        media.setPlaybackState(PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP).setState(if(s.phase==Phase.PAUSED) PlaybackState.STATE_PAUSED else PlaybackState.STATE_PLAYING,0,1f).build())
    }
    private fun finish() {
        generation++
        handler.removeCallbacks(ticker)
        Alarms.cancel(this)
        store.session = Session()
        store.mutedUntil = maxOf(store.mutedUntil,System.currentTimeMillis()+3600000)
        releaseAudio()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    override fun onDestroy() { alive = false; handler.removeCallbacksAndMessages(null); releaseAudio(); media.release(); super.onDestroy() }
    override fun onBind(intent: Intent?) = null
}
