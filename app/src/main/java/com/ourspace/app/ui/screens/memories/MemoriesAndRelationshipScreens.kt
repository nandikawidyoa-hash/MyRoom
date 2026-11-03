package com.ourspace.app.ui.screens.memories

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.ourspace.app.domain.usecase.RelationshipCalculator
import com.ourspace.app.ui.components.RomanticGlassCard
import com.ourspace.app.ui.components.RomanticPrimaryButton
import com.ourspace.app.ui.components.RomanticTextField
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel

enum class MemoriesSubTab(val label: String) {
    GALLERY("Memories Gallery"),
    RELATIONSHIP("Relationship & Time Capsule"),
    SURPRISES("Surprise Features")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoriesAndRelationshipScreen(
    viewModel: OurSpaceMainViewModel,
    initialSubTab: MemoriesSubTab = MemoriesSubTab.GALLERY
) {
    val memories by viewModel.memories.collectAsState()
    val capsules by viewModel.timeCapsules.collectAsState()
    val surprises by viewModel.surprises.collectAsState()
    val searchQuery by viewModel.memoriesSearchQuery.collectAsState()

    var activeTab by rememberSaveable { mutableStateOf(initialSubTab) }
    val colors = OurSpaceDesign.colors
    val stats = viewModel.getRelationshipStats()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Memories, Story & Surprises 📸", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MemoriesSubTab.entries.forEach { tab ->
                FilterChip(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    label = { Text(tab.label) }
                )
            }
        }

        when (activeTab) {
            MemoriesSubTab.GALLERY -> {
                var albumTitle by rememberSaveable { mutableStateOf("Our First Trip") }
                var caption by rememberSaveable { mutableStateOf("") }
                var isFavorite by rememberSaveable { mutableStateOf(true) }
                var randomMemoryCaption by rememberSaveable { mutableStateOf<String?>(null) }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Simpan Kenangan Baru (Kompresi & Thumbnail Otomatis)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = searchQuery, onValueChange = { viewModel.setMemoriesSearch(it) }, label = "🔍 Cari album atau caption kenangan...")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = albumTitle, onValueChange = { albumTitle = it }, label = "Nama Album")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = caption, onValueChange = { caption = it }, label = "Cerita di Balik Momen Ini", singleLine = false)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                        Text("Tandai sebagai Kenangan Favorit ❤️", style = MaterialTheme.typography.bodySmall)
                    }
                    RomanticPrimaryButton(
                        text = "Unggah Kenangan ke Firebase Storage",
                        onClick = {
                            if (caption.isNotBlank()) {
                                viewModel.uploadMemory(albumTitle, caption, null, isFavorite)
                                caption = ""
                            }
                        }
                    )
                    if (memories.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        RomanticPrimaryButton(
                            text = "🎲 Tampilkan Random Memory of the Day",
                            onClick = {
                                randomMemoryCaption = memories.random().caption
                            }
                        )
                    }
                }

                randomMemoryCaption?.let { randCap ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("🌟 Memory of the Day", style = MaterialTheme.typography.titleMedium, color = colors.primaryAccent)
                        Spacer(Modifier.height(4.dp))
                        Text(randCap, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    }
                }

                memories.forEach { memory ->
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${if (memory.isFavorite) "❤️ " else "📸 "}Album: ${memory.albumTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(memory.caption, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        if (memory.uploadStatus == "UPLOADING") {
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { memory.uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Diunggah oleh ${memory.uploadedByNickname} • Status: ${memory.uploadStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }
            }

            MemoriesSubTab.RELATIONSHIP -> {
                var capsuleTitle by rememberSaveable { mutableStateOf("") }
                var capsuleMessage by rememberSaveable { mutableStateOf("") }
                var unlockDays by rememberSaveable { mutableStateOf("30") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Relationship Counter & Milestones 💞", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Bersama selama ${stats.totalDaysTogether} Hari (${stats.monthsTogether} Bulan / ${stats.yearsTogether} Tahun)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.primaryAccent
                    )
                    Text(
                        "Hitung mundur Anniversary berikutnya: ${stats.daysUntilNextAnniversary} hari lagi",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Digital Time Capsule ⏳ (Dikunci Waktu Server)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tulis surat masa depan untuk pasanganmu. Pesan ini hanya bisa dibuka setelah tanggal yang ditentukan tercapai.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = capsuleTitle, onValueChange = { capsuleTitle = it }, label = "Judul Time Capsule")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = capsuleMessage, onValueChange = { capsuleMessage = it }, label = "Isi Pesan Rahasia Masa Depan", singleLine = false)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = unlockDays, onValueChange = { unlockDays = it.filter { c -> c.isDigit() } }, label = "Buka Setelah Berapa Hari?")
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Segel Digital Time Capsule",
                        onClick = {
                            if (capsuleTitle.isNotBlank() && capsuleMessage.isNotBlank()) {
                                viewModel.createTimeCapsule(
                                    title = capsuleTitle,
                                    sealedMessage = capsuleMessage,
                                    unlockInDays = unlockDays.toIntOrNull() ?: 30
                                )
                                capsuleTitle = ""
                                capsuleMessage = ""
                            }
                        }
                    )
                }

                val nowServer = System.currentTimeMillis()
                capsules.forEach { cap ->
                    val unlocked = RelationshipCalculator.isUnlockedByServerTime(cap.unlockAtServerMillis, nowServer)
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${if (unlocked) "🔓" else "🔒"} ${cap.title} (Oleh ${cap.authorNickname})",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        if (unlocked) {
                            Text(cap.sealedMessage, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        } else {
                            val remainingDays = ((cap.unlockAtServerMillis - nowServer) / 86400000L).coerceAtLeast(1L)
                            Text(
                                "Terkunci rapat oleh Server Time. Akan terbuka dalam $remainingDays hari lagi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.primaryAccent
                            )
                        }
                    }
                }
            }

            MemoriesSubTab.SURPRISES -> {
                var surpriseType by rememberSaveable { mutableStateOf("SCHEDULED_LETTER") }
                var surpriseTitle by rememberSaveable { mutableStateOf("") }
                var surpriseBody by rememberSaveable { mutableStateOf("") }
                var revealHours by rememberSaveable { mutableStateOf("0") }

                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Surprise Hub 🎁 (Pesan Terjadwal, Gift Card & Countdown)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SCHEDULED_LETTER", "DIGITAL_GIFT_CARD", "SECRET_NOTE", "COUNTDOWN_SURPRISE").forEach { type ->
                            FilterChip(
                                selected = surpriseType == type,
                                onClick = { surpriseType = type },
                                label = { Text(type.replace("_", " ")) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = surpriseTitle, onValueChange = { surpriseTitle = it }, label = "Judul Kejutan / Voucher Gift Card")
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = surpriseBody, onValueChange = { surpriseBody = it }, label = "Pesan Kejutan", singleLine = false)
                    Spacer(Modifier.height(8.dp))
                    RomanticTextField(value = revealHours, onValueChange = { revealHours = it.filter { c -> c.isDigit() } }, label = "Tampilkan Setelah Berapa Jam? (0 = Langsung)")
                    Spacer(Modifier.height(10.dp))
                    RomanticPrimaryButton(
                        text = "Jadwalkan Kejutan (Waktu Server)",
                        onClick = {
                            if (surpriseTitle.isNotBlank() && surpriseBody.isNotBlank()) {
                                viewModel.scheduleSurprise(
                                    surpriseType = surpriseType,
                                    title = surpriseTitle,
                                    content = surpriseBody,
                                    revealInHours = revealHours.toIntOrNull() ?: 0
                                )
                                surpriseTitle = ""
                                surpriseBody = ""
                            }
                        }
                    )
                }

                val nowServer = System.currentTimeMillis()
                surprises.forEach { s ->
                    val canReveal = RelationshipCalculator.isUnlockedByServerTime(s.scheduledRevealServerMillis, nowServer)
                    RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("🎁 [${s.surpriseType}] ${s.title}", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        if (canReveal) {
                            Text(s.content, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        } else {
                            Text("⏳ Kejutan sedang menghitung mundur hingga waktu server tiba!", style = MaterialTheme.typography.bodySmall, color = colors.primaryAccent)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
