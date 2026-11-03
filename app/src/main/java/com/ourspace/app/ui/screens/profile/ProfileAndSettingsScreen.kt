package com.ourspace.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ourspace.app.ui.components.RomanticGlassCard
import com.ourspace.app.ui.components.RomanticPrimaryButton
import com.ourspace.app.ui.components.RomanticTextField
import com.ourspace.app.ui.theme.AppThemeMode
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileAndSettingsScreen(
    viewModel: OurSpaceMainViewModel,
    onOpenAuthPairing: () -> Unit
) {
    val session by viewModel.sessionState.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val colors = OurSpaceDesign.colors

    var displayName by rememberSaveable { mutableStateOf(session.myDisplayName) }
    var myNickname by rememberSaveable { mutableStateOf(session.myNickname) }
    var partnerNickname by rememberSaveable { mutableStateOf(session.partnerNickname) }
    var anniversaryStr by rememberSaveable {
        val date = runCatching {
            Instant.ofEpochMilli(session.anniversaryMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        }.getOrDefault(LocalDate.now().minusYears(1))
        mutableStateOf(date.toString())
    }
    var unpairConfirmation by rememberSaveable { mutableStateOf("") }

    // Synchronize inputs with session state when loaded from preferences
    LaunchedEffect(session.myDisplayName, session.myNickname, session.partnerNickname, session.anniversaryMillis) {
        displayName = session.myDisplayName
        myNickname = session.myNickname
        partnerNickname = session.partnerNickname
        val date = runCatching {
            Instant.ofEpochMilli(session.anniversaryMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        }.getOrDefault(LocalDate.now().minusYears(1))
        anniversaryStr = date.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile & Settings ⚙️", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)

        session.statusMessage?.let { status ->
            RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(status, style = MaterialTheme.typography.bodyMedium, color = colors.primaryAccent)
            }
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Profil Saya & Pasangan", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            RomanticTextField(value = displayName, onValueChange = { displayName = it }, label = "Display Name")
            Spacer(Modifier.height(8.dp))
            RomanticTextField(value = myNickname, onValueChange = { myNickname = it }, label = "Nickname Saya")
            Spacer(Modifier.height(8.dp))
            RomanticTextField(value = partnerNickname, onValueChange = { partnerNickname = it }, label = "Nickname Pasangan")
            Spacer(Modifier.height(8.dp))
            RomanticTextField(
                value = anniversaryStr,
                onValueChange = { anniversaryStr = it },
                label = "Tanggal Jadian (Format: YYYY-MM-DD)"
            )
            Spacer(Modifier.height(10.dp))
            RomanticPrimaryButton(
                text = "Simpan Perubahan Profil",
                onClick = {
                    val parsedMillis = runCatching {
                        LocalDate.parse(anniversaryStr.trim())
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }.getOrDefault(session.anniversaryMillis)

                    viewModel.updateProfileAndAnniversary(
                        displayName = displayName,
                        nickname = myNickname,
                        partnerNickname = partnerNickname,
                        anniversaryMillis = parsedMillis
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
            RomanticPrimaryButton(
                text = "Kelola Akun & Kode Pairing Pasangan",
                onClick = onOpenAuthPairing
            )
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Pilih Tema Visual (Soft Romantic System)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = prefs.themeMode == mode,
                        onClick = { viewModel.selectTheme(mode) },
                        label = { Text(mode.displayName) }
                    )
                }
            }
        }

        RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Konfirmasi Putuskan Pasangan (Unpair)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Untuk mencegah pemutusan tidak sengaja, ketik UNPAIR di bawah ini sebelum memutuskan tautan pasangan.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
            Spacer(Modifier.height(8.dp))
            RomanticTextField(
                value = unpairConfirmation,
                onValueChange = { unpairConfirmation = it },
                label = "Ketik UNPAIR untuk konfirmasi"
            )
            Spacer(Modifier.height(8.dp))
            RomanticPrimaryButton(
                text = "Putuskan Tautan Pasangan",
                enabled = unpairConfirmation.equals("UNPAIR", ignoreCase = true),
                onClick = {
                    viewModel.unpairWithConfirmation(unpairConfirmation)
                    unpairConfirmation = ""
                }
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
