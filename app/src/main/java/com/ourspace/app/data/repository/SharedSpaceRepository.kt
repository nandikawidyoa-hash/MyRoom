package com.ourspace.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.ourspace.app.data.local.db.BucketListItemEntity
import com.ourspace.app.data.local.db.MemoryEntity
import com.ourspace.app.data.local.db.PendingSyncOperationEntity
import com.ourspace.app.data.local.db.PendingSyncQueueDao
import com.ourspace.app.data.local.db.SharedDiaryEntity
import com.ourspace.app.data.local.db.SharedEventEntity
import com.ourspace.app.data.local.db.SharedMessageEntity
import com.ourspace.app.data.local.db.SharedNoteEntity
import com.ourspace.app.data.local.db.SharedSpaceDao
import com.ourspace.app.data.local.db.SharedTaskEntity
import com.ourspace.app.data.local.db.SurpriseItemEntity
import com.ourspace.app.data.local.db.TimeCapsuleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class ConflictResolutionStrategy {
    KEEP_MINE,
    KEEP_PARTNER,
    MERGE_BOTH
}

@Singleton
class SharedSpaceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedDao: SharedSpaceDao,
    private val syncQueueDao: PendingSyncQueueDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeListeners = mutableListOf<ListenerRegistration>()
    private var listeningCoupleId: String? = null

    fun observeSharedNotes(coupleId: String): Flow<List<SharedNoteEntity>> =
        sharedDao.observeSharedNotes(coupleId)

    fun observeSharedDiary(coupleId: String): Flow<List<SharedDiaryEntity>> =
        sharedDao.observeSharedDiary(coupleId)

    fun observeSharedTasks(coupleId: String): Flow<List<SharedTaskEntity>> =
        sharedDao.observeSharedTasks(coupleId)

    fun observeSharedEvents(coupleId: String): Flow<List<SharedEventEntity>> =
        sharedDao.observeSharedEvents(coupleId)

    fun observeBucketList(coupleId: String): Flow<List<BucketListItemEntity>> =
        sharedDao.observeBucketList(coupleId)

    fun observeSharedMessages(coupleId: String): Flow<List<SharedMessageEntity>> =
        sharedDao.observeSharedMessages(coupleId)

    fun observeMemories(coupleId: String, query: String = ""): Flow<List<MemoryEntity>> =
        sharedDao.observeMemories(coupleId, query)

    fun observeTimeCapsules(coupleId: String): Flow<List<TimeCapsuleEntity>> =
        sharedDao.observeTimeCapsules(coupleId)

    fun observeSurprises(coupleId: String): Flow<List<SurpriseItemEntity>> =
        sharedDao.observeSurprises(coupleId)

    fun observePendingSyncCount(): Flow<Int> = syncQueueDao.observePendingCount()

    fun startRealtimeSyncForCouple(coupleId: String) {
        if (coupleId.isBlank() || listeningCoupleId == coupleId) return
        stopRealtimeSync()
        listeningCoupleId = coupleId

        val coupleDoc = firestore.collection("couples").document(coupleId)

        runCatching {
            val reg = coupleDoc.collection("sharedNotes").addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                repoScope.launch {
                    for (doc in snap.documents) {
                        val remoteVersion = doc.getLong("version") ?: 1L
                        val remoteContent = doc.getString("content") ?: ""
                        val localNote = sharedDao.getSharedNoteById(doc.id)

                        if (localNote != null && localNote.syncStatus == "PENDING") {
                            if (remoteVersion > localNote.version && remoteContent != localNote.content) {
                                sharedDao.upsertSharedNote(
                                    localNote.copy(
                                        hasConflict = true,
                                        conflictServerContent = remoteContent
                                    )
                                )
                            }
                        } else {
                            sharedDao.upsertSharedNote(
                                SharedNoteEntity(
                                    id = doc.id,
                                    coupleId = coupleId,
                                    title = doc.getString("title") ?: "Shared Note",
                                    content = remoteContent,
                                    category = doc.getString("category") ?: "Our Notes",
                                    colorHex = doc.getString("colorHex") ?: "#FDF0F6",
                                    lastEditedByUid = doc.getString("lastEditedByUid") ?: "",
                                    lastEditedByNickname = doc.getString("lastEditedByNickname") ?: "Partner",
                                    commentsJson = doc.getString("commentsJson") ?: "[]",
                                    activityHistoryJson = doc.getString("activityHistoryJson") ?: "[]",
                                    version = remoteVersion,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                                    syncStatus = "SYNCED",
                                    hasConflict = false,
                                    conflictServerContent = null
                                )
                            )
                        }
                    }
                }
            }
            activeListeners.add(reg)
        }
    }

    fun stopRealtimeSync() {
        activeListeners.forEach { runCatching { it.remove() } }
        activeListeners.clear()
        listeningCoupleId = null
    }

    suspend fun saveSharedNote(
        coupleId: String,
        noteId: String? = null,
        title: String,
        content: String,
        category: String,
        colorHex: String,
        editorUid: String,
        editorNickname: String
    ): SharedNoteEntity {
        val now = System.currentTimeMillis()
        val id = noteId ?: UUID.randomUUID().toString()
        val existing = sharedDao.getSharedNoteById(id)
        val baseVersion = existing?.version ?: 0L
        val nextVersion = baseVersion + 1L

        val localEntity = SharedNoteEntity(
            id = id,
            coupleId = coupleId,
            title = title.ifBlank { "Shared Note" },
            content = content,
            category = category,
            colorHex = colorHex,
            lastEditedByUid = editorUid,
            lastEditedByNickname = editorNickname,
            commentsJson = existing?.commentsJson ?: "[]",
            activityHistoryJson = existing?.activityHistoryJson ?: "[]",
            version = nextVersion,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            syncStatus = "PENDING",
            hasConflict = false,
            conflictServerContent = null
        )
        sharedDao.upsertSharedNote(localEntity)

        runCatching {
            val docRef = firestore.collection("couples").document(coupleId)
                .collection("sharedNotes").document(id)

            firestore.runTransaction { tx ->
                val remoteSnap = tx.get(docRef)
                if (remoteSnap.exists()) {
                    val remoteVersion = remoteSnap.getLong("version") ?: 1L
                    val remoteContent = remoteSnap.getString("content") ?: ""
                    if (remoteVersion > baseVersion && remoteContent != existing?.content) {
                        throw IllegalStateException("CONFLICT_DETECTED:$remoteContent")
                    }
                }
                tx.set(
                    docRef,
                    mapOf(
                        "id" to id,
                        "coupleId" to coupleId,
                        "title" to localEntity.title,
                        "content" to localEntity.content,
                        "category" to localEntity.category,
                        "colorHex" to localEntity.colorHex,
                        "lastEditedByUid" to editorUid,
                        "lastEditedByNickname" to editorNickname,
                        "version" to nextVersion,
                        "createdAt" to localEntity.createdAt,
                        "updatedAt" to now,
                        "serverUpdatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            sharedDao.upsertSharedNote(localEntity.copy(syncStatus = "SYNCED"))
        }.onFailure { err ->
            val msg = err.message.orEmpty()
            if (msg.startsWith("CONFLICT_DETECTED:")) {
                val remoteBody = msg.removePrefix("CONFLICT_DETECTED:")
                sharedDao.upsertSharedNote(
                    localEntity.copy(
                        hasConflict = true,
                        conflictServerContent = remoteBody,
                        syncStatus = "CONFLICT"
                    )
                )
            } else {
                syncQueueDao.enqueue(
                    PendingSyncOperationEntity(
                        entityType = "SHARED_NOTE",
                        entityId = id,
                        operation = "UPSERT",
                        payloadJson = "$coupleId|${localEntity.title}|${localEntity.content}",
                        baseVersion = baseVersion
                    )
                )
            }
        }
        return localEntity
    }

    suspend fun resolveSharedNoteConflict(
        note: SharedNoteEntity,
        strategy: ConflictResolutionStrategy,
        editorUid: String,
        editorNickname: String
    ) {
        val partnerContent = note.conflictServerContent.orEmpty()
        val resolvedContent = when (strategy) {
            ConflictResolutionStrategy.KEEP_MINE -> note.content
            ConflictResolutionStrategy.KEEP_PARTNER -> partnerContent
            ConflictResolutionStrategy.MERGE_BOTH ->
                "${note.content} | [Merged Partner]: $partnerContent"
        }
        val resolvedEntity = note.copy(
            content = resolvedContent,
            version = note.version + 1L,
            hasConflict = false,
            conflictServerContent = null,
            lastEditedByUid = editorUid,
            lastEditedByNickname = editorNickname,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )
        sharedDao.upsertSharedNote(resolvedEntity)
    }

    suspend fun saveSharedDiaryEntry(
        coupleId: String,
        authorUid: String,
        authorNickname: String,
        title: String,
        content: String,
        moodName: String
    ): SharedDiaryEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val entry = SharedDiaryEntity(
            id = id,
            coupleId = coupleId,
            authorUid = authorUid,
            authorNickname = authorNickname,
            title = title.ifBlank { "Our Daily Story" },
            content = content,
            moodName = moodName,
            entryDateMillis = now,
            createdAt = now,
            updatedAt = now,
            syncStatus = "SYNCED"
        )
        sharedDao.upsertSharedDiary(entry)
        return entry
    }

    suspend fun saveSharedTask(
        coupleId: String,
        title: String,
        category: String,
        assignedToNickname: String,
        dueDateMillis: Long?
    ): SharedTaskEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val task = SharedTaskEntity(
            id = id,
            coupleId = coupleId,
            title = title,
            category = category,
            assignedToNickname = assignedToNickname,
            dueDateMillis = dueDateMillis,
            createdAt = now,
            updatedAt = now,
            syncStatus = "SYNCED"
        )
        sharedDao.upsertSharedTask(task)
        return task
    }

    suspend fun toggleSharedTask(task: SharedTaskEntity, actorNickname: String) {
        val nextCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = nextCompleted,
            completedByNickname = if (nextCompleted) actorNickname else null,
            updatedAt = System.currentTimeMillis()
        )
        sharedDao.upsertSharedTask(updated)
    }

    suspend fun saveSharedEvent(
        coupleId: String,
        title: String,
        description: String,
        eventType: String,
        eventDateMillis: Long,
        isRecurringYearly: Boolean,
        createdByNickname: String
    ): SharedEventEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val event = SharedEventEntity(
            id = id,
            coupleId = coupleId,
            title = title,
            description = description,
            eventType = eventType,
            eventDateMillis = eventDateMillis,
            isRecurringYearly = isRecurringYearly,
            createdByNickname = createdByNickname,
            createdAt = now,
            updatedAt = now
        )
        sharedDao.upsertSharedEvent(event)
        return event
    }

    suspend fun saveBucketListItem(
        coupleId: String,
        title: String,
        description: String,
        category: String,
        progressPercent: Int = 0
    ): BucketListItemEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val item = BucketListItemEntity(
            id = id,
            coupleId = coupleId,
            title = title,
            description = description,
            category = category,
            progressPercent = progressPercent.coerceIn(0, 100),
            isCompleted = progressPercent >= 100,
            createdAt = now,
            updatedAt = now
        )
        sharedDao.upsertBucketListItem(item)
        return item
    }

    suspend fun updateBucketListProgress(item: BucketListItemEntity, newProgress: Int) {
        val clamped = newProgress.coerceIn(0, 100)
        val updated = item.copy(
            progressPercent = clamped,
            isCompleted = clamped >= 100,
            updatedAt = System.currentTimeMillis()
        )
        sharedDao.upsertBucketListItem(updated)
    }

    suspend fun sendLoveMessage(
        coupleId: String,
        senderUid: String,
        senderNickname: String,
        messageType: String,
        content: String
    ): SharedMessageEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val msg = SharedMessageEntity(
            id = id,
            coupleId = coupleId,
            senderUid = senderUid,
            senderNickname = senderNickname,
            messageType = messageType,
            content = content,
            createdAt = now,
            syncStatus = "SYNCED"
        )
        sharedDao.upsertSharedMessage(msg)
        return msg
    }

    suspend fun reactAndMarkMessageRead(message: SharedMessageEntity, emoji: String) {
        val updated = message.copy(
            reactionEmoji = emoji,
            isRead = true,
            readAtMillis = System.currentTimeMillis()
        )
        sharedDao.upsertSharedMessage(updated)
    }

    suspend fun uploadMemoryWithCompression(
        coupleId: String,
        uploaderUid: String,
        uploaderNickname: String,
        albumTitle: String,
        caption: String,
        mediaUri: Uri?,
        isFavorite: Boolean = false
    ): MemoryEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val initialMemory = MemoryEntity(
            id = id,
            coupleId = coupleId,
            albumId = albumTitle.lowercase().replace(" ", "_"),
            albumTitle = albumTitle.ifBlank { "Our Moments" },
            mediaType = "PHOTO",
            localCacheUri = mediaUri?.toString(),
            caption = caption,
            memoryDateMillis = now,
            isFavorite = isFavorite,
            uploadedByUid = uploaderUid,
            uploadedByNickname = uploaderNickname,
            uploadProgress = if (mediaUri != null) 10 else 100,
            uploadStatus = if (mediaUri != null) "UPLOADING" else "COMPLETED",
            createdAt = now
        )
        sharedDao.upsertMemory(initialMemory)

        if (mediaUri != null) {
            runCatching {
                val (compressedBytes, thumbBytes) = compressImageAndCreateThumbnail(mediaUri)
                val mainRef = storage.reference.child("couples/$coupleId/memories/${id}_full.jpg")
                val thumbRef = storage.reference.child("couples/$coupleId/memories/${id}_thumb.jpg")
                mainRef.putBytes(compressedBytes).await()
                thumbRef.putBytes(thumbBytes).await()
                val fullUrl = mainRef.downloadUrl.await().toString()
                val thumbUrl = thumbRef.downloadUrl.await().toString()

                val completed = initialMemory.copy(
                    remoteMediaUrl = fullUrl,
                    thumbnailUrl = thumbUrl,
                    albumCoverUrl = thumbUrl,
                    uploadProgress = 100,
                    uploadStatus = "COMPLETED"
                )
                sharedDao.upsertMemory(completed)
                return completed
            }.onFailure {
                val failed = initialMemory.copy(uploadStatus = "FAILED", uploadProgress = 0)
                sharedDao.upsertMemory(failed)
                return failed
            }
        }
        return initialMemory
    }

    private fun compressImageAndCreateThumbnail(uri: Uri): Pair<ByteArray, ByteArray> {
        val inputStream = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (original == null) {
            return ByteArray(0) to ByteArray(0)
        }
        val fullOut = ByteArrayOutputStream()
        original.compress(Bitmap.CompressFormat.JPEG, 82, fullOut)

        val ratio = original.width.toFloat() / original.height.coerceAtLeast(1).toFloat()
        val thumbWidth = 320
        val thumbHeight = (thumbWidth / ratio).toInt().coerceAtLeast(180)
        val thumbBitmap = Bitmap.createScaledBitmap(original, thumbWidth, thumbHeight, true)
        val thumbOut = ByteArrayOutputStream()
        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 70, thumbOut)

        return fullOut.toByteArray() to thumbOut.toByteArray()
    }

    suspend fun createTimeCapsule(
        coupleId: String,
        authorUid: String,
        authorNickname: String,
        title: String,
        sealedMessage: String,
        unlockAtServerMillis: Long
    ): TimeCapsuleEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val capsule = TimeCapsuleEntity(
            id = id,
            coupleId = coupleId,
            authorUid = authorUid,
            authorNickname = authorNickname,
            title = title,
            sealedMessage = sealedMessage,
            unlockAtServerMillis = unlockAtServerMillis,
            createdAt = now
        )
        sharedDao.upsertTimeCapsule(capsule)
        return capsule
    }

    suspend fun scheduleSurprise(
        coupleId: String,
        creatorUid: String,
        creatorNickname: String,
        surpriseType: String,
        title: String,
        content: String,
        giftCardTheme: String?,
        scheduledRevealServerMillis: Long
    ): SurpriseItemEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val surprise = SurpriseItemEntity(
            id = id,
            coupleId = coupleId,
            creatorUid = creatorUid,
            creatorNickname = creatorNickname,
            surpriseType = surpriseType,
            title = title,
            content = content,
            giftCardTheme = giftCardTheme,
            scheduledRevealServerMillis = scheduledRevealServerMillis,
            isRevealed = scheduledRevealServerMillis <= now,
            createdAt = now
        )
        sharedDao.upsertSurprise(surprise)
        return surprise
    }
}