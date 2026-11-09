package pl.powernap.studio

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.provider.Settings
import java.util.Locale

class Store(private val context: Context) {
    val prefs = context.applicationContext.getSharedPreferences("drzemka", Context.MODE_PRIVATE)
    var minutes: Int
        get() = prefs.getInt("minutes", 15).takeIf { it in listOf(10,15,20,30) } ?: 15
        set(v) { prefs.edit().putInt("minutes", v).apply() }
    var scene: Scene
        get() = runCatching { Scene.valueOf(prefs.getString("scene", "RAIN")!!) }.getOrDefault(Scene.RAIN)
        set(v) { prefs.edit().putString("scene", v.name).apply() }
    var volume: Float
        get() = prefs.getFloat("volume", .5f).coerceIn(0f, 1f)
        set(v) { prefs.edit().putFloat("volume", v.coerceIn(0f,1f)).apply() }
    var alarmVolume: Float
        get() = prefs.getFloat("alarmVolume", .9f).coerceIn(.1f, 1f)
        set(v) { prefs.edit().putFloat("alarmVolume", v.coerceIn(.1f,1f)).apply() }
    var heavy: Boolean
        get() = prefs.getBoolean("heavy", true)
        set(v) { prefs.edit().putBoolean("heavy", v).apply() }
    var language: String
        get() = prefs.getString("language", "") ?: ""
        set(v) { prefs.edit().putString("language", v).commit() }
    var observe: Boolean
        get() = prefs.getBoolean("observe", false)
        set(v) { prefs.edit().putBoolean("observe", v).putLong("resetActivity", System.currentTimeMillis()).apply() }
    var apps: Set<String>
        get() = prefs.getStringSet("apps", emptySet())!!.toSet()
        set(v) { prefs.edit().putStringSet("apps", v.toSet()).putLong("resetActivity", System.currentTimeMillis()).apply() }
    var threshold: Int
        get() = prefs.getInt("threshold", 20).coerceIn(10,30)
        set(v) { prefs.edit().putInt("threshold", v).apply() }
    var quiet: Boolean
        get() = prefs.getBoolean("quiet", true)
        set(v) { prefs.edit().putBoolean("quiet", v).apply() }
    var mutedUntil: Long
        get() = prefs.getLong("mutedUntil", 0)
        set(v) { prefs.edit().putLong("mutedUntil", v).apply() }
    var session: Session
        get() = Session(
            runCatching { Phase.valueOf(prefs.getString("phase", "IDLE")!!) }.getOrDefault(Phase.IDLE),
            prefs.getLong("end", 0), prefs.getLong("remaining", 0), prefs.getBoolean("snoozed", false), prefs.getLong("token", 0))
        set(s) { prefs.edit().putString("phase", s.phase.name).putLong("end",s.end).putLong("remaining",s.remaining).putBoolean("snoozed",s.snoozed).putLong("token",s.token).putInt("boot",Settings.Global.getInt(context.contentResolver,Settings.Global.BOOT_COUNT,0)).commit() }
    fun reconcileBoot() {
        if(session.phase != Phase.IDLE && prefs.getInt("boot",-1)!=Settings.Global.getInt(context.contentResolver,Settings.Global.BOOT_COUNT,0)) {
            session=Session()
            prefs.edit().putInt("error",R.string.session_interrupted).apply()
        }
    }
    fun clearActivity() { prefs.edit().putLong("resetActivity", System.currentTimeMillis()).apply() }
}

fun localized(context: Context): Context {
    val language = Store(context).language
    if (language.isEmpty()) return context
    val config = Configuration(context.resources.configuration)
    config.setLocales(LocaleList(Locale.forLanguageTag(language)))
    return context.createConfigurationContext(config)
}
