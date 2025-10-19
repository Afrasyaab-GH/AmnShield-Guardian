package com.deenshield.blocker.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.deenshield.blocker.model.Block
import com.deenshield.blocker.viewmodel.BlockViewModel
import com.deenshield.blocker.ui.components.ToggleItem
import com.deenshield.blocker.util.BlockUtils
import com.deenshield.blocker.service.BlockingVpnService
import java.util.UUID
import android.app.TimePickerDialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import com.deenshield.blocker.data.UserPrefs
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun BlocksScreen(vm: BlockViewModel) {
    val blocks = vm.blocks
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Blocks", style = MaterialTheme.typography.titleMedium)
        if (blocks.isEmpty()) {
            EmptyState(title = "No blocks yet", subtitle = "Tap + to create your first block.")
        }
        blocks.forEach { b ->
            Card(Modifier.padding(bottom = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(b.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Apps: ${'$'}{b.appIds.size} • Websites: ${'$'}{b.websites.size} • Keywords: ${'$'}{b.keywords.size}")
                }
            }
        }
    }
}

@Composable
private fun ServiceControls() {
    val ctx = LocalContext.current
    var vpnActive by remember { mutableStateOf(false) }
    var accessibilityActive by remember { mutableStateOf(false) }
    
    // Check service statuses
    LaunchedEffect(Unit) {
        while (true) {
            vpnActive = isVpnServiceActive(ctx)
            accessibilityActive = isAccessibilityServiceEnabled(ctx)
            kotlinx.coroutines.delay(2000) // Check every 2 seconds
        }
    }
    
    val vpnConsentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            // Start VPN service after consent
            val intent = Intent(ctx, BlockingVpnService::class.java).apply { action = BlockingVpnService.ACTION_START }
            ctx.startService(intent)
        }
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Protection Services", style = MaterialTheme.typography.titleMedium)
        
        // VPN Service Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (vpnActive) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text("VPN Protection", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (vpnActive) "Active - Filtering network traffic" else "Inactive",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (vpnActive) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Icon(
                        painter = painterResource(
                            if (vpnActive) android.R.drawable.presence_online 
                            else android.R.drawable.presence_offline
                        ),
                        contentDescription = null,
                        tint = if (vpnActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!vpnActive) {
                        Button(onClick = {
                            val prepare = VpnService.prepare(ctx)
                            if (prepare != null) {
                                vpnConsentLauncher.launch(prepare)
                            } else {
                                val intent = Intent(ctx, BlockingVpnService::class.java).apply { 
                                    action = BlockingVpnService.ACTION_START 
                                }
                                ctx.startService(intent)
                            }
                        }) { Text("Enable VPN") }
                    } else {
                        OutlinedButton(onClick = {
                            val intent = Intent(ctx, BlockingVpnService::class.java).apply { 
                                action = BlockingVpnService.ACTION_STOP 
                            }
                            ctx.startService(intent)
                        }) { Text("Disable VPN") }
                    }
                }
            }
        }
        
        // Accessibility Service Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (accessibilityActive) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text("App Blocking", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (accessibilityActive) "Active - Blocking apps" else "Inactive - Enable to block apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (accessibilityActive) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Icon(
                        painter = painterResource(
                            if (accessibilityActive) android.R.drawable.presence_online 
                            else android.R.drawable.presence_offline
                        ),
                        contentDescription = null,
                        tint = if (accessibilityActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                
                if (!accessibilityActive) {
                    OutlinedButton(onClick = {
                        val i = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(i)
                    }) { Text("Open Accessibility Settings") }
                }
            }
        }
    }
}

// Helper functions to check service status
private fun isVpnServiceActive(ctx: android.content.Context): Boolean {
    // Check if VPN is connected
    val connectivityManager = ctx.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) 
        as android.net.ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)
}

