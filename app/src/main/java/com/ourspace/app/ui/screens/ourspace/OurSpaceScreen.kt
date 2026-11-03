package com.ourspace.app.ui.screens.ourspace

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ourspace.app.data.repository.ConflictResolutionStrategy
import com.ourspace.app.domain.model.MoodType
import com.ourspace.app.ui.components.RomanticGlassCard
import com.ourspace.app.ui.components.RomanticPrimaryButton
import com.ourspace.app.ui.components.RomanticTextField
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel

enum class OurSpaceTab(val label: String) {
    SHARED_NOTES("Shared Notes"),
    SHARED_DIARY("Shared Diary"),
    SHARED_TASKS("Shared Tasks"),
    CALENDAR("Calendar"),
    BUCKET_LIST("Bucket List"),
    MESSAGES("Love Messages")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OurSpaceScreen(viewModel: OurSpaceMainViewModel) {
    val session by viewModel.sessionState.collectAsState()
    val sharedNotes by viewModel.sharedNotes.collectAsState()
    val sharedDiary by viewModel.sharedDiary.collectAsState()
    val sharedTasks by viewModel.sharedTasks.collectAsState()
    val sharedEvents by viewModel.sharedEvents.collectAsState()
    val bucketList by viewModel.bucketList.collectAsState()
    val sharedMessages by viewModel.sharedMessages.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(OurSpaceTab.SHARED_NOTES) }
    val colors = OurSpaceDesign.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Our Space 💞",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary
        )
        Text(
            text = "Ruang bersama ${session.myNickname} & ${session.partnerNickname} — Tersinkronisasi Real-Time",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OurSpaceTab.entries.forEach { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = { Text(tab.label) }
                )
            }
        }

        when (selectedTab) {
            OurSpaceTab.SHARED_NOTES -> {
                var title by rememberSaveable { mutableStateOf("") }
                var content by rememberSaveable { mutableStateOf("") }
                var category by rememberSaveable { mutableStateOf("Our Plans") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Buat / Edit Catatan Bersama (Collaborative & Conflict-Safe)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = title, onValueChange = { title = it }, label = "Judul Catatan Bersama")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = content, onValueChange = { content = it }, label = "Isi Catatan Bersama", singleLine = false)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = category, onValueChange = { category = it }, label = "Kategori")
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Simpan & Sinkronkan",
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                viewModel.saveSharedNote(title = title, content = content, category = category)
                                title = ""
                                content = ""
                            }
                        }
                    )
                }

                sharedNotes.forEach { note ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("${note.title} (v${note.version})", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(note.content, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Terakhir diedit oleh ${note.lastEditedByNickname} • Status: ${note.syncStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        if (note.hasConflict) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⚠️ Konflik Perubahan Terdeteksi! Pilih cara menggabungkan:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = { viewModel.resolveConflict(note, ConflictResolutionStrategy.KEEP_MINE) },
                                    label = { Text("Gunakan Versiku") }
                                )
                                AssistChip(
                                    onClick = { viewModel.resolveConflict(note, ConflictResolutionStrategy.KEEP_PARTNER) },
                                    label = { Text("Versi Pasangan") }
                                )
                                AssistChip(
                                    onClick = { viewModel.resolveConflict(note, ConflictResolutionStrategy.MERGE_BOTH) },
                                    label = { Text("Gabungkan Keduanya") }
                                )
                            }
                        }
                    }
                }
            }

            OurSpaceTab.SHARED_DIARY -> {
                var diaryTitle by rememberSaveable { mutableStateOf("") }
                var diaryContent by rememberSaveable { mutableStateOf("") }
                var mood by rememberSaveable { mutableStateOf(MoodType.IN_LOVE) }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Tulis Jurnal Bersama & Bagikan Mood", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MoodType.entries.forEach { m ->
                            FilterChip(
                                selected = mood == m,
                                onClick = { mood = m },
                                label = { Text("${m.emoji} ${m.label}") }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = diaryTitle, onValueChange = { diaryTitle = it }, label = "Judul Cerita Hari Ini")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = diaryContent, onValueChange = { diaryContent = it }, label = "Cerita kita hari ini...", singleLine = false)
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Bagikan ke Shared Diary",
                        onClick = {
                            if (diaryContent.isNotBlank()) {
                                viewModel.saveSharedDiary(diaryTitle, diaryContent, mood.name)
                                diaryTitle = ""
                                diaryContent = ""
                            }
                        }
                    )
                }

                sharedDiary.forEach { entry ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("${entry.moodName} • ${entry.title}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text("Oleh ${entry.authorNickname}", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                        Spacer(Modifier.height(6.dp))
                        Text(entry.content, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }

            OurSpaceTab.SHARED_TASKS -> {
                var taskTitle by rememberSaveable { mutableStateOf("") }
                var category by rememberSaveable { mutableStateOf("Checklist") }
                var assignedTo by rememberSaveable { mutableStateOf("Both") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Shared Checklist & Shopping List", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = taskTitle, onValueChange = { taskTitle = it }, label = "Nama Tugas / Barang Belanjaan")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Checklist", "Shopping List").forEach { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Both", session.myNickname, session.partnerNickname).forEach { who ->
                            FilterChip(selected = assignedTo == who, onClick = { assignedTo = who }, label = { Text("PIC: $who") })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Tambah ke Daftar Bersama",
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.saveSharedTask(taskTitle, category, assignedTo, null)
                                taskTitle = ""
                            }
                        }
                    )
                }

                sharedTasks.forEach { task ->
                    RomanticGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.toggleSharedTask(task) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.isCompleted, onCheckedChange = { viewModel.toggleSharedTask(task) })
                            Column {
                                Text("[${task.category}] ${task.title}", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                                Text("Ditugaskan ke: ${task.assignedToNickname}", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            OurSpaceTab.CALENDAR -> {
                var eventTitle by rememberSaveable { mutableStateOf("") }
                var eventDesc by rememberSaveable { mutableStateOf("") }
                var eventType by rememberSaveable { mutableStateOf("ANNIVERSARY") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Kalender Pasangan & Pengingat Penting", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ANNIVERSARY", "BIRTHDAY", "SPECIAL", "REMINDER").forEach { type ->
                            FilterChip(selected = eventType == type, onClick = { eventType = type }, label = { Text(type) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = eventTitle, onValueChange = { eventTitle = it }, label = "Nama Acara / Hari Spesial")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = eventDesc, onValueChange = { eventDesc = it }, label = "Catatan Pengingat")
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Simpan ke Kalender Bersama",
                        onClick = {
                            if (eventTitle.isNotBlank()) {
                                viewModel.saveSharedEvent(eventTitle, eventDesc, eventType, System.currentTimeMillis() + 86400000L * 7)
                                eventTitle = ""
                                eventDesc = ""
                            }
                        }
                    )
                }

                sharedEvents.forEach { ev ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("📅 [${ev.eventType}] ${ev.title}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        if (ev.description.isNotBlank()) {
                            Text(ev.description, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        }
                        Text("Dibuat oleh ${ev.createdByNickname}", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    }
                }
            }

            OurSpaceTab.BUCKET_LIST -> {
                var itemTitle by rememberSaveable { mutableStateOf("") }
                var itemDesc by rememberSaveable { mutableStateOf("") }
                var itemCategory by rememberSaveable { mutableStateOf("Places to Visit") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Shared Bucket List & Wishlist Impian", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Places to Visit", "Activities Together", "Wishlist").forEach { cat ->
                            FilterChip(selected = itemCategory == cat, onClick = { itemCategory = cat }, label = { Text(cat) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = "Impian Bersama")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = itemDesc, onValueChange = { itemDesc = it }, label = "Detail Rencana")
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Tambahkan ke Bucket List",
                        onClick = {
                            if (itemTitle.isNotBlank()) {
                                viewModel.saveBucketItem(itemTitle, itemDesc, itemCategory)
                                itemTitle = ""
                                itemDesc = ""
                            }
                        }
                    )
                }

                bucketList.forEach { item ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("✨ [${item.category}] ${item.title}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        if (item.description.isNotBlank()) {
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Progress: ${item.progressPercent}%", style = MaterialTheme.typography.labelMedium, color = colors.primaryAccent)
                        Slider(
                            value = item.progressPercent.toFloat(),
                            onValueChange = { viewModel.updateBucketProgress(item, it.toInt()) },
                            valueRange = 0f..100f
                        )
                    }
                }
            }

            OurSpaceTab.MESSAGES -> {
                var msgText by rememberSaveable { mutableStateOf("") }
                var msgType by rememberSaveable { mutableStateOf("LOVE_NOTE") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Love Notes & Digital Letters 💌", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOVE_NOTE", "DIGITAL_LETTER", "PERSONAL_MESSAGE").forEach { t ->
                            FilterChip(selected = msgType == t, onClick = { msgType = t }, label = { Text(t.replace("_", " ")) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = msgText, onValueChange = { msgText = it }, label = "Tulis pesan cinta untuk ${session.partnerNickname}...", singleLine = false)
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Kirim ke Pasangan",
                        onClick = {
                            if (msgText.isNotBlank()) {
                                viewModel.sendLoveNote(msgType, msgText)
                                msgText = ""
                            }
                        }
                    )
                }

                sharedMessages.forEach { msg ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${msg.senderNickname} (${msg.messageType})", style = MaterialTheme.typography.titleSmall, color = colors.primaryAccent)
                            Text(if (msg.isRead) "✓✓ Dibaca" else "✓ Terkirim", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(msg.content, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("❤️", "🥰", "🌸", "✨").forEach { emoji ->
                                AssistChip(
                                    onClick = { viewModel.reactToMessage(msg, emoji) },
                                    label = { Text(if (msg.reactionEmoji == emoji) "$emoji Dipilih" else emoji) }
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
