package com.ourspace.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val coupleId: String? = null,
    val partnerUid: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CoupleSpace(
    val coupleId: String = "",
    val memberIds: List<String> = emptyList(),
    val memberNicknames: Map<String, String> = emptyMap(),
    val memberPhotos: Map<String, String> = emptyMap(),
    val anniversaryDateEpochMillis: Long = System.currentTimeMillis(),
    val tagline: String = "Two Hearts. One Little Universe.",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class InvitationCode(
    val code: String = "",
    val creatorUid: String = "",
    val creatorNickname: String = "",
    val creatorPhotoUrl: String = "",
    val coupleId: String = "",
    val expiresAt: Long = 0L,
    val isUsed: Boolean = false,
    val usedByUid: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MoodType(val emoji: String, val label: String) {
    IN_LOVE("🥰", "In Love"),
    HAPPY("😊", "Happy"),
    GRATEFUL("🌸", "Grateful"),
    CALM("🍵", "Calm"),
    MISSING_YOU("🥺", "Missing You"),
    TIRED("🌙", "Tired"),
    SAD("🌧️", "Need a Hug")
}

enum class TaskPriority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3)
}

@Serializable
data class NoteComment(
    val id: String = "",
    val authorUid: String = "",
    val authorNickname: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ActivityHistoryItem(
    val id: String = "",
    val actorUid: String = "",
    val actorNickname: String = "",
    val actionSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
