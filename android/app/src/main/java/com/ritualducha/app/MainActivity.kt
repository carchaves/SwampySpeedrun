package com.ritualducha.app

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

// ---------- palette ----------
val Accent = Color(0xFF2E9AA8)
val AccentDim = Color(0xFF9DC0C8)
val Ink = Color(0xFF123F47)
val InkStrong = Color(0xFF0F4D57)
val Muted = Color(0xFF7BA7B1)
val Muted2 = Color(0xFF8BB6BF)
val DelRed = Color(0xFFD5738B)
val CardBg = Color(0xDCFFFFFF)

data class IconDef(val id: String, val label: String, val drawable: Int)

val ICONS = listOf(
    IconDef("ducha", "Ducha", R.drawable.ic_ducha),
    IconDef("banera", "Bañera", R.drawable.ic_banera),
    IconDef("jabon", "Jabón", R.drawable.ic_jabon),
    IconDef("champu", "Champú", R.drawable.ic_champu),
    IconDef("esponja", "Esponja", R.drawable.ic_esponja),
    IconDef("burbujas", "Burbujas", R.drawable.ic_burbujas),
    IconDef("gota", "Agua", R.drawable.ic_gota),
    IconDef("toalla", "Toalla", R.drawable.ic_toalla),
    IconDef("cepillo", "Cepillo dental", R.drawable.ic_cepillo),
    IconDef("afeitar", "Afeitar", R.drawable.ic_afeitar),
    IconDef("peine", "Peine", R.drawable.ic_peine),
    IconDef("crema", "Crema", R.drawable.ic_crema),
    IconDef("espejo", "Espejo", R.drawable.ic_espejo),
    IconDef("patito", "Patito", R.drawable.ic_patito),
    IconDef("reloj", "Tiempo", R.drawable.ic_reloj),
)
fun iconDrawableFor(id: String): Int = ICONS.firstOrNull { it.id == id }?.drawable ?: ICONS[0].drawable

class ActionItem(val id: String, name: String, seconds: Int, icon: String) {
    var name by mutableStateOf(name)
    var seconds by mutableStateOf(seconds)
    var icon by mutableStateOf(icon)
}

fun mmss(secD: Double): String {
    val s = Math.max(0.0, Math.ceil(secD)).toInt()
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}
fun spokenTime(secD: Double): String {
    val s = Math.max(0.0, Math.round(secD).toDouble()).toInt()
    val m = s / 60; val r = s % 60
    val pm = if (m == 1) "1 minuto" else "$m minutos"
    val ps = if (r == 1) "1 segundo" else "$r segundos"
    return if (m > 0 && r > 0) "$pm y $ps" else if (m > 0) pm else ps
}

private const val PREFS = "ritual-ducha-v1"

fun defaultItems(): List<ActionItem> = listOf(
    ActionItem("a1", "Mojarse el cuerpo", 30, "ducha"),
    ActionItem("a2", "Champú en el cabello", 60, "champu"),
    ActionItem("a3", "Enjuagar el cabello", 45, "gota"),
    ActionItem("a4", "Enjabonar el cuerpo", 90, "jabon"),
    ActionItem("a5", "Enjuague final", 45, "burbujas"),
    ActionItem("a6", "Secarse con la toalla", 60, "toalla"),
)

class Routine(val id: String, name: String, items: List<ActionItem>) {
    var name by mutableStateOf(name)
    val items = items.toMutableStateList()
}

