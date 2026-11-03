package com.ourspace.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ourspace.app.ui.components.LittleUniverseIllustration
import com.ourspace.app.ui.components.RomanticGlassCard
import com.ourspace.app.ui.components.RomanticPrimaryButton
import com.ourspace.app.ui.components.RomanticTextField
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel
import com.ourspace.app.ui.viewmodel.SessionState

enum class AuthSubPage(val label: String) {
    LOGIN("Login"),
    REGISTER("Register"),
    FORGOT_PASSWORD("Forgot Password"),
    PAIRING("Couple Pairing")
}

@Composable
fun AuthAndPairingScreen(
    session: SessionState,
    viewModel: OurSpaceMainViewModel,
    onContinueToHome: () -> Unit
) {
    var activePage by rememberSaveable { mutableStateOf(AuthSubPage.PAIRING) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf(session.myDisplayName) }
    var nickname by rememberSaveable { mutableStateOf(session.myNickname) }
    var inviteCodeInput by rememberSaveable { mutableStateOf("") }
    val colors = OurSpaceDesign.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LittleUniverseIllustration(
            title = "OUR SPACE",
            subtitle = "Two Hearts. One Little Universe."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthSubPage.entries.forEach { tab ->
                FilterChip(
                    selected = activePage == tab,
                    onClick = { activePage = tab },
                    label = { Text(tab.label) }
                )
            }
        }

        if (!session.statusMessage.isNullOrBlank()) {
            RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = session.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primaryAccent
                )
            }
        }

        when (activePage) {
            AuthSubPage.LOGIN -> {
                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Masuk ke Akun Anda", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(12.dp))
                    RomanticTextField(value = email, onValueChange = { email = it }, label = "Email")
                    Spacer(Modifier.height(10.dp))
                    RomanticTextField(value = password, onValueChange = { password = it }, label = "Password")
                    Spacer(Modifier.height(16.dp))
                    RomanticPrimaryButton(
                        text = "Login",
                        isLoading = session.isLoading,
                        onClick = {
                            viewModel.login(email, password)
                            onContinueToHome()
                        }
                    )
                    TextButton(
                        onClick = { activePage = AuthSubPage.FORGOT_PASSWORD },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Lupa Password?")
                    }
                }
            }

            AuthSubPage.REGISTER -> {
                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Daftar & Buat Profil", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(12.dp))
                    RomanticTextField(value = displayName, onValueChange = { displayName = it }, label = "Nama Lengkap (Display Name)")
                    Spacer(Modifier.height(10.dp))
                    RomanticTextField(value = nickname, onValueChange = { nickname = it }, label = "Nama Panggilan Sayang (Nickname)")
                    Spacer(Modifier.height(10.dp))
                    RomanticTextField(value = email, onValueChange = { email = it }, label = "Email")
                    Spacer(Modifier.height(10.dp))
                    RomanticTextField(value = password, onValueChange = { password = it }, label = "Password (Min. 8 karakter)")
                    Spacer(Modifier.height(16.dp))
                    RomanticPrimaryButton(
                        text = "Buat Akun & Lanjut Pairing",
                        isLoading = session.isLoading,
                        onClick = {
                            viewModel.register(email, password, displayName, nickname)
                            activePage = AuthSubPage.PAIRING
                        }
                    )
                }
            }

            AuthSubPage.FORGOT_PASSWORD -> {
                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Reset Password", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Masukkan email akun Anda untuk menerima tautan pemulihan kata sandi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    RomanticTextField(value = email, onValueChange = { email = it }, label = "Email Terdaftar")
                    Spacer(Modifier.height(16.dp))
                    RomanticPrimaryButton(
                        text = "Kirim Email Pemulihan",
                        onClick = { activePage = AuthSubPage.LOGIN }
                    )
                }
            }

            AuthSubPage.PAIRING -> {
                RomanticGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Atomic Couple Pairing", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Maksimal 2 anggota dalam 1 pasangan. Kode undangan bersifat sekali pakai dengan masa berlaku 24 jam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.height(14.dp))
                    RomanticPrimaryButton(
                        text = "Buat Invitation Code Baru (Pengguna Pertama)",
                        onClick = { viewModel.generateInviteCode() }
                    )
                    session.activeInviteCode?.let { invite ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Kode Undangan Anda: ${invite.code}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.primaryAccent
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    RomanticTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it.uppercase().take(8) },
                        label = "Masukkan Invitation Code Pasangan (8 Karakter)",
                        placeholder = "Contoh: LOVE2026"
                    )
                    Spacer(Modifier.height(12.dp))
                    RomanticPrimaryButton(
                        text = "Verifikasi & Hubungkan Our Space",
                        onClick = {
                            viewModel.redeemInviteCode(inviteCodeInput)
                            onContinueToHome()
                        }
                    )
                }
            }
        }
    }
}
