package pl.powernap.studio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF141E33)
private val Cream = Color(0xFFF5F0E6)
private val Orange = Color(0xFFFF914D)
private val Card = Color(0xFF202E47)
private val Muted = Color(0xFFA5B0C3)
private val palette = darkColorScheme(primary=Orange,onPrimary=Navy,background=Navy,onBackground=Cream,surface=Card,onSurface=Cream,surfaceVariant=Card,onSurfaceVariant=Muted,secondary=Cream,onSecondary=Navy,secondaryContainer=Orange.copy(alpha=.18f),onSecondaryContainer=Orange,outline=Muted.copy(alpha=.5f))

class MainActivity : ComponentActivity() {
    private lateinit var controller: NapController
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { controller.refresh() }
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(localized(newBase)) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),navigationBarStyle=SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        controller=NapController(this)
        controller.store.reconcileBoot()
        Notices.channels(this)
        if(controller.store.session.phase!=Phase.IDLE && !NapService.alive) controller.command("RESTORE")
        if(intent.action=="PLAN_NAP") controller.planNap()
        setContent { MaterialTheme(colorScheme=palette) { App(controller,this) } }
    }
    override fun onResume() { super.onResume(); if(::controller.isInitialized) controller.refresh() }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); if(intent.action=="PLAN_NAP") controller.planNap() }
    override fun onDestroy() { controller.close(); super.onDestroy() }
    fun permissions() { if(Build.VERSION.SDK_INT>=33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else notificationSettings() }
    fun notificationSettings() { startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,packageName)) }
    fun alarmSettings() { if(Build.VERSION.SDK_INT>=31) startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:$packageName"))) }
    fun accessSettings() { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
}

@Composable private fun App(controller: NapController, activity: MainActivity) {
    val state by controller.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    val homeRequest by controller.homeRequest.collectAsStateWithLifecycle()
    LaunchedEffect(homeRequest) {tab=0}
    Scaffold(containerColor=Navy,bottomBar={
        NavigationBar(containerColor=Navy,tonalElevation=0.dp) {
            listOf(R.string.home to Icons.Outlined.NightsStay,R.string.sounds to Icons.Outlined.GraphicEq,R.string.settings to Icons.Outlined.Tune).forEachIndexed { i,(label,icon) ->
                NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icon,null)},label={Text(stringResource(label),fontSize=11.sp)},modifier=Modifier.testTag("tab$i"),colors=NavigationBarItemDefaults.colors(indicatorColor=Card,selectedIconColor=Orange,selectedTextColor=Orange))
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when(tab) {
                0 -> Home(state,controller) {tab=1}
                1 -> Sounds(state,controller)
                else -> Preferences(state,controller,activity)
            }
        }
    }
}