fun defaultRoutine(): Routine = Routine("r" + System.currentTimeMillis(), "Aseo Speedrun", defaultItems())

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private val ttsReady = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val r = tts?.setLanguage(Locale("es", "ES"))
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                ttsReady.value = true
            }
        }
        setContent {
            val quicksand = FontFamily(
                Font(R.font.quicksand_medium, FontWeight.Medium),
                Font(R.font.quicksand_bold, FontWeight.Bold),
            )
            MaterialTheme {
                RitualDuchaApp(
                    fontFamily = quicksand,
                    speak = { text ->
                        if (ttsReady.value) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ritual-ducha-utt")
                    },
                    cancelSpeech = { tts?.stop() },
                )
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun RitualDuchaApp(fontFamily: FontFamily, speak: (String) -> Unit, cancelSpeech: () -> Unit) {
    val context = LocalContext.current

    val routines = remember { mutableStateListOf<Routine>().apply { add(defaultRoutine()) } }
    var currentId by remember { mutableStateOf(routines[0].id) }
    var voiceEnabled by remember { mutableStateOf(true) }
    var started by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var idx by remember { mutableStateOf(0) }
    var remainMs by remember { mutableStateOf(0L) }
    var pickerFor by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf("menu") } // "menu" | "edit"
    var editingItemId by remember { mutableStateOf<String?>(null) }

    val current = routines.firstOrNull { it.id == currentId } ?: routines.first()
    val items = current.items
    val routineName = current.name

    // Acceso en vivo a los items de la rutina actual (para el loop/coroutines).
    fun curItems() = (routines.firstOrNull { it.id == currentId } ?: routines.first()).items

    fun serialize(): String {
        val root = JSONObject()
        val rs = JSONArray()
        for (r in routines) {
            val ro = JSONObject()
            ro.put("id", r.id); ro.put("name", r.name)
            val arr = JSONArray()
            for (it in r.items) {
                val j = JSONObject()
                j.put("id", it.id); j.put("name", it.name); j.put("seconds", it.seconds); j.put("icon", it.icon)
                arr.put(j)
            }
            ro.put("items", arr)
            rs.put(ro)
        }
        root.put("routines", rs)
        root.put("currentId", currentId)
        return root.toString()
    }
    fun save() {
        try {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("data", serialize()).apply()
        } catch (e: Exception) { /* ignore */ }
    }

    LaunchedEffect(Unit) {
        try {
            val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = sp.getString("data", null)
            if (raw != null) {
                val o = JSONObject(raw)
                val rs = o.optJSONArray("routines")
                if (rs != null && rs.length() > 0) {
                    // Formato nuevo: varias rutinas.
                    val loadedRoutines = mutableListOf<Routine>()
                    for (i in 0 until rs.length()) {
                        val ro = rs.getJSONObject(i)
                        val arr = ro.optJSONArray("items") ?: JSONArray()
                        val its = mutableListOf<ActionItem>()
                        for (k in 0 until arr.length()) {
                            val it = arr.getJSONObject(k)
                            its.add(ActionItem(it.getString("id"), it.getString("name"), it.getInt("seconds"), it.getString("icon")))
                        }
                        loadedRoutines.add(Routine(ro.getString("id"), ro.optString("name", "Rutina"), its))
                    }
                    routines.clear(); routines.addAll(loadedRoutines)
                    val cid = o.optString("currentId", "")
                    currentId = if (routines.any { it.id == cid }) cid else routines.first().id
                } else {
                    // Formato viejo: una sola rutina (migrar).
                    val arr = o.optJSONArray("items")
                    val its = mutableListOf<ActionItem>()
                    if (arr != null) for (k in 0 until arr.length()) {
                        val it = arr.getJSONObject(k)
                        its.add(ActionItem(it.getString("id"), it.getString("name"), it.getInt("seconds"), it.getString("icon")))
                    }
                    val r = Routine("r" + System.currentTimeMillis(), o.optString("name", "Aseo Speedrun"),
                        if (its.isNotEmpty()) its else defaultItems())
                    routines.clear(); routines.add(r); currentId = r.id
                }
            }
        } catch (e: Exception) {
            routines.clear(); val r = defaultRoutine(); routines.add(r); currentId = r.id
        }
        loaded = true
    }

    var endAt by remember { mutableStateOf(0L) }
    var pausedAt by remember { mutableStateOf(0L) }
    var flagHalf by remember { mutableStateOf(false) }
    var flagFive by remember { mutableStateOf(false) }

    fun totalSeconds(): Double = curItems().sumOf { it.seconds }.toDouble()

    fun startAction(i: Int, announce: Boolean = true) {
        val its = curItems()
        if (i < 0 || i >= its.size) {
            playing = false; finished = true; remainMs = 0
            speak("Rutina completada. ¡Listo!")
            return
        }
        val it = its[i]
        flagHalf = false; flagFive = false
        endAt = System.currentTimeMillis() + it.seconds * 1000L
        idx = i; started = true; playing = true; finished = false
        remainMs = it.seconds * 1000L
        pickerFor = null
        if (announce) speak("${it.name}. ${spokenTime(it.seconds.toDouble())}.")
    }
    fun finishRoutine() {
        playing = false; finished = true; remainMs = 0
        speak("Rutina completada. ¡Listo!")
    }
    fun onPlay() {
        if (!started || finished) {
            if (curItems().isEmpty()) return
            startAction(0)
        } else if (playing) {
            pausedAt = remainMs
            playing = false
            cancelSpeech()
        } else {
            endAt = System.currentTimeMillis() + (if (pausedAt > 0) pausedAt else remainMs)
            playing = true
        }
    }
    fun onSkip() {
        if (!started) return
        cancelSpeech()
        val n = idx + 1
        if (n < curItems().size) startAction(n) else finishRoutine()
    }
    fun onReset() {
        cancelSpeech()
        started = false; playing = false; finished = false; idx = 0; remainMs = 0
    }
    fun toggleVoice() {
        if (voiceEnabled) cancelSpeech()
        voiceEnabled = !voiceEnabled
    }

    // Cambiar de rutina: detiene el reproductor y resetea el estado.
    fun selectRoutine(id: String) {
        if (id == currentId) return
        cancelSpeech()
        started = false; playing = false; finished = false; idx = 0; remainMs = 0
        pickerFor = null
        currentId = id
        save()
    }
    fun addRoutine() {
        val r = Routine("r" + System.currentTimeMillis(), "Nueva rutina", defaultItems())
        routines.add(r)
        selectRoutine(r.id)
    }
    fun deleteRoutine(id: String) {
        if (routines.size <= 1) return
        val wasCurrent = id == currentId
        routines.removeAll { it.id == id }
        if (wasCurrent) {
            cancelSpeech()
            started = false; playing = false; finished = false; idx = 0; remainMs = 0
            pickerFor = null
            currentId = routines.first().id
        }
        save()
    }
    // Abrir la vista de edición de una rutina desde el menú.
    fun openRoutine(id: String) {
        if (id != currentId) selectRoutine(id)
        screen = "edit"
    }
    // Arrancar una rutina directo desde el menú (play a la izquierda).
    fun playRoutine(id: String) {
        if (id != currentId) { currentId = id; save() }
        cancelSpeech()
        started = false; playing = false; finished = false; idx = 0; remainMs = 0; pickerFor = null
        if (curItems().isNotEmpty()) startAction(0)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(150)
            if (!playing) continue
            val its = curItems()
            if (idx >= its.size) { finishRoutine(); continue }
            val it = its[idx]
            val remain = endAt - System.currentTimeMillis()
            val dur = it.seconds * 1000L
            if (remain <= 0) {
                val n = idx + 1
                if (n < its.size) startAction(n) else finishRoutine()
                continue
            }
            if (!flagHalf && remain <= dur / 2 && dur >= 20000) {
                flagHalf = true
                speak("Mitad del tiempo. Quedan ${spokenTime(remain / 1000.0)}.")
            }
            if (!flagFive && remain <= dur * 0.05) {
                flagFive = true
                speak("Quedan ${spokenTime(remain / 1000.0)}.")
            }
            remainMs = remain
        }
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFFF4FCFD), Color(0xFFDDF1F6), Color(0xFFC9E7F0)))

    Box(Modifier.fillMaxSize().background(bgGradient)) {
        BubbleField(Modifier.fillMaxSize())

        if (screen == "menu") {
            MenuScreen(
                fontFamily = fontFamily,
                routines = routines,
                voiceEnabled = voiceEnabled,
                onToggleVoice = ::toggleVoice,
                onOpen = ::openRoutine,
                onPlay = ::playRoutine,
                onAdd = { addRoutine(); screen = "edit" },
                onDelete = ::deleteRoutine,
            )
        } else {
        Column(Modifier.fillMaxSize()) {
            HeaderBar(fontFamily, routineName, { current.name = it; save() }, voiceEnabled, ::toggleVoice, onBack = { screen = "menu" })
            StatsRow(fontFamily, mmss(totalSeconds()), items.size.toString(), if (started) "${idx + 1}/${items.size}" else "—")

            var dragId by remember { mutableStateOf<String?>(null) }
            var dragDy by remember { mutableStateOf(0f) }
            val rowStepPx = with(LocalDensity.current) { 88.dp.toPx() }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 2.dp, bottom = 26.dp)
            ) {
                for (row in items) {
                    val dragging = dragId == row.id
                    ActionRow(
                        fontFamily = fontFamily,
                        item = row,
                        dragging = dragging,
                        dragOffset = if (dragging) dragDy else 0f,
                        onClick = { editingItemId = row.id },
                        dragModifier = Modifier.pointerInput(row.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragId = row.id; dragDy = 0f },
                                onDragEnd = { dragId = null; dragDy = 0f; save() },
                                onDragCancel = { dragId = null; dragDy = 0f; save() },
                                onDrag = { change, amount ->
                                    change.consume()
                                    dragDy += amount.y
                                    val i = items.indexOf(row)
                                    if (i >= 0) {
                                        if (dragDy > rowStepPx && i < items.size - 1) {
                                            val t = items[i]; items[i] = items[i + 1]; items[i + 1] = t; dragDy -= rowStepPx
                                        } else if (dragDy < -rowStepPx && i > 0) {
                                            val t = items[i]; items[i] = items[i - 1]; items[i - 1] = t; dragDy += rowStepPx
                                        }
                                    }
                                },
                            )
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
                AddActionButton(fontFamily) {
                    val nid = "a" + System.currentTimeMillis()
                    items.add(ActionItem(nid, "Nueva acción", 45, "burbujas")); save()
                    editingItemId = nid
                }
                Spacer(Modifier.height(6.dp))
            }

            ControlsBar(
                fontFamily = fontFamily,
                forPlayer = false,
                playing = playing,
                started = started,
                finished = finished,
                onReset = ::onReset,
                onPlay = ::onPlay,
                onSkip = ::onSkip,
            )
        }
        }

        if (started) {
            PlayerOverlay(
                fontFamily = fontFamily,
                items = items,
                idx = idx,
                finished = finished,
                playing = playing,
                remainMs = remainMs,
                totalSeconds = totalSeconds(),
                voiceEnabled = voiceEnabled,
                onToggleVoice = ::toggleVoice,
                onClose = ::onReset,
                onReset = ::onReset,
                onPlay = ::onPlay,
                onSkip = ::onSkip,
            )
        }

        val editing = items.firstOrNull { it.id == editingItemId }
        if (editing != null) {
            ItemEditDialog(
                fontFamily = fontFamily,
                item = editing,
                onSave = { save() },
                onDelete = { items.remove(editing); editingItemId = null; save() },
                onDismiss = { editingItemId = null; save() },
            )
        }
    }
}

