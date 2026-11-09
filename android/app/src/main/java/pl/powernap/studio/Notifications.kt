package pl.powernap.studio

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

object Notices {
    const val SESSION = 41
    const val REMINDER = 42
    fun channels(context: Context) {
        val c = localized(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            Triple("session", R.string.channel_session, NotificationManager.IMPORTANCE_LOW),
            Triple("alarm", R.string.channel_alarm, NotificationManager.IMPORTANCE_HIGH),
            Triple("reminder", R.string.channel_reminder, NotificationManager.IMPORTANCE_DEFAULT)
        ).forEach { (id,label,level) ->
            val channel = NotificationChannel(id,c.getString(label),level)
            if (id != "reminder") { channel.setSound(null,null); channel.enableVibration(false) }
            manager.createNotificationChannel(channel)
        }
    }
    fun open(context: Context, reminder: Boolean = false): PendingIntent = PendingIntent.getActivity(context,if(reminder) 2 else 1,
        Intent(context,MainActivity::class.java).setAction(if(reminder) "PLAN_NAP" else "OPEN").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun action(context: Context, action: String): PendingIntent = PendingIntent.getForegroundService(context,action.hashCode(),Intent(context,NapService::class.java).setAction(action),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun session(context: Context, state: Session): Notification {
        channels(context)
        val c = localized(context)
        val waking = state.phase == Phase.WAKING || state.phase == Phase.TESTING
        val builder = NotificationCompat.Builder(context,if(waking) "alarm" else "session")
            .setSmallIcon(R.drawable.ic_notification).setContentTitle(c.getString(if(waking) R.string.alarm_title else R.string.app_name))
            .setContentText(c.getString(if(waking) R.string.alarm_body else if(state.phase == Phase.PAUSED) R.string.paused_copy else R.string.nap_copy))
            .setContentIntent(open(context)).setOngoing(true).setOnlyAlertOnce(true)
            .setCategory(if(waking) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        if (state.phase == Phase.NAPPING) builder.setWhen(state.end).setUsesChronometer(true).setChronometerCountDown(true)
        if (waking) {
            builder.addAction(0,c.getString(R.string.awake),action(context,"STOP"))
            if (!state.snoozed && state.phase == Phase.WAKING) builder.addAction(0,c.getString(R.string.snooze),action(context,"SNOOZE"))
        } else {
            builder.addAction(0,c.getString(if(state.phase==Phase.PAUSED) R.string.resume else R.string.pause),action(context,if(state.phase==Phase.PAUSED) "START" else "PAUSE"))
            builder.addAction(0,c.getString(R.string.stop),action(context,"STOP"))
        }
        return builder.build()
    }
    fun remindersAllowed(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return context.getSystemService(NotificationManager::class.java).getNotificationChannel("reminder")?.importance != NotificationManager.IMPORTANCE_NONE
    }
    fun remind(context: Context): Boolean {
        channels(context)
        if (!remindersAllowed(context)) return false
        val c = localized(context)
        fun defer(action: String) = PendingIntent.getBroadcast(context,action.hashCode(),Intent(context,ReminderReceiver::class.java).setAction(action),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(context,"reminder").setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(c.getString(R.string.reminder_title)).setContentText(c.getString(R.string.reminder_body))
            .setStyle(NotificationCompat.BigTextStyle().bigText(c.getString(R.string.reminder_body)))
            .setContentIntent(open(context,true)).setAutoCancel(true)
            .addAction(0,c.getString(R.string.nap_15),open(context,true))
            .addAction(0,c.getString(R.string.later),defer("LATER"))
            .addAction(0,c.getString(R.string.not_today),defer("TODAY")).build()
        return runCatching { context.getSystemService(NotificationManager::class.java).notify(REMINDER,n); true }.getOrDefault(false)
    }
}
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Store(context).mutedUntil = if(intent.action=="TODAY") Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR,1); set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }.timeInMillis else System.currentTimeMillis()+3600000
        context.getSystemService(NotificationManager::class.java).cancel(Notices.REMINDER)
    }
}

object Alarms {
    fun allowed(c: Context) = Build.VERSION.SDK_INT < 31 || c.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    private fun pending(c: Context, token: Long = 0) = PendingIntent.getBroadcast(c, 7, Intent(c,AlarmReceiver::class.java).putExtra("token",token),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun schedule(c: Context, s: Session) { c.getSystemService(AlarmManager::class.java).setAlarmClock(AlarmManager.AlarmClockInfo(s.end,Notices.open(c)),pending(c,s.token)) }
    fun cancel(c: Context) { c.getSystemService(AlarmManager::class.java).cancel(pending(c)) }
}
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        val s = Store(context).session
        if (s.phase == Phase.NAPPING && s.token == intent.getLongExtra("token",-1) && System.currentTimeMillis() >= s.end-1000) {
            context.startForegroundService(Intent(context,NapService::class.java).setAction("WAKE"))
        }
    }
}