@Composable private fun Home(s: ViewState, c: NapController, sounds: ()->Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while(true) { now=System.currentTimeMillis(); delay(1000) } }
    val phase=s.session.phase
    val remaining=s.session.seconds(now,s.minutes)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=26.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment=Alignment.CenterVertically) { Icon(painterResource(R.drawable.ic_sun),null,Modifier.size(40.dp),tint=Color.Unspecified); Text("Drzemka",fontFamily=FontFamily.Serif,fontSize=30.sp) }
        Text(stringResource(R.string.tagline),fontSize=9.sp,letterSpacing=2.sp,color=Muted)
        Spacer(Modifier.height(22.dp))
        Sun(phase)
        val timerText=if(phase==Phase.IDLE) stringResource(R.string.minutes,s.minutes) else String.format(Locale.ROOT,"%02d:%02d",remaining/60,remaining%60)
        Text(timerText,fontSize=64.sp,fontFamily=FontFamily.Serif,modifier=Modifier.testTag("timer").clearAndSetSemantics { contentDescription= "${(remaining+59)/60} min" })
        Text(stringResource(when(phase) { Phase.IDLE->R.string.idle_copy; Phase.NAPPING->R.string.nap_copy; Phase.PAUSED->R.string.paused_copy; else->R.string.wake_copy }),fontSize=13.sp,color=Muted)
        Spacer(Modifier.height(24.dp))
        if(phase==Phase.IDLE) Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.semantics { contentDescription="" }) {
            listOf(10,15,20,30).forEach { min -> FilterChip(selected=s.minutes==min,onClick={c.minutes(min)},label={Text(stringResource(R.string.minutes,min))}) }
        } else if(phase==Phase.NAPPING) Text(stringResource(R.string.wake_at,DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(s.session.end))),color=Muted,fontSize=12.sp)
        Spacer(Modifier.height(22.dp))
        Surface(onClick=sounds,shape=RoundedCornerShape(22.dp),color=Card,modifier=Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                Icon(sceneIcon(s.scene),null,tint=Orange,modifier=Modifier.size(28.dp))
                Column(Modifier.weight(1f).padding(start=14.dp)) { Text(stringResource(R.string.sound_label),fontSize=9.sp,letterSpacing=1.5.sp,color=Muted); Text(stringResource(s.scene.label),fontSize=17.sp) }
                Icon(Icons.Outlined.ChevronRight,null,tint=Muted)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick={c.command(when(phase) { Phase.NAPPING->"PAUSE"; Phase.WAKING,Phase.TESTING->"STOP"; else->"START" })},modifier=Modifier.fillMaxWidth().height(58.dp).testTag("primary"),shape=RoundedCornerShape(30.dp)) {
            Icon(if(phase==Phase.NAPPING) Icons.Outlined.Pause else if(phase==Phase.WAKING) Icons.Outlined.WbSunny else Icons.Outlined.PlayArrow,null)
            Spacer(Modifier.width(10.dp)); Text(stringResource(when(phase) { Phase.IDLE->R.string.start; Phase.PAUSED->R.string.resume; Phase.NAPPING->R.string.pause; else->R.string.awake }),fontWeight=FontWeight.SemiBold)
        }
        if(phase==Phase.WAKING && !s.session.snoozed) TextButton(onClick={c.command("SNOOZE")}) {Text(stringResource(R.string.snooze))}
        else if(phase==Phase.NAPPING || phase==Phase.PAUSED) TextButton(onClick={c.command("STOP")}) {Text(stringResource(R.string.stop),color=Muted)}
        else { Spacer(Modifier.height(12.dp)); Text(stringResource(if(s.heavy) R.string.heavy else R.string.gentle),color=Muted,fontSize=11.sp) }
        if(s.error!=0) Text(stringResource(s.error),color=Orange,fontSize=12.sp,modifier=Modifier.padding(top=12.dp).semantics { liveRegion=LiveRegionMode.Polite })
        if(!s.exact || !s.notifications) Text(stringResource(R.string.permission_error),color=Muted,fontSize=11.sp,modifier=Modifier.padding(top=10.dp))
        Spacer(Modifier.height(24.dp))
    }
}
@Composable private fun Sun(phase: Phase) {
    Canvas(Modifier.size(200.dp).semantics { }) {
        val r=size.width*.32f
        val center=Offset(size.width/2,size.height*.46f)
        drawCircle(Brush.radialGradient(listOf(Orange.copy(alpha=.13f),Color.Transparent),center,r*1.5f),r*1.5f,center)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFC080),Orange),center,r),r,center,alpha=if(phase==Phase.PAUSED) .45f else 1f)
        drawRect(Navy,Offset(0f,size.height*.65f),androidx.compose.ui.geometry.Size(size.width,size.height*.35f))
        drawLine(Cream.copy(alpha=.4f),Offset(size.width*.1f,size.height*.65f),Offset(size.width*.9f,size.height*.65f),1.dp.toPx())
        drawLine(Orange.copy(alpha=.45f),Offset(size.width*.32f,size.height*.72f),Offset(size.width*.68f,size.height*.72f),2.dp.toPx())
        drawLine(Orange.copy(alpha=.2f),Offset(size.width*.4f,size.height*.78f),Offset(size.width*.6f,size.height*.78f),2.dp.toPx())
        listOf(Offset(.12f,.18f),Offset(.88f,.28f),Offset(.76f,.08f)).forEach { drawCircle(Cream.copy(alpha=.6f),2.dp.toPx(),Offset(it.x*size.width,it.y*size.height)) }
    }
}
fun sceneIcon(scene: Scene): ImageVector = when(scene) {
    Scene.RAIN->Icons.Outlined.WaterDrop; Scene.OCEAN->Icons.Outlined.Waves; Scene.FOREST->Icons.Outlined.Forest
    Scene.MEADOW->Icons.Outlined.LocalFlorist; Scene.PLANE->Icons.Outlined.Flight; Scene.TRAIN->Icons.Outlined.Train
    Scene.THUNDER->Icons.Outlined.Thunderstorm; Scene.STORM->Icons.Outlined.Tsunami; Scene.FIRE->Icons.Outlined.LocalFireDepartment
    Scene.WIND->Icons.Outlined.Air; Scene.WHITE->Icons.Outlined.GraphicEq; Scene.BROWN->Icons.Outlined.BlurOn
}
@Composable private fun Sounds(s: ViewState,c: NapController) {
    Column(Modifier.fillMaxSize().padding(horizontal=24.dp)) {
        Title(stringResource(R.string.sounds),stringResource(R.string.sound_intro))
        Text(stringResource(R.string.sound_note),fontSize=12.sp,color=Muted,modifier=Modifier.padding(bottom=16.dp))
        LazyVerticalGrid(columns=GridCells.Fixed(2),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),modifier=Modifier.weight(1f).testTag("soundsGrid")) {
            items(Scene.entries) { scene ->
                val active=s.scene==scene
                Surface(onClick={c.scene(scene)},color=if(active) Orange else Card,shape=RoundedCornerShape(22.dp),modifier=Modifier.heightIn(min=132.dp).semantics { selected=active }) {
                    Column(Modifier.padding(17.dp)) {
                        Icon(sceneIcon(scene),null,tint=if(active) Navy else Cream,modifier=Modifier.size(26.dp))
                        Spacer(Modifier.height(15.dp))
                        Text(stringResource(scene.label),color=if(active) Navy else Cream,fontSize=15.sp,fontWeight=FontWeight.Medium)
                        Text(stringResource(scene.hint),color=if(active) Navy.copy(alpha=.75f) else Muted,fontSize=11.sp,lineHeight=15.sp)
                    }
                }
            }
        }
        Volume(stringResource(R.string.volume),s.volume) {c.store.volume=it}
    }
}
@Composable private fun Title(title: String,subtitle: String) {
    Spacer(Modifier.height(24.dp)); Text(title,fontFamily=FontFamily.Serif,fontSize=32.sp); Text(subtitle,color=Muted,fontSize=13.sp,modifier=Modifier.padding(top=6.dp,bottom=20.dp))
}
@Composable private fun Volume(label: String,value: Float,min: Float=0f,change: (Float)->Unit) {
    Row(Modifier.fillMaxWidth().padding(top=14.dp),horizontalArrangement=Arrangement.SpaceBetween) {Text(label,fontSize=13.sp); Text("${(value*100).toInt()}%",color=Muted,fontSize=13.sp)}
    Slider(value=value,onValueChange=change,valueRange=min..1f,modifier=Modifier.semantics { contentDescription=label })
}
@Composable private fun Panel(title: String,content: @Composable ColumnScope.()->Unit) {
    Surface(color=Card,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().padding(bottom=16.dp)) {
        Column(Modifier.padding(20.dp)) { Text(title,fontSize=19.sp,fontFamily=FontFamily.Serif,modifier=Modifier.padding(bottom=12.dp)); content() }
    }
}
@Composable private fun Preferences(s: ViewState,c: NapController,activity: MainActivity) {
    var consent by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(false) }
    var cleared by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).testTag("settingsScroll")) {
        Title(stringResource(R.string.settings),stringResource(R.string.scroll_intro))
        Panel(stringResource(R.string.language)) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("" to stringResource(R.string.system_language),"pl" to "Polski","en" to "English").forEach { (code,label) -> FilterChip(selected=s.language==code,onClick={c.language(code,activity)},label={Text(label,fontSize=12.sp)}) }
            }
        }
        Panel(stringResource(R.string.wake_title)) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { FilterChip(selected=!s.heavy,onClick={c.store.heavy=false},label={Text(stringResource(R.string.gentle))}); FilterChip(selected=s.heavy,onClick={c.store.heavy=true},label={Text(stringResource(R.string.heavy))}) }
            Text(stringResource(if(s.heavy) R.string.heavy_hint else R.string.gentle_hint),color=Muted,fontSize=12.sp)
            Volume(stringResource(R.string.alarm_volume),s.alarmVolume,.1f) {c.store.alarmVolume=it}
            OutlinedButton(onClick={c.command(if(s.session.phase==Phase.TESTING) "STOP" else "TEST")},enabled=s.session.phase in listOf(Phase.IDLE,Phase.TESTING),modifier=Modifier.fillMaxWidth().testTag("testAlarm")) {Text(stringResource(if(s.session.phase==Phase.TESTING) R.string.stop_test else R.string.test_alarm))}
            Text(stringResource(R.string.system_alarm_volume,s.systemVolume),fontSize=12.sp,modifier=Modifier.padding(top=10.dp))
            Text(stringResource(R.string.alarm_system_note),color=Muted,fontSize=11.sp)
        }
        Panel(stringResource(R.string.scroll_title)) {
            Row(verticalAlignment=Alignment.CenterVertically) { Text(stringResource(R.string.scroll_enabled),modifier=Modifier.weight(1f)); Switch(checked=s.observe,onCheckedChange={if(it) consent=true else c.observe(false)},modifier=Modifier.testTag("observe")) }
            Text(stringResource(R.string.scroll_explanation),fontSize=12.sp,color=Muted)
            TextButton(onClick={apps=true}) {Text(stringResource(R.string.selected_apps)+" (${s.apps.size})")}
            if(s.apps.isEmpty()) Text(stringResource(R.string.no_apps),color=Orange,fontSize=11.sp)
            Text(stringResource(R.string.threshold),fontSize=12.sp,modifier=Modifier.padding(top=12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf(10,20,30).forEach { v -> FilterChip(selected=s.threshold==v,onClick={c.store.threshold=v},label={Text(stringResource(R.string.minutes,v))}) } }
            Row(verticalAlignment=Alignment.CenterVertically) {Text(stringResource(R.string.quiet_hours),fontSize=12.sp,modifier=Modifier.weight(1f)); Switch(checked=s.quiet,onCheckedChange={c.store.quiet=it})}
            Text(stringResource(if(s.accessibility) R.string.accessibility_on else R.string.accessibility_off),color=Muted,fontSize=12.sp)
            TextButton(onClick={if(s.observe) activity.accessSettings() else consent=true}) {Text(stringResource(R.string.open_accessibility))}
            TextButton(onClick={if(!Notices.remind(activity)) activity.permissions()},modifier=Modifier.testTag("previewReminder")) {Text(stringResource(R.string.test_reminder))}
            TextButton(onClick={c.clear(); cleared=true}) {Text(stringResource(R.string.clear_activity))}
            if(cleared) Text(stringResource(R.string.cleared),color=Muted,fontSize=12.sp)
        }
        Panel(stringResource(R.string.notifications)) {
            Text(stringResource(if(s.notifications) R.string.notification_on else R.string.notification_off),color=Muted,fontSize=12.sp)
            TextButton(onClick={if(s.notifications) activity.notificationSettings() else activity.permissions()}) {Text(stringResource(if(s.notifications) R.string.open_notification_settings else R.string.enable_notifications))}
            Text(stringResource(if(s.exact) R.string.exact_on else R.string.exact_off),color=Muted,fontSize=12.sp)
            if(!s.exact) TextButton(onClick={activity.alarmSettings()}) {Text(stringResource(R.string.grant_alarm))}
        }
        Panel(stringResource(R.string.privacy)) { Text(stringResource(R.string.privacy_body),color=Muted,fontSize=12.sp) }
    }
    if(consent) AlertDialog(onDismissRequest={consent=false},title={Text(stringResource(R.string.disclosure_title))},text={Text(stringResource(R.string.disclosure),modifier=Modifier.verticalScroll(rememberScrollState()))},confirmButton={TextButton(onClick={c.observe(true); consent=false; activity.accessSettings()}) {Text(stringResource(R.string.agree))}},dismissButton={TextButton(onClick={consent=false}) {Text(stringResource(R.string.cancel))}})
    if(apps) {
        val installed=remember {c.installedApps()}
        AlertDialog(onDismissRequest={apps=false},title={Text(stringResource(R.string.selected_apps))},text={ Column {Text(stringResource(R.string.apps_note),fontSize=12.sp); LazyColumn(Modifier.heightIn(max=400.dp)) {items(installed) { (pkg,label) -> Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().clickable {c.app(pkg,pkg !in s.apps)}) {Checkbox(checked=pkg in s.apps,onCheckedChange={c.app(pkg,it)}); Text(label,fontSize=14.sp)} } } } },confirmButton={TextButton(onClick={apps=false}) {Text(stringResource(R.string.done))}})
    }
}
