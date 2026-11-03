package com.ourspace.app.ui.screens.myspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.ourspace.app.domain.model.MoodType
import com.ourspace.app.domain.model.TaskPriority
import com.ourspace.app.ui.components.RomanticGlassCard
import com.ourspace.app.ui.components.RomanticPrimaryButton
import com.ourspace.app.ui.components.RomanticTextField
import com.ourspace.app.ui.security.BiometricLockHelper
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel

enum class MySpaceSection(val label: String) {
    NOTES("Personal Notes"),
    DIARY("Personal Diary"),
    TASKS("Personal Tasks"),
    COLLECTION("Private Collection"),
    SECURITY("Security & Lock")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MySpaceScreen(viewModel: OurSpaceMainViewModel) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isUnlocked by viewModel.isMySpaceUnlocked.collectAsState()
    val notes by viewModel.personalNotes.collectAsState()
    val diaryEntries by viewModel.personalDiary.collectAsState()
    val tasks by viewModel.personalTasks.collectAsState()
    val collectionItems by viewModel.privateCollection.collectAsState()
    val searchQuery by viewModel.personalSearchQuery.collectAsState()

    var activeSection by rememberSaveable { mutableStateOf(MySpaceSection.NOTES) }
    var pinInput by rememberSaveable { mutableStateOf("") }
    var pinError by rememberSaveable { mutableStateOf<String?>(null) }

    val colors = OurSpaceDesign.colors
    val context = LocalContext.current

