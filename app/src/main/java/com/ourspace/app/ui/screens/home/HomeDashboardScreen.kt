package com.ourspace.app.ui.screens.home

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
import com.ourspace.app.ui.components.CoupleUniverseHeader
import com.ourspace.app.ui.components.RomanticGlassCard
import com.ourspace.app.ui.components.RomanticPrimaryButton
import com.ourspace.app.ui.components.RomanticTextField
import com.ourspace.app.ui.components.SyncState
import com.ourspace.app.ui.components.SyncStatusIndicator
import com.ourspace.app.ui.navigation.BottomNavTab
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    viewModel: OurSpaceMainViewModel,
    onNavigateTab: (BottomNavTab) -> Unit,
    onOpenRelationshipHub: () -> Unit,
    onOpenSurpriseHub: () -> Unit
) {
    val session by viewModel.sessionState.collectAsState()
    val sharedNotes by viewModel.sharedNotes.collectAsState()
    val sharedTasks by viewModel.sharedTasks.collectAsState()
    val sharedEvents by viewModel.sharedEvents.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val sharedMessages by viewModel.sharedMessages.collectAsState()
    val pendingSync by viewModel.pendingSyncCount.collectAsState()

    val stats = viewModel.getRelationshipStats()
    val dailyQuestion = viewModel.getDailyQuestion()
    var dailyAnswer by rememberSaveable { mutableStateOf("") }
    val colors = OurSpaceDesign.colors

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 4..10 -> "Selamat Pagi"
        in 11..14 -> "Selamat Siang"
        in 15..18 -> "Selamat Sore"
        else -> "Selamat Malam"
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
                Text(
                    text = "$greeting, ${session.myNickname} ✨",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "Bersama ${session.partnerNickname} di rumah kecil kalian",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
            SyncStatusIndicator(
                syncState = if (pendingSync > 0) SyncState.OFFLINE_PENDING else SyncState.SYNCED,
                pendingCount = pendingSync
            )
        }

        CoupleUniverseHeader(
            myNickname = session.myNickname,
            partnerNickname = session.partnerNickname,
            daysTogether = stats.totalDaysTogether
        )

        RomanticGlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenRelationshipHub
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Anniversary Countdown", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Text(
                        "${stats.daysUntilNextAnniversary} hari menuju Anniversary ke-${stats.nextAnniversaryNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primaryAccent
                    )
                }
                Text("Ketuk Detail →", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
            }
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { onNavigateTab(BottomNavTab.OUR_SPACE) },
                    label = { Text("📝 Catatan Bersama") }
                )
                AssistChip(
                    onClick = {
                        viewModel.sendLoveNote("LOVE_NOTE", "Aku kangen kamu hari ini, ${session.partnerNickname} 🤍")
                    },
                    label = { Text("💌 Kirim Love Note Cepat") }
                )
                AssistChip(
                    onClick = { onNavigateTab(BottomNavTab.MEMORIES) },
                    label = { Text("📸 Tambah Kenangan") }
                )
                AssistChip(
                    onClick = onOpenSurpriseHub,
                    label = { Text("🎁 Buat Kejutan") }
                )
                AssistChip(
                    onClick = onOpenRelationshipHub,
                    label = { Text("⏳ Time Capsule") }
                )
            }
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Daily Couple Question 💭", style = MaterialTheme.typography.titleMedium, color = colors.primaryAccent)
            Spacer(Modifier.height(6.dp))
            Text(dailyQuestion, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
            Spacer(Modifier.height(10.dp))
            RomanticTextField(
                value = dailyAnswer,
                onValueChange = { dailyAnswer = it },
                label = "Tulis jawabanmu untuk ${session.partnerNickname}..."
            )
            Spacer(Modifier.height(10.dp))
            RomanticPrimaryButton(
                text = "Bagikan Jawaban ke Our Space",
                onClick = {
                    if (dailyAnswer.isNotBlank()) {
                        viewModel.sendLoveNote("DAILY_QUESTION", "Q: $dailyQuestion - A: $dailyAnswer")
                        dailyAnswer = ""
                    }
                }
            )
        }

        sharedMessages.firstOrNull()?.let { latestMsg ->
            RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Love Note Terbaru dari ${latestMsg.senderNickname} 💌",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(latestMsg.content, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            }
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Catatan Bersama Terbaru", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(6.dp))
            sharedNotes.take(2).forEach { note ->
                Text("• ${note.title}: ${note.content.take(70)}", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Tugas Bersama Hari Ini", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(6.dp))
            val pendingTasks = sharedTasks.filter { !it.isCompleted }.take(3)
            if (pendingTasks.isEmpty()) {
                Text("Semua tugas bersama sudah selesai! 🌸", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            } else {
                pendingTasks.forEach { task ->
                    Text("☐ ${task.title} (${task.assignedToNickname})", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                }
            }
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Kenangan (${memories.size}) & Agenda Mendatang (${sharedEvents.size})",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Ketuk tab Memories atau Our Space untuk melihat galeri foto, kalender bulanan, dan daftar impian kalian.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}
