package pl.powernap.studio

import android.app.Activity
import android.app.NotificationManager
import android.content.*
import android.media.AudioManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ViewState(
    val session: Session = Session(), val minutes: Int = 15, val scene: Scene = Scene.RAIN,
    val volume: Float = .5f, val alarmVolume: Float = .9f, val heavy: Boolean = true,
    val language: String = "", val observe: Boolean = false, val apps: Set<String> = emptySet(),
    val threshold: Int = 20, val quiet: Boolean = true, val notifications: Boolean = false,
    val exact: Boolean = false, val accessibility: Boolean = false, val systemVolume: Int = 0,
    val error: Int = 0,
)
class NapController(private val context: Context) {
    val store = Store(context)
    private val mutable = MutableStateFlow(ViewState())
    val state = mutable.asStateFlow()
    private val homeRequests = MutableStateFlow(0)
    val homeRequest = homeRequests.asStateFlow()
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }
    init { store.prefs.registerOnSharedPreferenceChangeListener(listener); refresh() }
    fun close() { store.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    fun refresh() {
        val audio = context.getSystemService(AudioManager::class.java)
        val access = context.getSystemService(AccessibilityManager::class.java).getEnabledAccessibilityServiceList(-1)
            .any { it.resolveInfo.serviceInfo.packageName==context.packageName }
        mutable.value = ViewState(store.session,store.minutes,store.scene,store.volume,store.alarmVolume,store.heavy,store.language,store.observe,store.apps,store.threshold,store.quiet,
            NotificationManagerCompat.from(context).areNotificationsEnabled(),Alarms.allowed(context),access,
            audio.getStreamVolume(AudioManager.STREAM_ALARM)*100/audio.getStreamMaxVolume(AudioManager.STREAM_ALARM).coerceAtLeast(1),store.prefs.getInt("error",0))
    }
    fun command(action: String) {
        store.prefs.edit().remove("error").apply()
        if(action=="START" && (!state.value.exact || !state.value.notifications)) {
            store.prefs.edit().putInt("error",R.string.permission_error).apply(); return
        }
        runCatching { context.startForegroundService(Intent(context,NapService::class.java).setAction(action)) }
            .onFailure { store.prefs.edit().putInt("error",R.string.audio_error).apply() }
    }
    fun minutes(value: Int) { if(store.session.phase==Phase.IDLE) store.minutes=value }
    fun scene(value: Scene) { store.scene=value; if(store.session.phase==Phase.NAPPING) command("UPDATE") }
    fun observe(value: Boolean) { store.observe=value; if(!value) context.getSystemService(NotificationManager::class.java).cancel(Notices.REMINDER) }
    fun app(packageName: String, enabled: Boolean) { store.apps=if(enabled) store.apps+packageName else store.apps-packageName }
    fun clear() { store.clearActivity() }
    fun planNap() {
        if(store.session.phase==Phase.IDLE) store.minutes=15
        store.mutedUntil=System.currentTimeMillis()+3600000
        context.getSystemService(NotificationManager::class.java).cancel(Notices.REMINDER)
        homeRequests.value++
    }
    fun language(value: String, activity: Activity) { store.language=value; activity.recreate() }
    fun installedApps(): List<Pair<String,String>> = context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)
        .filter { it.activityInfo.packageName!=context.packageName }
        .map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }.distinctBy { it.first }.sortedBy { it.second.lowercase() }
}