@Composable
fun HeaderBar(fontFamily: FontFamily, name: String, onNameChange: (String) -> Unit, voiceEnabled: Boolean, onToggleVoice: () -> Unit, onBack: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color.White).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("‹", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Accent)
            }
        } else {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_ducha), null, tint = Accent, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = InkStrong),
            singleLine = true,
            cursorBrush = SolidColor(Accent),
        )
        Spacer(Modifier.width(10.dp))
        IconTile(icon = if (voiceEnabled) R.drawable.ic_speaker_on else R.drawable.ic_speaker_off, size = 44.dp,
            bg = if (voiceEnabled) Accent else Color.White, tint = if (voiceEnabled) Color.White else AccentDim, onClick = onToggleVoice)
    }
}

@Composable
fun MenuScreen(
    fontFamily: FontFamily,
    routines: List<Routine>,
    voiceEnabled: Boolean,
    onToggleVoice: () -> Unit,
    onOpen: (String) -> Unit,
    onPlay: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // Barra superior
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) { Icon(painterResource(R.drawable.ic_ducha), null, tint = Accent, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(10.dp))
            Text("Aseo Speedrun", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = InkStrong, modifier = Modifier.weight(1f))
            IconTile(icon = if (voiceEnabled) R.drawable.ic_speaker_on else R.drawable.ic_speaker_off, size = 44.dp,
                bg = if (voiceEnabled) Accent else Color.White, tint = if (voiceEnabled) Color.White else AccentDim, onClick = onToggleVoice)
        }
        Text("TUS RUTINAS", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Muted2,
            letterSpacing = 1.6.sp, modifier = Modifier.padding(start = 22.dp, top = 6.dp, bottom = 2.dp))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 6.dp, bottom = 26.dp)
        ) {
            for (r in routines) {
                val count = r.items.size
                val total = mmss(r.items.sumOf { it.seconds }.toDouble())
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x142E9AA8))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBg)
                        .border(1.5.dp, Color(0xF2FFFFFF), RoundedCornerShape(22.dp))
                        .clickable { onOpen(r.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(52.dp).clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(Color(0xFF37B0BF), Color(0xFF1F7F8D))))
                            .clickable { onPlay(r.id) },
                        contentAlignment = Alignment.Center,
                    ) { Text("▶", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.name.ifBlank { "Rutina" }, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = InkStrong, maxLines = 1)
                        Text("$count pasos · $total", fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Muted, modifier = Modifier.padding(top = 2.dp))
                    }
                    if (routines.size > 1) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Color.White)
                                .border(1.dp, Color(0x4DE28E9E), RoundedCornerShape(11.dp)).clickable { onDelete(r.id) },
                            contentAlignment = Alignment.Center,
                        ) { Text("✕", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = DelRed) }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            AddActionButton(fontFamily, "+ Nueva rutina", onAdd)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun IconTile(icon: Int, size: androidx.compose.ui.unit.Dp, bg: Color, tint: Color, onClick: (() -> Unit)? = null, border: Color? = null) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .then(if (border != null) Modifier.border(1.5.dp, border, RoundedCornerShape(14.dp)) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun StatsRow(fontFamily: FontFamily, total: String, count: String, step: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 0.dp).padding(bottom = 12.dp)) {
        StatCell(fontFamily, "DURACIÓN", total, Modifier.weight(1f))
        Spacer(Modifier.width(10.dp))
        StatCell(fontFamily, "ACCIONES", count, Modifier.weight(1f))
        Spacer(Modifier.width(10.dp))
        StatCell(fontFamily, "PASO", step, Modifier.weight(1f))
    }
}

