package com.ourspace.app.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ourspace.app.data.local.preferences.UserPreferencesRepository
import com.ourspace.app.domain.model.CoupleSpace
import com.ourspace.app.domain.model.InvitationCode
import com.ourspace.app.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Singleton

@Singleton
class AuthPairingRepository constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val secureRandom = SecureRandom()
    private val codeAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    val currentUserUid: String?
        get() = runCatching { auth.currentUser?.uid }.getOrNull()

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = runCatching {
            firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        trySend(
                            UserProfile(
                                uid = snapshot.getString("uid") ?: uid,
                                email = snapshot.getString("email") ?: "",
                                displayName = snapshot.getString("displayName") ?: "",
                                nickname = snapshot.getString("nickname") ?: "",
                                photoUrl = snapshot.getString("photoUrl") ?: "",
                                coupleId = snapshot.getString("coupleId"),
                                partnerUid = snapshot.getString("partnerUid"),
                                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                                updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                            )
                        )
                    } else {
                        trySend(null)
                    }
                }
        }.getOrNull()
        awaitClose { registration?.remove() }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        nickname: String
    ): Result<UserProfile> = runCatching {
        require(email.contains("@")) { "Format email tidak valid." }
        require(password.length >= 8) { "Password minimal 8 karakter." }
        require(displayName.isNotBlank()) { "Display name wajib diisi." }
        require(nickname.isNotBlank()) { "Nickname panggilan wajib diisi." }

        val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = authResult.user?.uid ?: error("Gagal membuat akun pengguna.")
        val now = System.currentTimeMillis()
        val profile = UserProfile(
            uid = uid,
            email = email.trim(),
            displayName = displayName.trim(),
            nickname = nickname.trim(),
            createdAt = now,
            updatedAt = now
        )
        firestore.collection("users").document(uid).set(
            mapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "nickname" to profile.nickname,
                "photoUrl" to profile.photoUrl,
                "coupleId" to null,
                "partnerUid" to null,
                "createdAt" to now,
                "updatedAt" to now
            )
        ).await()
        profile
    }

    suspend fun loginWithEmail(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        result.user?.uid ?: error("Autentikasi gagal.")
    }

    suspend fun createInvitationCode(
        creatorUid: String,
        creatorNickname: String,
        creatorPhotoUrl: String = ""
    ): Result<InvitationCode> = runCatching {
        val codeBuilder = StringBuilder(8)
        repeat(8) {
            codeBuilder.append(codeAlphabet[secureRandom.nextInt(codeAlphabet.length)])
        }
        val code = codeBuilder.toString()
        val now = System.currentTimeMillis()
        val expiresAt = now + (24L * 60L * 60L * 1000L)
        val coupleId = "couple_${creatorUid.take(8)}_${code.take(4)}"

        val invitation = InvitationCode(
            code = code,
            creatorUid = creatorUid,
            creatorNickname = creatorNickname,
            creatorPhotoUrl = creatorPhotoUrl,
            coupleId = coupleId,
            expiresAt = expiresAt,
            isUsed = false,
            createdAt = now
        )

        firestore.runTransaction { tx ->
            val userRef = firestore.collection("users").document(creatorUid)
            val userSnap = tx.get(userRef)
            val existingCoupleId = userSnap.getString("coupleId")
            val existingPartnerUid = userSnap.getString("partnerUid")
            if (!existingCoupleId.isNullOrBlank() && !existingPartnerUid.isNullOrBlank()) {
                throw IllegalStateException("Akun Anda sudah terhubung dengan pasangan.")
            }

            val inviteRef = firestore.collection("invitationCodes").document(code)
            tx.set(
                inviteRef,
                mapOf(
                    "code" to invitation.code,
                    "creatorUid" to invitation.creatorUid,
                    "creatorNickname" to invitation.creatorNickname,
                    "creatorPhotoUrl" to invitation.creatorPhotoUrl,
                    "coupleId" to invitation.coupleId,
                    "expiresAt" to invitation.expiresAt,
                    "isUsed" to false,
                    "usedByUid" to null,
                    "createdAt" to now,
                    "serverCreatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()

        invitation
    }

    suspend fun redeemInvitationCodeAtomically(
        joinerUid: String,
        joinerNickname: String,
        joinerPhotoUrl: String,
        rawCode: String
    ): Result<CoupleSpace> = runCatching {
        val normalizedCode = rawCode.trim().uppercase()
        require(normalizedCode.length == 8) { "Kode undangan harus 8 karakter." }
        val now = System.currentTimeMillis()

        val coupleSpace = firestore.runTransaction { tx ->
            val inviteRef = firestore.collection("invitationCodes").document(normalizedCode)
            val inviteSnap = tx.get(inviteRef)

            if (!inviteSnap.exists()) {
                throw IllegalArgumentException("Kode undangan tidak ditemukan.")
            }
            if (inviteSnap.getBoolean("isUsed") == true) {
                throw IllegalStateException("Kode undangan ini sudah pernah digunakan.")
            }
            val expiresAt = inviteSnap.getLong("expiresAt") ?: 0L
            if (now > expiresAt) {
                throw IllegalStateException("Kode undangan sudah kedaluwarsa.")
            }

            val creatorUid = inviteSnap.getString("creatorUid")
                ?: throw IllegalStateException("Data pembuat kode tidak valid.")
            val creatorNickname = inviteSnap.getString("creatorNickname") ?: "Partner"
            val creatorPhotoUrl = inviteSnap.getString("creatorPhotoUrl") ?: ""
            val coupleId = inviteSnap.getString("coupleId")
                ?: "couple_${creatorUid.take(6)}_${joinerUid.take(6)}"

            if (creatorUid == joinerUid) {
                throw IllegalArgumentException("Anda tidak dapat memasukkan kode undangan milik sendiri.")
            }

            val creatorRef = firestore.collection("users").document(creatorUid)
            val joinerRef = firestore.collection("users").document(joinerUid)
            val coupleRef = firestore.collection("couples").document(coupleId)

            val creatorSnap = tx.get(creatorRef)
            val joinerSnap = tx.get(joinerRef)
            val coupleSnap = tx.get(coupleRef)

            if (!creatorSnap.getString("coupleId").isNullOrBlank()) {
                throw IllegalStateException("Pembuat kode sudah terhubung dalam pasangan lain.")
            }
            if (!joinerSnap.getString("coupleId").isNullOrBlank()) {
                throw IllegalStateException("Akun Anda sudah terhubung dalam pasangan lain.")
            }
            if (coupleSnap.exists()) {
                @Suppress("UNCHECKED_CAST")
                val currentMembers = (coupleSnap.get("memberIds") as? List<String>).orEmpty()
                if (currentMembers.size >= 2) {
                    throw IllegalStateException("Our Space ini sudah memiliki maksimal 2 anggota.")
                }
            }

            val memberIds = listOf(creatorUid, joinerUid)
            val memberNicknames = mapOf(
                creatorUid to creatorNickname,
                joinerUid to joinerNickname
            )
            val memberPhotos = mapOf(
                creatorUid to creatorPhotoUrl,
                joinerUid to joinerPhotoUrl
            )

            tx.update(
                inviteRef,
                mapOf(
                    "isUsed" to true,
                    "usedByUid" to joinerUid,
                    "usedAt" to now,
                    "serverUsedAt" to FieldValue.serverTimestamp()
                )
            )

            tx.set(
                coupleRef,
                mapOf(
                    "coupleId" to coupleId,
                    "memberIds" to memberIds,
                    "memberNicknames" to memberNicknames,
                    "memberPhotos" to memberPhotos,
                    "anniversaryDateEpochMillis" to now,
                    "tagline" to "Two Hearts. One Little Universe.",
                    "createdAt" to now,
                    "updatedAt" to now,
                    "serverCreatedAt" to FieldValue.serverTimestamp()
                )
            )

            tx.update(creatorRef, mapOf("coupleId" to coupleId, "partnerUid" to joinerUid, "updatedAt" to now))
            tx.update(joinerRef, mapOf("coupleId" to coupleId, "partnerUid" to creatorUid, "updatedAt" to now))

            CoupleSpace(
                coupleId = coupleId,
                memberIds = memberIds,
                memberNicknames = memberNicknames,
                memberPhotos = memberPhotos,
                anniversaryDateEpochMillis = now,
                createdAt = now,
                updatedAt = now
            )
        }.await()

        preferencesRepository.updateCachedCoupleSession(
            coupleId = coupleSpace.coupleId,
            partnerNickname = coupleSpace.memberNicknames.entries
                .firstOrNull { it.key != joinerUid }?.value ?: "My Love"
        )
        coupleSpace
    }

    suspend fun unpairCoupleAtomically(
        requesterUid: String,
        coupleId: String,
        confirmationPhrase: String
    ): Result<Unit> = runCatching {
        require(confirmationPhrase.trim().equals("UNPAIR", ignoreCase = true)) {
            "Ketik UNPAIR untuk mengonfirmasi pemutusan pasangan."
        }
        firestore.runTransaction { tx ->
            val coupleRef = firestore.collection("couples").document(coupleId)
            val coupleSnap = tx.get(coupleRef)
            if (!coupleSnap.exists()) return@runTransaction

            @Suppress("UNCHECKED_CAST")
            val memberIds = (coupleSnap.get("memberIds") as? List<String>).orEmpty()
            if (requesterUid !in memberIds) {
                throw IllegalAccessException("Anda bukan anggota pasangan ini.")
            }

            memberIds.forEach { uid ->
                val userRef = firestore.collection("users").document(uid)
                tx.update(userRef, mapOf("coupleId" to null, "partnerUid" to null, "updatedAt" to System.currentTimeMillis()))
            }
            tx.update(coupleRef, mapOf("isUnpaired" to true, "unpairedByUid" to requesterUid, "unpairedAt" to System.currentTimeMillis()))
        }.await()
        preferencesRepository.updateCachedCoupleSession(null, null)
    }
}
