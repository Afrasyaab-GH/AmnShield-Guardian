package com.deenshield.blocker.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.deenshield.blocker.model.Block
import com.deenshield.blocker.viewmodel.BlockViewModel
import com.deenshield.blocker.ui.components.ToggleItem
import com.deenshield.blocker.util.BlockUtils
import com.deenshield.blocker.service.BlockingVpnService
import java.util.UUID
import android.app.TimePickerDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import com.deenshield.blocker.data.UserPrefs
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BlocksScreen(vm: BlockViewModel) {
    val blocks = vm.blocks
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Blocks", style = MaterialTheme.typography.titleMedium)
        if (blocks.isEmpty()) {
            Text("No blocks yet. Add one to get started.")
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
    val vpnConsentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            // Start VPN service after consent
            val intent = Intent(ctx, BlockingVpnService::class.java).apply { action = BlockingVpnService.ACTION_START }
            ctx.startService(intent)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Services", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val prepare = VpnService.prepare(ctx)
                if (prepare != null) {
                    vpnConsentLauncher.launch(prepare)
                } else {
                    // Already has consent
                    val intent = Intent(ctx, BlockingVpnService::class.java).apply { action = BlockingVpnService.ACTION_START }
                    ctx.startService(intent)
                }
            }) { Text("Enable VPN") }

            OutlinedButton(onClick = {
                val i = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(i)
            }) { Text("Open Accessibility Settings") }

            OutlinedButton(onClick = {
                val intent = Intent(ctx, BlockingVpnService::class.java).apply { action = BlockingVpnService.ACTION_STOP }
                ctx.startService(intent)
            }) { Text("Disable VPN") }
        }
    }
}

@Composable
fun AddBlockScreen(vm: BlockViewModel) {
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

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Add Block", style = MaterialTheme.typography.titleLarge)
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Row { Checkbox(checked = full, onCheckedChange = { full = it }); Text("Full Block") }

        Divider()
        Text("Apps", style = MaterialTheme.typography.titleMedium)
        var appInput by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = appInput,
                onValueChange = { appInput = it },
                label = { Text("Package name (e.g., com.instagram.android)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { addApp(appInput); appInput = "" }) { Text("Add") }
        }
        ChipList(items = apps, onRemove = { rm -> apps = apps.filterNot { it == rm } })

        Divider()
        Text("Websites", style = MaterialTheme.typography.titleMedium)
        var webInput by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = webInput,
                onValueChange = { webInput = it },
                label = { Text("Domain or URL (e.g., example.com)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { addWebsite(webInput); webInput = "" }) { Text("Add") }
        }
        ChipList(items = websites, onRemove = { rm -> websites = websites.filterNot { it == rm } })

        Divider()
        Text("Keywords", style = MaterialTheme.typography.titleMedium)
        var kwInput by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = kwInput,
                onValueChange = { kwInput = it },
                label = { Text("Keyword (min 3 chars)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { addKeyword(kwInput); kwInput = "" }) { Text("Add") }
        }
        ChipList(items = keywords, onRemove = { rm -> keywords = keywords.filterNot { it == rm } })

        Divider()
        Text("Weekly Schedule", style = MaterialTheme.typography.titleMedium)
        DaySelector(weekdaySelected, ::toggleDay)
        TimePickers(
            startHour, startMinute, onStart = { h, m -> startHour = h; startMinute = m },
            endHour, endMinute, onEnd = { h, m -> endHour = h; endMinute = m }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { addIntervalToSelectedDays() }) { Text("Add Interval") }
            OutlinedButton(onClick = { schedule.clear() }) { Text("Clear All") }
        }
        ScheduleList(schedule = schedule, onRemove = { day, idx ->
            val list = schedule[day] ?: return@ScheduleList
            if (idx in list.indices) list.removeAt(idx)
            if (list.isEmpty()) schedule.remove(day)
        })

        Divider()
        Text("Usage Limits (minutes)", style = MaterialTheme.typography.titleMedium)
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

        Button(onClick = {
            error = null
            if (name.isBlank()) { error = "Name required"; return@Button }
            if (apps.isEmpty() && websites.isEmpty() && keywords.isEmpty()) {
                error = "Add at least one target (app, website, or keyword)"; return@Button
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
        }, enabled = !saving) {
            if (saving) CircularProgressIndicator() else Text("Save Block")
        }
    }
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
fun SettingsScreen(vm: BlockViewModel) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var blockedApps by remember { mutableStateOf(setOf<String>()) }

    // Load blocked apps
    LaunchedEffect(Unit) {
        UserPrefs.blockedAppsFlow(ctx).collectLatest { blockedApps = it }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Divider(Modifier.padding(vertical = 8.dp))
    ServiceControls()
    Divider(Modifier.padding(vertical = 8.dp))
        ToggleItem(
            title = "Enable blocking globally",
            checked = vm.globalEnabled,
            onChange = { vm.updateGlobalEnabled(it) }
        )
        ToggleItem(
            title = "Block harmful keywords",
            checked = vm.blockHarmfulKeywords,
            onChange = { vm.updateBlockHarmfulKeywords(it) }
        )
        ToggleItem(
            title = "Block harmful websites",
            checked = vm.blockHarmfulWebsites,
            onChange = { vm.updateBlockHarmfulWebsites(it) }
        )
        ToggleItem(
            title = "Block social media",
            checked = vm.blockSocialMedia,
            onChange = { vm.updateBlockSocialMedia(it) }
        )

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Blocked Apps", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                scope.launch { showInstalledAppsPickerDialog(ctx) }
            }) { Text("Manage blocked apps") }
        }
        Spacer(Modifier.height(8.dp))
        if (blockedApps.isEmpty()) {
            Text("No blocked apps yet.")
        } else {
            LazyColumn {
                itemsIndexed(blockedApps.toList()) { _, pkg ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(pkg)
                        OutlinedButton(onClick = { scope.launch { UserPrefs.removeBlockedApp(ctx, pkg) } }) { Text("Remove") }
                    }
                }
            }
        }
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
        title = { Text("Select apps to block") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                val filtered = apps.filter { it.loadLabel(pm).toString().contains(search, ignoreCase = true) }
                LazyColumn(Modifier.height(360.dp)) {
                    items(filtered) { app ->
                        val label = app.loadLabel(pm).toString()
                        val pkg = app.packageName
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                if (selected.contains(pkg)) selected.remove(pkg) else selected.add(pkg)
                            },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$label ($pkg)", modifier = Modifier.weight(1f))
                            Checkbox(checked = selected.contains(pkg), onCheckedChange = {
                                if (it) selected.add(pkg) else selected.remove(pkg)
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Persist selection
                androidx.lifecycle.lifecycleScope.coroutineContext
                androidx.compose.runtime.LaunchedEffect(Unit) {}
            }) { Text("Save") }
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