@Composable
fun StatCell(fontFamily: FontFamily, label: String, value: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xD8FFFFFF))
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Muted2, letterSpacing = 1.4.sp)
        Text(value, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Accent)
    }
}

@Composable
fun ActionCard(
    fontFamily: FontFamily,
    item: ActionItem,
    showPicker: Boolean,
    onToggleIconPicker: () -> Unit,
    onNameChange: (String) -> Unit,
    onMinChange: (Int) -> Unit,
    onSecChange: (Int) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x142E9AA8))
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.5.dp, Color(0xF2FFFFFF), RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon = iconDrawableFor(item.icon), size = 52.dp, bg = Color(0xFFF5FCFD), tint = Accent, onClick = onToggleIconPicker)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                BasicTextField(
                    value = item.name, onValueChange = onNameChange,
                    textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkStrong),
                    singleLine = true,
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    NumberBox(fontFamily, item.seconds / 60) { onMinChange(it) }
                    Text(" min", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF9DC0C8))
                    Spacer(Modifier.width(4.dp))
                    NumberBox(fontFamily, item.seconds % 60) { onSecChange(it) }
                    Text(" seg", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF9DC0C8))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column {
                SmallArrowButton(fontFamily, "▲", onUp)
                Spacer(Modifier.height(5.dp))
                SmallArrowButton(fontFamily, "▼", onDown)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(34.dp, 52.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x4DE28E9E), RoundedCornerShape(11.dp))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = DelRed)
            }
        }
    }
}

