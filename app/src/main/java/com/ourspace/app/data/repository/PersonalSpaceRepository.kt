package com.ourspace.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ourspace.app.data.local.db.PendingSyncOperationEntity
import com.ourspace.app.data.local.db.PendingSyncQueueDao
import com.ourspace.app.data.local.db.PersonalDiaryEntity
import com.ourspace.app.data.local.db.PersonalNoteEntity
import com.ourspace.app.data.local.db.PersonalSpaceDao
import com.ourspace.app.data.local.db.PersonalTaskEntity
import com.ourspace.app.data.local.db.PrivateCollectionItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Singleton

@Singleton
class PersonalSpaceRepository constructor(
    private val personalDao: PersonalSpaceDao,
    private val syncQueueDao: PendingSyncQueueDao,
    private val firestore: FirebaseFirestore
) {
    fun observeNotes(ownerUid: String, searchQuery: String, category: String): Flow<List<PersonalNoteEntity>> =
        personalDao.observePersonalNotes(ownerUid, searchQuery, category)

    suspend fun savePersonalNote(
        ownerUid: String,
        noteId: String? = null,
        title: String,
        content: String,
        category: String,
        colorHex: String,
        isPinned: Boolean,
        isFavorite: Boolean
    ): PersonalNoteEntity {
        val now = System.currentTimeMillis()
        val id = noteId ?: UUID.randomUUID().toString()
        val entity = PersonalNoteEntity(
            id = id,
            ownerUid = ownerUid,
            title = title.ifBlank { "Untitled Note" },
            content = content,
            category = category,
            colorHex = colorHex,
            isPinned = isPinned,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            syncStatus = "PENDING"
        )
        personalDao.upsertPersonalNote(entity)

        runCatching {
            firestore.collection("users").document(ownerUid)
                .collection("privateNotes").document(id)
                .set(entity).await()
            personalDao.upsertPersonalNote(entity.copy(syncStatus = "SYNCED"))
        }.onFailure {
            syncQueueDao.enqueue(
                PendingSyncOperationEntity(
                    entityType = "PERSONAL_NOTE",
                    entityId = id,
                    operation = "UPSERT",
                    payloadJson = "${entity.ownerUid}|${entity.title}|${entity.content}"
                )
            )
        }
        return entity
    }

    suspend fun deletePersonalNote(ownerUid: String, noteId: String) {
        personalDao.deletePersonalNote(noteId, ownerUid)
        runCatching {
            firestore.collection("users").document(ownerUid)
                .collection("privateNotes").document(noteId)
                .delete().await()
        }
    }

    fun observeDiary(ownerUid: String): Flow<List<PersonalDiaryEntity>> =
        personalDao.observePersonalDiary(ownerUid)

    suspend fun savePersonalDiaryEntry(
        ownerUid: String,
        entryId: String? = null,
        title: String,
        richContent: String,
        moodName: String,
        photoUrisJson: String = "[]",
        entryDateMillis: Long = System.currentTimeMillis()
    ): PersonalDiaryEntity {
        val now = System.currentTimeMillis()
        val id = entryId ?: UUID.randomUUID().toString()
        val entry = PersonalDiaryEntity(
            id = id,
            ownerUid = ownerUid,
            title = title.ifBlank { "My Daily Reflection" },
            richContent = richContent,
            moodName = moodName,
            photoUrisJson = photoUrisJson,
            entryDateMillis = entryDateMillis,
            createdAt = now,
            updatedAt = now,
            syncStatus = "SYNCED"
        )
        personalDao.upsertPersonalDiary(entry)
        runCatching {
            firestore.collection("users").document(ownerUid)
                .collection("privateDiary").document(id)
                .set(entry).await()
        }
        return entry
    }

    fun observeTasks(ownerUid: String): Flow<List<PersonalTaskEntity>> =
        personalDao.observePersonalTasks(ownerUid)

    suspend fun savePersonalTask(
        ownerUid: String,
        taskId: String? = null,
        title: String,
        priority: String,
        dueDateMillis: Long?,
        isCompleted: Boolean = false
    ): PersonalTaskEntity {
        val now = System.currentTimeMillis()
        val id = taskId ?: UUID.randomUUID().toString()
        val task = PersonalTaskEntity(
            id = id,
            ownerUid = ownerUid,
            title = title,
            priority = priority,
            dueDateMillis = dueDateMillis,
            isCompleted = isCompleted,
            createdAt = now,
            updatedAt = now,
            syncStatus = "SYNCED"
        )
        personalDao.upsertPersonalTask(task)
        runCatching {
            firestore.collection("users").document(ownerUid)
                .collection("privateTasks").document(id)
                .set(task).await()
        }
        return task
    }

    suspend fun togglePersonalTask(task: PersonalTaskEntity) {
        val updated = task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis())
        personalDao.upsertPersonalTask(updated)
    }

    fun observePrivateCollection(ownerUid: String): Flow<List<PrivateCollectionItemEntity>> =
        personalDao.observePrivateCollection(ownerUid)

    suspend fun savePrivateCollectionItem(
        ownerUid: String,
        itemType: String,
        folderName: String,
        title: String,
        bodyOrCaption: String,
        mediaUri: String? = null
    ): PrivateCollectionItemEntity {
        val now = System.currentTimeMillis()
        val item = PrivateCollectionItemEntity(
            id = UUID.randomUUID().toString(),
            ownerUid = ownerUid,
            itemType = itemType,
            folderName = folderName.ifBlank { "Private Folder" },
            title = title,
            bodyOrCaption = bodyOrCaption,
            mediaUri = mediaUri,
            createdAt = now,
            updatedAt = now
        )
        personalDao.upsertPrivateCollectionItem(item)
        return item
    }

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(("ourspace_salt_" + pin.trim()).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(inputPin: String, expectedHash: String?): Boolean {
        if (expectedHash.isNullOrBlank()) return true
        return hashPin(inputPin) == expectedHash
    }
}