    val requiresLock = (prefs.biometricLockEnabled || !prefs.privatePinHash.isNullOrBlank()) && !isUnlocked
    if (requiresLock) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("🔒 My Space Terkunci", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ruang pribadi ini terisolasi sepenuhnya dan tidak dapat dilihat oleh pasangan Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                Spacer(Modifier.height(14.dp))
                RomanticTextField(
                    value = pinInput,
                    onValueChange = {
                        pinInput = it
                        pinError = null
                    },
                    label = "Masukkan PIN Pribadi",
                    errorMessage = pinError
                )
                Spacer(Modifier.height(12.dp))
                RomanticPrimaryButton(
                    text = "Buka dengan PIN",
                    onClick = {
                        if (!viewModel.unlockMySpaceWithPin(pinInput)) {
                            pinError = "PIN tidak sesuai."
                        } else {
                            pinInput = ""
                        }
                    }
                )
                if (prefs.biometricLockEnabled && context is FragmentActivity) {
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Buka dengan Sidik Jari / Biometrik",
                        onClick = {
                            if (BiometricLockHelper.canAuthenticateWithBiometrics(context)) {
                                BiometricLockHelper.showBiometricPrompt(
                                    activity = context,
                                    onSuccess = { viewModel.unlockMySpaceByBiometric() },
                                    onError = { err -> pinError = err }
                                )
                            } else {
                                viewModel.unlockMySpaceByBiometric()
                            }
                        }
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("My Space 🔒", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
                Text(
                    "Ruang pribadi eksklusif milikmu (Tidak terlihat oleh pasangan)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
            if (!prefs.privatePinHash.isNullOrBlank() || prefs.biometricLockEnabled) {
                TextButton(onClick = { viewModel.lockMySpace() }) {
                    Text("Kunci Sekarang")
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MySpaceSection.entries.forEach { section ->
                FilterChip(
                    selected = activeSection == section,
                    onClick = { activeSection = section },
                    label = { Text(section.label) }
                )
            }
        }

        when (activeSection) {
            MySpaceSection.NOTES -> {
                var noteTitle by rememberSaveable { mutableStateOf("") }
                var noteContent by rememberSaveable { mutableStateOf("") }
                var noteCategory by rememberSaveable { mutableStateOf("Reflections") }
                var isPinned by rememberSaveable { mutableStateOf(false) }
                var isFavorite by rememberSaveable { mutableStateOf(false) }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Tambah Catatan Pribadi (Autosave Aktif)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = searchQuery, onValueChange = { viewModel.setPersonalSearch(it) }, label = "🔍 Cari catatan pribadi...")
                    Spacer(Modifier.height(10.dp))
                    RomanticTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = "Judul Catatan")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = "Isi Catatan Pribadi",
                        singleLine = false
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                        Text("Pin", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.padding(horizontal = 8.dp))
                        Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                        Text("Favorite ⭐", style = MaterialTheme.typography.bodySmall)
                    }
                    RomanticPrimaryButton(
                        text = "Simpan ke My Space",
                        onClick = {
                            if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                                viewModel.savePersonalNote(
                                    title = noteTitle,
                                    content = noteContent,
                                    category = noteCategory,
                                    colorHex = "#FDF0F6",
                                    isPinned = isPinned,
                                    isFavorite = isFavorite
                                )
                                noteTitle = ""
                                noteContent = ""
                            }
                        }
                    )
                }

                readingNote?.let { rNote ->
                    AlertDialog(
                        onDismissRequest = { readingNote = null },
                        title = { Text(rNote.title, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary) },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("Kategori: ${rNote.category}", style = MaterialTheme.typography.labelSmall, color = colors.primaryAccent)
                                Spacer(Modifier.height(8.dp))
                                Text(rNote.content, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { readingNote = null }) {
                                Text("Tutup")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                viewModel.deletePersonalNote(rNote.id)
                                readingNote = null
                            }) {
                                Text("Hapus Catatan", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }

                notes.forEach { note ->
                    RomanticGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { readingNote = note }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (note.isPinned) "📌 " else ""}${if (note.isFavorite) "⭐ " else ""}${note.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Row {
                                TextButton(onClick = { readingNote = note }) {
                                    Text("Baca")
                                }
                                TextButton(onClick = { viewModel.deletePersonalNote(note.id) }) {
                                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Text(note.content.take(120) + if (note.content.length > 120) "..." else "", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Kategori: ${note.category} • Ketuk untuk membaca lengkap", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    }
                }
            }

            MySpaceSection.DIARY -> {
                var diaryTitle by rememberSaveable { mutableStateOf("") }
                var diaryBody by rememberSaveable { mutableStateOf("") }
                var selectedMood by rememberSaveable { mutableStateOf(MoodType.HAPPY) }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Personal Daily Journal", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MoodType.entries.forEach { mood ->
                            FilterChip(
                                selected = selectedMood == mood,
                                onClick = { selectedMood = mood },
                                label = { Text("${mood.emoji} ${mood.label}") }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = diaryTitle, onValueChange = { diaryTitle = it }, label = "Judul Hari Ini")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(
                        value = diaryBody,
                        onValueChange = { diaryBody = it },
                        label = "Ceritakan harimu (mendukung format **tebal** & _miring_)",
                        singleLine = false
                    )
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Simpan Jurnal Harian",
                        onClick = {
                            if (diaryBody.isNotBlank()) {
                                viewModel.savePersonalDiary(diaryTitle, diaryBody, selectedMood.name)
                                diaryTitle = ""
                                diaryBody = ""
                            }
                        }
                    )
                }

                diaryEntries.forEach { entry ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("${entry.moodName} — ${entry.title}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(entry.richContent, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }

            MySpaceSection.TASKS -> {
                var taskTitle by rememberSaveable { mutableStateOf("") }
                var priority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Personal To-Do & Checklist", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = taskTitle, onValueChange = { taskTitle = it }, label = "Tugas Pribadi Baru")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskPriority.entries.forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p.label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Tambah Tugas Pribadi",
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.savePersonalTask(taskTitle, priority.name, null)
                                taskTitle = ""
                            }
                        }
                    )
                }

                tasks.forEach { task ->
                    RomanticGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.togglePersonalTask(task) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.isCompleted, onCheckedChange = { viewModel.togglePersonalTask(task) })
                            Column {
                                Text(task.title, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                                Text("Prioritas: ${task.priority}", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            MySpaceSection.COLLECTION -> {
                var folder by rememberSaveable { mutableStateOf("Draft Letters") }
                var itemTitle by rememberSaveable { mutableStateOf("") }
                var itemBody by rememberSaveable { mutableStateOf("") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Private Collection (Draft Surat, Foto & Memori Pribadi)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = folder, onValueChange = { folder = it }, label = "Nama Folder Pribadi")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = "Judul Draft / Memori")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = itemBody, onValueChange = { itemBody = it }, label = "Isi Draft Surat / Catatan Kenangan", singleLine = false)
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Simpan ke Folder Pribadi",
                        onClick = {
                            if (itemTitle.isNotBlank()) {
                                viewModel.savePrivateCollectionItem("DRAFT_LETTER", folder, itemTitle, itemBody)
                                itemTitle = ""
                                itemBody = ""
                            }
                        }
                    )
                }

                collectionItems.forEach { item ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("📁 [${item.folderName}] ${item.title}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(item.bodyOrCaption, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }

            MySpaceSection.SECURITY -> {
                var newPin by rememberSaveable { mutableStateOf("") }
                var bioEnabled by rememberSaveable { mutableStateOf(prefs.biometricLockEnabled) }
                var timeoutMinutes by rememberSaveable { mutableStateOf(prefs.autoLockTimeoutMinutes) }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Pengaturan Keamanan My Space", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kunci Sidik Jari / Biometrik", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = bioEnabled, onCheckedChange = { bioEnabled = it })
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(
                        value = newPin,
                        onValueChange = { newPin = it.filter { c -> c.isDigit() }.take(6) },
                        label = "Atur PIN Pribadi (4-6 Digit, kosongkan untuk menonaktifkan)"
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Auto-Lock Timeout: $timeoutMinutes menit", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 5, 15, 30).forEach { mins ->
                            FilterChip(
                                selected = timeoutMinutes == mins,
                                onClick = { timeoutMinutes = mins },
                                label = { Text("$mins mnt") }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    RomanticPrimaryButton(
                        text = "Simpan Pengaturan Keamanan",
                        onClick = {
                            viewModel.configureMySpaceSecurity(bioEnabled, newPin.ifBlank { null }, timeoutMinutes)
                            newPin = ""
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
