package com.ourspace.app.data.local.db


data class PersonalNoteEntity(
    val id: String,
    val ownerUid: String,
    val title: String,
    val content: String,
    val category: String = "General",
    val colorHex: String = "#FDF0F6",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class PersonalDiaryEntity(
    val id: String,
    val ownerUid: String,
    val title: String,
    val richContent: String,
    val moodName: String = "HAPPY",
    val photoUrisJson: String = "[]",
    val entryDateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class PersonalTaskEntity(
    val id: String,
    val ownerUid: String,
    val title: String,
    val checklistJson: String = "[]",
    val dueDateMillis: Long? = null,
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class PrivateCollectionItemEntity(
    val id: String,
    val ownerUid: String,
    val itemType: String,
    val folderName: String = "General",
    val title: String,
    val bodyOrCaption: String,
    val mediaUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class SharedNoteEntity(
    val id: String,
    val coupleId: String,
    val title: String,
    val content: String,
    val category: String = "Our Notes",
    val colorHex: String = "#FDF0F6",
    val lastEditedByUid: String,
    val lastEditedByNickname: String,
    val commentsJson: String = "[]",
    val activityHistoryJson: String = "[]",
    val version: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED",
    val hasConflict: Boolean = false,
    val conflictServerContent: String? = null
)

data class SharedDiaryEntity(
    val id: String,
    val coupleId: String,
    val authorUid: String,
    val authorNickname: String,
    val title: String,
    val content: String,
    val moodName: String = "IN_LOVE",
    val attachmentsJson: String = "[]",
    val reactionsMapJson: String = "{}",
    val commentsJson: String = "[]",
    val entryDateMillis: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class SharedTaskEntity(
    val id: String,
    val coupleId: String,
    val title: String,
    val category: String = "Checklist",
    val assignedToUid: String? = null,
    val assignedToNickname: String = "Both",
    val dueDateMillis: Long? = null,
    val isCompleted: Boolean = false,
    val completedByNickname: String? = null,
    val version: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class SharedEventEntity(
    val id: String,
    val coupleId: String,
    val title: String,
    val description: String = "",
    val eventType: String = "SPECIAL",
    val eventDateMillis: Long,
    val reminderMinutesBefore: Int = 1440,
    val isRecurringYearly: Boolean = false,
    val createdByNickname: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class BucketListItemEntity(
    val id: String,
    val coupleId: String,
    val title: String,
    val description: String = "",
    val category: String = "Places to Visit",
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val targetDateMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class SharedMessageEntity(
    val id: String,
    val coupleId: String,
    val senderUid: String,
    val senderNickname: String,
    val messageType: String = "LOVE_NOTE",
    val content: String,
    val reactionEmoji: String? = null,
    val isRead: Boolean = false,
    val readAtMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class MemoryEntity(
    val id: String,
    val coupleId: String,
    val albumId: String = "default",
    val albumTitle: String = "Our Moments",
    val albumCoverUrl: String? = null,
    val mediaType: String = "PHOTO",
    val remoteMediaUrl: String = "",
    val thumbnailUrl: String? = null,
    val localCacheUri: String? = null,
    val caption: String,
    val memoryDateMillis: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val uploadedByUid: String,
    val uploadedByNickname: String,
    val uploadProgress: Int = 100,
    val uploadStatus: String = "COMPLETED",
    val createdAt: Long = System.currentTimeMillis()
)

data class TimeCapsuleEntity(
    val id: String,
    val coupleId: String,
    val authorUid: String,
    val authorNickname: String,
    val title: String,
    val sealedMessage: String,
    val unlockAtServerMillis: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isOpened: Boolean = false,
    val syncStatus: String = "SYNCED"
)

data class SurpriseItemEntity(
    val id: String,
    val coupleId: String,
    val creatorUid: String,
    val creatorNickname: String,
    val surpriseType: String,
    val title: String,
    val content: String,
    val giftCardTheme: String? = null,
    val scheduledRevealServerMillis: Long,
    val isRevealed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

data class PendingSyncOperationEntity(
    val opId: Long = 0L,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val baseVersion: Long = 1L,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
