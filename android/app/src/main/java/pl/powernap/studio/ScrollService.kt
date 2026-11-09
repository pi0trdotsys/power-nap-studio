package pl.powernap.studio

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.app.KeyguardManager
import android.os.PowerManager
import java.util.Calendar

class ScrollService : AccessibilityService() {
    private val window = ScrollWindow()
    private var resetAt = 0L
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event==null) return
        val store = Store(this)
        val reset = store.prefs.getLong("resetActivity",0)
        if(reset != resetAt) { window.clear(); resetAt=reset }
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val now = System.currentTimeMillis()
        val allowed = maySuggestBreak(store.observe,store.session.phase,store.quiet,hour,now,store.mutedUntil) && getSystemService(PowerManager::class.java).isInteractive &&
            !getSystemService(KeyguardManager::class.java).isKeyguardLocked
        if(!allowed) { window.clear(); return }
        val packageName = event.packageName?.toString() ?: return
        if(packageName !in store.apps) { if(event.eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) window.clear(); return }
        if(event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return
        if(window.record(now,store.threshold) && Notices.remind(this)) {
            store.mutedUntil=now+3600000
            window.clear()
        }
    }
    override fun onInterrupt() { window.clear() }
}