@Composable
fun NumberBox(fontFamily: FontFamily, value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Box(
        modifier = Modifier
            .width(44.dp).height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White)
            .border(1.dp, Color(0x38ACD2DA), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = text,
            onValueChange = { v ->
                text = v.filter { it.isDigit() }.take(2)
                val n = text.toIntOrNull() ?: 0
                onChange(n.coerceIn(0, 59))
            },
            textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Accent, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Accent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SmallArrowButton(fontFamily: FontFamily, symbol: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp, 24.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White)
            .border(1.dp, Color(0x33ACD2DA), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF5A97A3))
    }
}

@Composable
fun AddActionButton(fontFamily: FontFamily, label: String = "+ Añadir acción", onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x8CFFFFFF))
            .border(2.dp, Accent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Accent)
    }
}

@Composable
fun ActionRow(
    fontFamily: FontFamily,
    item: ActionItem,
    dragging: Boolean,
    dragOffset: Float,
    onClick: () -> Unit,
    dragModifier: Modifier,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .zIndex(if (dragging) 1f else 0f)
            .graphicsLayer { translationY = dragOffset }
            .shadow(if (dragging) 14.dp else 6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x142E9AA8))
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.5.dp, Color(0xF2FFFFFF), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon = iconDrawableFor(item.icon), size = 52.dp, bg = Color(0xFFF5FCFD), tint = Accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name.ifBlank { "Acción" }, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkStrong, maxLines = 1)
            Text(mmss(item.seconds.toDouble()), fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Accent, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            dragModifier
                .size(38.dp, 52.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFFF5FCFD))
                .border(1.dp, Color(0x33ACD2DA), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("⠿", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF5A97A3))
        }
    }
}