private fun isAccessibilityServiceEnabled(ctx: android.content.Context): Boolean {
    val expectedComponentName = "com.deenshield.blocker/com.deenshield.blocker.service.AccessibilityBlocker"
    val enabledServices = Settings.Secure.getString(
        ctx.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(expectedComponentName)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun AddBlockScreen(vm: BlockViewModel, nav: NavController) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf("") }
    var full by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(listOf<String>()) }
    var websites by remember { mutableStateOf(listOf<String>()) }
    var keywords by remember { mutableStateOf(listOf<String>()) }
    var weekdaySelected by remember { mutableStateOf(BooleanArray(7) { false }) } // Mon..Sun
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(17) }
    var endMinute by remember { mutableStateOf(0) }
    var schedule by remember { mutableStateOf(mutableMapOf<Int, MutableList<IntRange>>()) }
    var dailyLimit by remember { mutableStateOf(0) }
    var hourlyLimit by remember { mutableStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var webError by remember { mutableStateOf<String?>(null) }
    var kwError by remember { mutableStateOf<String?>(null) }

    // Handle result from WeeklyScheduleScreen
    val backStackEntry = nav.currentBackStackEntryAsState().value
    LaunchedEffect(backStackEntry?.savedStateHandle?.get<String>("weekly_result")) {
        val encoded = backStackEntry?.savedStateHandle?.get<String>("weekly_result")
        if (encoded != null) {
            val weekly = decodeWeeklyFromEncoded(encoded)
            schedule = weeklyMapToSchedule(weekly)
            backStackEntry?.savedStateHandle?.remove<String>("weekly_result")
        }
    }

    fun addApp(pkg: String) {
        val id = pkg.trim()
        if (id.isNotEmpty() && !apps.contains(id)) apps = apps + id
    }
    fun addWebsite(input: String) {
        val d = BlockUtils.normalizeDomain(input)
        if (d.isNotEmpty() && !websites.contains(d)) websites = websites + d
    }
    fun addKeyword(kw: String) {
        val k = kw.trim().lowercase()
        if (k.length >= 3 && !keywords.contains(k)) keywords = keywords + k
    }
    fun toggleDay(index: Int) {
        val arr = weekdaySelected.copyOf(); arr[index] = !arr[index]; weekdaySelected = arr
    }
    fun addIntervalToSelectedDays() {
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        if (end <= start) { error = "End must be after start"; return }
        for (i in 0..6) {
            if (weekdaySelected[i]) {
                val day = i + 1 // 1..7
                val list = schedule.getOrPut(day) { mutableListOf() }
                list.add(IntRange(start, end))
            }
        }
        // clear selection
        weekdaySelected = BooleanArray(7) { false }
    }

    var appInput by remember { mutableStateOf("") }
    var webInput by remember { mutableStateOf("") }
    var kwInput by remember { mutableStateOf("") }

    // Preload installed apps for suggestions
    val pm = ctx.packageManager
    val installed = remember {
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { it.loadLabel(pm).toString() }
    }

    fun isValidDomain(s: String): Boolean {
        val d = BlockUtils.normalizeDomain(s)
        val regex = Regex("^([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$")
        return d.isNotEmpty() && regex.matches(d)
    }

    fun addWebsitesBulk(input: String) {
        webError = null
        val tokens = input.split(',', ';', '\n', '\t', ' ').map { it.trim() }.filter { it.isNotEmpty() }
        val added = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        tokens.forEach { t ->
            val d = BlockUtils.normalizeDomain(t)
            if (isValidDomain(d) && !websites.contains(d)) added += d else skipped += t
        }
        if (added.isNotEmpty()) websites = websites + added
        if (skipped.isNotEmpty()) webError = "Skipped invalid: ${skipped.joinToString(", ")}"
    }

    fun pasteKeywordsFromClipboard() {
        val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString()?.trim()
        if (!text.isNullOrEmpty()) {
            val tokens = text.split(',', ';', '\n', '\t').map { it.trim().lowercase() }.filter { it.length >= 3 }
            tokens.forEach { addKeyword(it) }
        }
    }

    fun copyKeywordsToClipboard() {
        val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("keywords", keywords.joinToString(","))
        cm.setPrimaryClip(clip)
    }

    fun saveBlock() {
        error = null
        if (name.isBlank()) { error = "Name required"; return }
        if (apps.isEmpty() && websites.isEmpty() && keywords.isEmpty()) {
            error = "Add at least one target (app, website, or keyword)"; return
        }
        saving = true
        val scheduleMap: Map<Int, List<IntRange>> = schedule.mapValues { it.value.toList() }
        val block = Block(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            appIds = apps,
            websites = websites,
            keywords = keywords,
            fullBlock = full,
            weeklySchedule = scheduleMap,
            dailyLimitMinutes = dailyLimit,
            hourlyLimitMinutes = hourlyLimit
        )
        vm.addBlock(block)
        // Clear form after save
        name = ""; full = false; apps = emptyList(); websites = emptyList(); keywords = emptyList()
        weekdaySelected = BooleanArray(7) { false }; schedule.clear(); dailyLimit = 0; hourlyLimit = 0
        saving = false
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(tonalElevation = 3.dp) {
                Button(
                    onClick = { saveBlock() },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    if (saving) CircularProgressIndicator() else Text("Save Block")
                }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Add Block", style = MaterialTheme.typography.titleLarge)
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                BlockSectionCard(title = "Details") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        supportingText = { Text("Pick a clear name for this block") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row { Checkbox(checked = full, onCheckedChange = { full = it }); Text("Full Block") }
                }
            }

            item {
                var showAppPicker by remember { mutableStateOf(false) }
                BlockSectionCard(title = "Apps") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = appInput,
                            onValueChange = { appInput = it },
                            label = { Text("Package name (e.g., com.instagram.android)") },
                            supportingText = { Text("Type to search installed apps below") },
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(onClick = { addApp(appInput); appInput = "" }) { Text("Add") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAppPicker = true }) { Text("Pick from installed") }
                    }
                    // Suggestions from installed apps
                    val suggestions = installed.filter {
                        appInput.isNotBlank() && (it.packageName.contains(appInput, true) || it.loadLabel(pm).toString().contains(appInput, true))
                    }.take(5)
                    if (suggestions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            suggestions.forEach { app ->
                                val label = app.loadLabel(pm).toString()
                                val pkg = app.packageName
                                Row(
                                    Modifier.fillMaxWidth().clickable { addApp(pkg); appInput = "" },
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$label ($pkg)")
                                    OutlinedButton(onClick = { addApp(pkg); appInput = "" }) { Text("Add") }
                                }
                            }
                        }
                    }
                    ChipsFlow(items = apps, onRemove = { rm -> apps = apps.filterNot { it == rm } })
                    if (showAppPicker) {
                        InstalledAppsPickerDialog(
                            onDismiss = { showAppPicker = false },
                            onConfirm = { selected ->
                                val merged = (apps + selected).distinct()
                                apps = merged
                                showAppPicker = false
                            }
                        )
                    }
                }
            }

            item {
                BlockSectionCard(title = "Websites") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = webInput,
                            onValueChange = { webInput = it; webError = null },
                            label = { Text("Domain or URL (e.g., example.com)") },
                            supportingText = { Text(webError ?: "You can paste multiple, separated by commas") },
                            isError = webError != null,
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(onClick = {
                            addWebsitesBulk(webInput)
                            webInput = ""
                        }) { Text("Add") }
                    }
                    ChipsFlow(items = websites, onRemove = { rm -> websites = websites.filterNot { it == rm } })
                }
            }

            item {
                BlockSectionCard(title = "Keywords") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = kwInput,
                            onValueChange = { kwInput = it; kwError = if (it.isNotEmpty() && it.length < 3) "Min 3 chars" else null },
                            label = { Text("Keyword") },
                            supportingText = { Text(kwError ?: "Enter sensitive terms to block") },
                            isError = kwError != null,
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(onClick = { if (kwInput.length >= 3) { addKeyword(kwInput); kwInput = "" } }) { Text("Add") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pasteKeywordsFromClipboard() }) { Text("Paste CSV") }
                        OutlinedButton(onClick = { copyKeywordsToClipboard() }) { Text("Copy CSV") }
                    }
                    ChipsFlow(items = keywords, onRemove = { rm -> keywords = keywords.filterNot { it == rm } })
                }
            }

            item {
                BlockSectionCard(title = "Weekly Schedule") {
                    Text("Select days, then set start/end time, and Add Interval", style = MaterialTheme.typography.bodyMedium)
                    DaySelector(weekdaySelected, ::toggleDay)
                    TimePickers(
                        startHour, startMinute, onStart = { h, m -> startHour = h; startMinute = m },
                        endHour, endMinute, onEnd = { h, m -> endHour = h; endMinute = m }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { addIntervalToSelectedDays() }) { Text("Add Interval") }
                        OutlinedButton(onClick = { schedule.clear() }) { Text("Clear All") }
                        OutlinedButton(onClick = {
                            // Navigate to full WeeklyScheduleScreen with current schedule mapped to DayOfWeek/LocalTime pairs
                            val weekly = scheduleToWeeklyMap(schedule)
                            val encoded = encodeWeeklyToEncoded(weekly)
                            nav.currentBackStackEntry?.savedStateHandle?.set("weekly_initial", encoded)
                            nav.navigate("weeklySchedule")
                        }) { Text("Open Full Scheduler") }
                    }
                    ScheduleList(schedule = schedule, onRemove = { day, idx ->
                        val list = schedule[day] ?: return@ScheduleList
                        if (idx in list.indices) list.removeAt(idx)
                        if (list.isEmpty()) schedule.remove(day)
                    })
                }
            }

            item {
                BlockSectionCard(title = "Usage Limits (minutes)") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var dailyInput by remember { mutableStateOf("") }
                        var hourlyInput by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = if (dailyInput.isEmpty()) dailyLimit.toString() else dailyInput,
                            onValueChange = { dailyInput = it; dailyLimit = it.toIntOrNull() ?: 0 },
                            label = { Text("Daily") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = if (hourlyInput.isEmpty()) hourlyLimit.toString() else hourlyInput,
                            onValueChange = { hourlyInput = it; hourlyLimit = it.toIntOrNull() ?: 0 },
                            label = { Text("Hourly") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// Helpers to convert between legacy map<Int, List<IntRange>> and desired Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>
private fun scheduleToWeeklyMap(src: Map<Int, MutableList<IntRange>>): Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>> =
    src.entries.associate { (day, ranges) ->
        val dow = DayOfWeek.of(if (day in 1..7) day else 1)
        val pairs = ranges.map { r -> LocalTime.of(r.first / 60, r.first % 60) to LocalTime.of(r.last / 60, r.last % 60) }
        dow to pairs
    }

private fun weeklyMapToSchedule(src: Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>): MutableMap<Int, MutableList<IntRange>> =
    src.entries.associate { (dow, pairs) ->
        val day = dow.value
        val ranges = pairs.map { (s, e) -> IntRange(s.hour * 60 + s.minute, e.hour * 60 + e.minute) }.toMutableList()
        day to ranges
    }.toMutableMap()

// Encoding helpers (simple, safe for SavedStateHandle): Mon=08:00-16:00;Tue=... using ISO times
fun encodeWeeklyToEncoded(map: Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>): String =
    map.entries.sortedBy { it.key.value }.joinToString("|") { (d, list) ->
        val v = list.joinToString(",") { (s, e) -> "%02d:%02d-%02d:%02d".format(s.hour, s.minute, e.hour, e.minute) }
        "${d.name}=$v"
    }

fun decodeWeeklyFromEncoded(encoded: String?): Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>> {
    if (encoded.isNullOrBlank()) return DayOfWeek.values().associateWith { emptyList() }
    return encoded.split('|').mapNotNull { part ->
        val idx = part.indexOf('='); if (idx <= 0) return@mapNotNull null
        val day = runCatching { DayOfWeek.valueOf(part.substring(0, idx)) }.getOrNull() ?: return@mapNotNull null
        val values = part.substring(idx + 1)
        val ranges = if (values.isBlank()) emptyList() else values.split(',').mapNotNull { r ->
            val dash = r.indexOf('-'); if (dash <= 0) return@mapNotNull null
            val s = r.substring(0, dash)
            val e = r.substring(dash + 1)
            val (sh, sm) = s.split(':').mapNotNull { it.toIntOrNull() }.let { if (it.size == 2) it[0] to it[1] else return@mapNotNull null }
            val (eh, em) = e.split(':').mapNotNull { it.toIntOrNull() }.let { if (it.size == 2) it[0] to it[1] else return@mapNotNull null }
            LocalTime.of(sh, sm) to LocalTime.of(eh, em)
        }
        day to ranges
    }.toMap().let { map ->
        // Ensure all days present
        DayOfWeek.values().associateWith { d -> map[d] ?: emptyList() }
    }
}

@Composable
private fun BlockSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsFlow(items: List<String>, onRemove: (String) -> Unit) {
    if (items.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { s ->
            AssistChip(
                onClick = { },
                label = { Text(s) },
                trailingIcon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Remove"
                    )
                },
                modifier = Modifier.clickable { onRemove(s) }
            )
        }
    }
}

@Composable
private fun InstalledAppsPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(mutableSetOf<String>()) }
    val apps = remember {
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { it.loadLabel(pm).toString() }
    }
    val filtered = apps.filter { 
        val label = it.loadLabel(pm).toString()
        val pkg = it.packageName
        search.isBlank() || label.contains(search, true) || pkg.contains(search, true) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick Apps to Block") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = search, 
                    onValueChange = { search = it }, 
                    label = { Text("Search apps") }, 
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("${selected.size} apps selected", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                LazyColumn(Modifier.fillMaxWidth().height(400.dp)) {
                    items(filtered) { app ->
                        val label = app.loadLabel(pm).toString()
                        val pkg = app.packageName
                        val isSelected = selected.contains(pkg)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (isSelected) selected.remove(pkg) else selected.add(pkg)
                                },
                            elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(label, style = MaterialTheme.typography.bodyLarge)
                                    Text(pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Checkbox(
                                    checked = isSelected, 
                                    onCheckedChange = {
                                        if (it) selected.add(pkg) else selected.remove(pkg)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected.toList()) }) { 
                Text("Add ${selected.size} App${if (selected.size != 1) "s" else ""}") 
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ChipList(items: List<String>, onRemove: (String) -> Unit) {
    if (items.isEmpty()) return
    LazyColumn {
        items(items) { s ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(s)
                OutlinedButton(onClick = { onRemove(s) }) { Text("Remove") }
            }
        }
    }
}

@Composable
private fun DaySelector(selected: BooleanArray, onToggle: (Int) -> Unit) {
    val labels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = selected[index],
                onClick = { onToggle(index) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun TimePickers(
    startH: Int, startM: Int, onStart: (Int, Int) -> Unit,
    endH: Int, endM: Int, onEnd: (Int, Int) -> Unit
) {
    val ctx = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            TimePickerDialog(ctx, { _, h, m -> onStart(h, m) }, startH, startM, true).show()
        }) { Text("Start: %02d:%02d".format(startH, startM)) }
        OutlinedButton(onClick = {
            TimePickerDialog(ctx, { _, h, m -> onEnd(h, m) }, endH, endM, true).show()
        }) { Text("End: %02d:%02d".format(endH, endM)) }
    }
}

@Composable
private fun ScheduleList(
    schedule: Map<Int, List<IntRange>>,
    onRemove: (day: Int, index: Int) -> Unit
) {
    if (schedule.isEmpty()) return
    val dayNames = mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        schedule.entries.sortedBy { it.key }.forEach { (day, ranges) ->
            Card {
                Column(Modifier.padding(8.dp)) {
                    Text(dayNames[day] ?: day.toString(), style = MaterialTheme.typography.titleSmall)
                    ranges.forEachIndexed { idx, r ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            fun fmt(m: Int) = "%02d:%02d".format(m / 60, m % 60)
                            Text("${fmt(r.first)} - ${fmt(r.last)}")
                            OutlinedButton(onClick = { onRemove(day, idx) }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockingOverlayDialog(onDismiss: () -> Unit, reason: String) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Blocked") },
        text = { Text(reason) },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors()) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsScreen(vm: BlockViewModel) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var blockedApps by remember { mutableStateOf(setOf<String>()) }
    var showAppsDialog by remember { mutableStateOf(false) }

    // Load blocked apps
    LaunchedEffect(Unit) {
        UserPrefs.blockedAppsFlow(ctx).collectLatest { blockedApps = it }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.titleLarge)
        }
        
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        
        item {
            ServiceControls()
        }
        
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text("Global Settings", style = MaterialTheme.typography.titleMedium)
        }
        
        item {
            ToggleItem(
                title = "Enable blocking globally",
                checked = vm.globalEnabled,
                onChange = { vm.updateGlobalEnabled(it) }
            )
        }
        
        item {
            ToggleItem(
                title = "Block harmful keywords",
                checked = vm.blockHarmfulKeywords,
                onChange = { vm.updateBlockHarmfulKeywords(it) }
            )
        }
        
        item {
            ToggleItem(
                title = "Block harmful websites",
                checked = vm.blockHarmfulWebsites,
                onChange = { vm.updateBlockHarmfulWebsites(it) }
            )
        }
        
        item {
            ToggleItem(
                title = "Block social media",
                checked = vm.blockSocialMedia,
                onChange = { vm.updateBlockSocialMedia(it) }
            )
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text("Blocked Apps (${blockedApps.size})", style = MaterialTheme.typography.titleMedium)
        }
        
        item {
            Button(
                onClick = { showAppsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("+ Add Blocked Apps") 
            }
        }
        
        items(blockedApps.toList()) { pkg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = pkg,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { scope.launch { UserPrefs.removeBlockedApp(ctx, pkg) } }
                    ) { 
                        Text("Remove") 
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showAppsDialog) {
        InstalledAppsDialog(ctx = ctx, onDismiss = { showAppsDialog = false })
    }
}

@Composable
private fun InstalledAppsDialog(
    ctx: android.content.Context,
    onDismiss: () -> Unit
) {
    val pm = ctx.packageManager
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(mutableSetOf<String>()) }
    val apps = remember {
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { it.loadLabel(pm).toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Apps to Block") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search apps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("${selected.size} apps selected", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                val filtered = apps.filter { 
                    val label = it.loadLabel(pm).toString()
                    search.isBlank() || label.contains(search, ignoreCase = true) || it.packageName.contains(search, ignoreCase = true)
                }
                LazyColumn(Modifier.fillMaxWidth().height(400.dp)) {
                    items(filtered) { app ->
                        val label = app.loadLabel(pm).toString()
                        val pkg = app.packageName
                        val isSelected = selected.contains(pkg)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (isSelected) selected.remove(pkg) else selected.add(pkg)
                                },
                            elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                    Text(pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Checkbox(checked = isSelected, onCheckedChange = {
                                    if (it) selected.add(pkg) else selected.remove(pkg)
                                })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val scope = rememberCoroutineScope()
            Button(onClick = {
                val toSave = selected.toSet()
                scope.launch {
                    UserPrefs.addBlockedApps(ctx, toSave)
                    onDismiss()
                }
            }) { Text("Block ${selected.size} App${if (selected.size != 1) "s" else ""}") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private suspend fun showInstalledAppsPickerDialog(ctx: android.content.Context) {
    // Compose AlertDialog needs to be shown from composable. As a pragmatic approach,
    // for this iteration we fallback to the previous auto-populate approach if dialog
    // is not feasible here.
    val pm = ctx.packageManager
    val apps = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
        .sortedBy { it.loadLabel(pm).toString() }

    // Fallback: block popular apps found
    val popular = listOf(
        "com.instagram.android", "com.facebook.katana", "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill", "com.twitter.android", "com.reddit.frontpage"
    ).toSet()
    val found = apps.map { it.packageName }.filter { it in popular }.toSet()
    if (found.isNotEmpty()) {
        UserPrefs.addBlockedApps(ctx, found)
    }
}

@Composable fun UsageScreen() { Text("Usage") }
@Composable fun ReportsScreen() { Text("Reports") }
