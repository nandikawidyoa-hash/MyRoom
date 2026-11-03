package com.ourspace.app.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_notes",
    indices = [Index("ownerUid"), Index("category"), Index("isPinned")]
)
data class PersonalNoteEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "personal_diary_entries",
    indices = [Index("ownerUid"), Index("entryDateMillis")]
)
data class PersonalDiaryEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "personal_tasks",
    indices = [Index("ownerUid"), Index("isCompleted")]
)
data class PersonalTaskEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "private_collections",
    indices = [Index("ownerUid"), Index("folderName")]
)
data class PrivateCollectionItemEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "shared_notes",
    indices = [Index("coupleId"), Index("updatedAt")]
)
data class SharedNoteEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "shared_diary",
    indices = [Index("coupleId"), Index("entryDateMillis")]
)
data class SharedDiaryEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "shared_tasks",
    indices = [Index("coupleId"), Index("isCompleted")]
)
data class SharedTaskEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "shared_events",
    indices = [Index("coupleId"), Index("eventDateMillis")]
)
data class SharedEventEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "shared_bucket_list",
    indices = [Index("coupleId"), Index("category")]
)
data class BucketListItemEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "shared_messages",
    indices = [Index("coupleId"), Index("createdAt")]
)
data class SharedMessageEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "memories",
    indices = [Index("coupleId"), Index("memoryDateMillis"), Index("albumId")]
)
data class MemoryEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "time_capsules",
    indices = [Index("coupleId"), Index("unlockAtServerMillis")]
)
data class TimeCapsuleEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "surprise_items",
    indices = [Index("coupleId"), Index("scheduledRevealServerMillis")]
)
data class SurpriseItemEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "pending_sync_queue",
    indices = [Index("createdAt")]
)
data class PendingSyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val opId: Long = 0L,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val baseVersion: Long = 1L,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