@Composable
fun ItemEditDialog(
    fontFamily: FontFamily,
    item: ActionItem,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var startAt by remember { mutableStateOf(0L) }
    LaunchedEffect(running) {
        while (running) {
            elapsedMs = System.currentTimeMillis() - startAt
            delay(100)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Box(Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDismiss() }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("Listo", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = Accent, fontSize = 15.sp)
            }
        },
        dismissButton = {
            Box(Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDelete() }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("Borrar", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = DelRed, fontSize = 15.sp)
            }
        },
        title = { Text("Editar acción", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = InkStrong) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // Título
                Text("TÍTULO", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Muted2, letterSpacing = 1.2.sp)
                Box(
                    Modifier.fillMaxWidth().padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Color(0xFFF2FBFC))
                        .border(1.dp, Color(0x33ACD2DA), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    BasicTextField(
                        value = item.name, onValueChange = { item.name = it; onSave() },
                        textStyle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkStrong),
                        singleLine = true, cursorBrush = SolidColor(Accent), modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Icono
                Spacer(Modifier.height(14.dp))
                Text("ICONO", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Muted2, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(6.dp))
                for (row in ICONS.chunked(5)) {
                    Row(Modifier.padding(vertical = 3.dp)) {
                        for (ic in row) {
                            val sel = ic.id == item.icon
                            Box(Modifier.padding(3.dp)) {
                                IconTile(icon = ic.drawable, size = 46.dp,
                                    bg = if (sel) Accent else Color.White, tint = if (sel) Color.White else Accent,
                                    onClick = { item.icon = ic.id; onSave() }, border = if (sel) null else Color(0x332E9AA8))
                            }
                        }
                    }
                }

                // Duración
                Spacer(Modifier.height(14.dp))
                Text("DURACIÓN", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Muted2, letterSpacing = 1.2.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    NumberBox(fontFamily, item.seconds / 60) { m -> item.seconds = maxOf(5, m * 60 + item.seconds % 60); onSave() }
                    Text(" min", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentDim)
                    Spacer(Modifier.width(6.dp))
                    NumberBox(fontFamily, item.seconds % 60) { s -> item.seconds = maxOf(5, (item.seconds / 60) * 60 + s); onSave() }
                    Text(" seg", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentDim)
                }

                // Cronómetro
                Spacer(Modifier.height(16.dp))
                Text("CRONÓMETRO", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Muted2, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (running) Brush.verticalGradient(listOf(Color(0xFFE28E9E), Color(0xFFC75C72)))
                                else Brush.verticalGradient(listOf(Color(0xFF37B0BF), Color(0xFF1F7F8D))))
                            .clickable {
                                if (!running) { elapsedMs = 0L; startAt = System.currentTimeMillis(); running = true }
                                else {
                                    running = false
                                    item.seconds = maxOf(5, Math.round(elapsedMs / 1000.0).toInt())
                                    onSave()
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (running) "■ Detener y usar" else "⏱ Cronometrar",
                            fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(mmss(elapsedMs / 1000.0), fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Accent)
                }
            }
        },
    )
}

