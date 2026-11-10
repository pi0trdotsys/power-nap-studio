package pl.powernap.studio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private val context: Context get()=instrumentation.targetContext
    private fun shell(command: String) {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }
    private fun screenshot(language: String,name: String) {
        compose.waitForIdle()
        Thread.sleep(400)
        val bitmap=instrumentation.uiAutomation.takeScreenshot()
        val file=File(context.getExternalFilesDir(null),"screenshots/$language/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        bitmap.recycle()
    }
    @Test fun bilingualScreensAndRealSessionLifecycle() {
        listOf("en","pl").forEach { language ->
            val store=Store(context)
            store.prefs.edit().clear().commit()
            store.language=language
            ActivityScenario.launch(MainActivity::class.java).use {
                compose.onNodeWithTag("primary").assertIsDisplayed()
                screenshot(language,"home")
                compose.onNodeWithTag("tab1").performClick()
                screenshot(language,"sounds")
                compose.onNodeWithText(if(language=="pl") "Samolot" else "Airplane").performClick()
                assertEquals(Scene.PLANE,store.scene)
                compose.onNodeWithTag("tab2").performClick()
                screenshot(language,"settings")
                compose.onNodeWithTag("tab0").performClick()
                compose.onNodeWithTag("primary").performClick()
                compose.waitUntil(5000) {store.session.phase==Phase.NAPPING}
                screenshot(language,"napping")
                compose.onNodeWithTag("primary").performClick()
                compose.waitUntil(5000) {store.session.phase==Phase.PAUSED}
                val seconds=store.session.remaining
                Thread.sleep(1100)
                assertEquals(seconds,store.session.remaining)
                compose.onNodeWithTag("primary").performClick()
                compose.waitUntil(5000) {store.session.phase==Phase.NAPPING}
                val nearEnd=store.session.copy(end=System.currentTimeMillis()+1500)
                store.session=nearEnd
                Alarms.schedule(context,nearEnd)
                compose.waitUntil(7000) {store.session.phase==Phase.WAKING}
                screenshot(language,"waking")
                // Opening the activity has not dismissed the alarm.
                assertEquals(Phase.WAKING,store.session.phase)
                compose.onNodeWithText(if(language=="pl") "Jeszcze 2 minuty" else "2 more minutes").performClick()
                compose.waitUntil(5000) {store.session.phase==Phase.NAPPING}
                assertTrue(store.session.snoozed)
                context.startService(Intent(context,NapService::class.java).setAction("STOP"))
                compose.waitUntil(5000) {store.session.phase==Phase.IDLE}
                assertTrue(Notices.remind(context))
                shell("cmd statusbar expand-notifications")
                val title=localized(context).getString(R.string.reminder_title)
                compose.waitUntil(15000) { instrumentation.uiAutomation.rootInActiveWindow?.findAccessibilityNodeInfosByText(title)?.isNotEmpty()==true }
                Thread.sleep(1500)
                screenshot(language,"notification")
                shell("cmd statusbar collapse")
                ReminderReceiver().onReceive(context,Intent("LATER"))
                assertTrue(store.mutedUntil>System.currentTimeMillis()+3500000)
            }
        }
    }
    @Test fun alarmTestStopsAndSettingsSurviveRecreation() {
        val store=Store(context)
        store.session=Session()
        store.language="en"
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            compose.onNodeWithTag("tab2").performClick()
            compose.onNodeWithTag("testAlarm").performScrollTo().performClick()
            compose.waitUntil(5000) {store.session.phase==Phase.TESTING}
            compose.waitUntil(9000) {store.session.phase==Phase.IDLE}
            store.scene=Scene.FIRE
            store.volume=.25f
            scenario.recreate()
            assertEquals(Scene.FIRE,Store(context).scene)
            assertEquals(.25f,Store(context).volume,.001f)
        }
    }
    @Test fun wakeUpRunsWithScreenOffAndOpeningDoesNotDismiss() {
        val store=Store(context)
        store.session=Session()
        store.language="en"
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithTag("primary").performClick()
            compose.waitUntil(5000) {store.session.phase==Phase.NAPPING}
            val nearEnd=store.session.copy(end=System.currentTimeMillis()+3000)
            store.session=nearEnd
            Alarms.schedule(context,nearEnd)
            instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_SLEEP").close()
            compose.waitUntil(12000) {store.session.phase==Phase.WAKING}
            assertTrue(context.getSystemService(android.app.NotificationManager::class.java).activeNotifications.any { it.id==Notices.SESSION })
            instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_WAKEUP").close()
            instrumentation.uiAutomation.executeShellCommand("wm dismiss-keyguard").close()
            context.startActivity(Intent(context,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            compose.waitForIdle()
            assertEquals(Phase.WAKING,store.session.phase)
            context.startService(Intent(context,NapService::class.java).setAction("STOP"))
            compose.waitUntil(5000) {store.session.phase==Phase.IDLE}
        }
    }
}