@Composable
fun ControlsBar(
    fontFamily: FontFamily,
    forPlayer: Boolean,
    playing: Boolean,
    started: Boolean,
    finished: Boolean,
    onReset: () -> Unit,
    onPlay: () -> Unit,
    onSkip: () -> Unit,
) {
    val side = if (forPlayer) 56.dp else 50.dp
    val playHeight = if (forPlayer) 68.dp else 62.dp
    val label = if (playing) "❚❚  Pausa" else if (started && !finished) "▶  Continuar" else "▶  Play"

    val rowMod = if (forPlayer) Modifier.fillMaxWidth()
        else Modifier
            .fillMaxWidth()
            .background(Color(0xF0FFFFFF))
            .padding(horizontal = 20.dp, vertical = 14.dp)

    Row(rowMod, verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon = R.drawable.ic_reset, size = side, bg = Color.White, tint = Accent, onClick = onReset)
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier
                .weight(1f)
                .height(playHeight)
                .clip(RoundedCornerShape(if (forPlayer) 22.dp else 20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF37B0BF), Color(0xFF1F7F8D))))
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = if (forPlayer) 19.sp else 18.sp, color = Color.White)
        }
        Spacer(Modifier.width(14.dp))
        IconTile(icon = R.drawable.ic_skip, size = side, bg = Color.White, tint = Accent, onClick = onSkip)
    }
}

@Composable
fun PlayerOverlay(
    fontFamily: FontFamily,
    items: List<ActionItem>,
    idx: Int,
    finished: Boolean,
    playing: Boolean,
    remainMs: Long,
    totalSeconds: Double,
    voiceEnabled: Boolean,
    onToggleVoice: () -> Unit,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onPlay: () -> Unit,
    onSkip: () -> Unit,
) {
    val cur = if (!finished && idx < items.size) items[idx] else null
    val next = if (cur != null) items.getOrNull(idx + 1) else items.getOrNull(0)
    val dur = (cur?.seconds ?: 0) * 1000.0
    val frac = if (cur != null && dur > 0) (remainMs / dur).coerceIn(0.0, 1.0) else if (finished) 0.0 else 1.0
    val animFrac by animateFloatAsState(targetValue = frac.toFloat(), animationSpec = tween(150, easing = LinearEasing), label = "ring")

    val bg = Brush.verticalGradient(listOf(Color(0xFFF7FDFE), Color(0xFFD9EFF5), Color(0xFFBFE1EC)))

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xE6FFFFFF)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Text("✕", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = Accent) }

            Text(
                if (finished) "COMPLETADA" else if (playing) "EN MARCHA" else "EN PAUSA",
                fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Muted,
                letterSpacing = 2.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            IconTile(icon = if (voiceEnabled) R.drawable.ic_speaker_on else R.drawable.ic_speaker_off, size = 44.dp,
                bg = if (voiceEnabled) Accent else Color.White, tint = if (voiceEnabled) Color.White else AccentDim, onClick = onToggleVoice)
        }

        Box(Modifier.size(250.dp).align(Alignment.CenterHorizontally).padding(top = 16.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val strokePx = 15.dp.toPx()
                val inset = strokePx / 2
                drawArc(
                    color = Color(0x26123F47), startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                drawArc(
                    color = if (playing) Accent else AccentDim, startAngle = -90f, sweepAngle = 360f * animFrac, useCenter = false,
                    topLeft = Offset(inset, inset), size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painterResource(iconDrawableFor(cur?.icon ?: "patito")), null, tint = Accent,
                    modifier = Modifier.size(38.dp).padding(bottom = 4.dp),
                )
                Text(
                    if (cur != null) mmss(remainMs / 1000.0) else if (finished) "0:00" else mmss(totalSeconds),
                    fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 48.sp, color = InkStrong,
                )
                Text("restante", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Muted, letterSpacing = 1.sp)
            }
        }

        Text(
            cur?.name ?: "¡Listo, a brillar!",
            fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = InkStrong,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        Text(
            if (cur != null) (if (next != null) "Siguiente: ${next.name}" else "Último paso") else "Rutina terminada",
            fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Muted,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )

        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.Center) {
            items.forEachIndexed { i, it ->
                val color = when {
                    !finished && i == idx -> Accent
                    (i < idx) || finished -> Accent.copy(alpha = 0.5f)
                    else -> Accent.copy(alpha = 0.16f)
                }
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(28.dp, 6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(color)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        ControlsBar(fontFamily, true, playing, true, finished, onReset, onPlay, onSkip)
    }
}

@Composable
fun IconPickerDialog(selected: String, onChoose: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        text = {
            Column {
                Text("Elegir icono", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                val rows = ICONS.chunked(5)
                for (row in rows) {
                    Row(Modifier.padding(vertical = 4.dp)) {
                        for (ic in row) {
                            val isSel = ic.id == selected
                            Box(Modifier.padding(4.dp)) {
                                IconTile(
                                    icon = ic.drawable, size = 48.dp,
                                    bg = if (isSel) Accent else Color.White,
                                    tint = if (isSel) Color.White else Accent,
                                    onClick = { onChoose(ic.id) },
                                    border = if (isSel) null else Color(0x332E9AA8),
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

// ---------- floating background bubbles (matches the original design's
// "rise" + "sway" CSS keyframe animation) ----------

private data class BubbleSpec(val leftFrac: Float, val size: Dp, val riseMs: Int, val delayMs: Int, val swayMs: Int)

private val BUBBLE_SPECS = listOf(
    BubbleSpec(0.08f, 44.dp, 15000, 0, 6000),
    BubbleSpec(0.26f, 20.dp, 11000, 2000, 5000),
    BubbleSpec(0.47f, 32.dp, 18000, 4000, 7000),
    BubbleSpec(0.66f, 14.dp, 9000, 1000, 4000),
    BubbleSpec(0.84f, 52.dp, 20000, 3000, 8000),
)

@Composable
fun BubbleField(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val width = maxWidth
        val height = maxHeight
        for (spec in BUBBLE_SPECS) {
            key(spec) { Bubble(spec, width, height) }
        }
    }
}

@Composable
private fun Bubble(spec: BubbleSpec, containerWidth: Dp, containerHeight: Dp) {
    val transition = rememberInfiniteTransition(label = "bubble")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.riseMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(spec.delayMs),
        ),
        label = "rise",
    )
    val sway by transition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.swayMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sway",
    )

    // CSS baseline was `bottom: -50px` (off-screen below) animated by translateY(-100vh),
    // so in top-relative coordinates the bubble starts a full container-height below
    // the bottom edge and travels up past the top edge.
    val travel = containerHeight + 50.dp
    val y = travel * (1f - progress) - 50.dp
    val scale = 0.7f + 0.45f * progress
    val alpha = if (progress < 0.12f) {
        (progress / 0.12f) * 0.7f
    } else {
        (0.7f * (1f - (progress - 0.12f) / 0.88f)).coerceAtLeast(0f)
    }

    Box(
        Modifier
            .offset(x = containerWidth * spec.leftFrac + sway.dp, y = y)
            .size(spec.size)
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color.White, Color.White.copy(alpha = 0.18f), Color(0xFF78C8D7).copy(alpha = 0.32f)),
                )
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)), CircleShape)
    )
}
