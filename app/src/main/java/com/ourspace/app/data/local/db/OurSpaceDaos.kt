package com.ourspace.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalSpaceDao {
    @Query(
        "SELECT * FROM personal_notes WHERE ownerUid = :uid AND (:query = '' OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND (:category = 'All' OR category = :category) ORDER BY isPinned DESC, updatedAt DESC"
    )
    fun observePersonalNotes(uid: String, query: String, category: String): Flow<List<PersonalNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersonalNote(note: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes WHERE id = :noteId AND ownerUid = :uid")
    suspend fun deletePersonalNote(noteId: String, uid: String)

    @Query("SELECT * FROM personal_diary_entries WHERE ownerUid = :uid ORDER BY entryDateMillis DESC")
    fun observePersonalDiary(uid: String): Flow<List<PersonalDiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersonalDiary(entry: PersonalDiaryEntity)

    @Query("DELETE FROM personal_diary_entries WHERE id = :entryId AND ownerUid = :uid")
    suspend fun deletePersonalDiary(entryId: String, uid: String)

    @Query("SELECT * FROM personal_tasks WHERE ownerUid = :uid ORDER BY isCompleted ASC, COALESCE(dueDateMillis, 9223372036854775807) ASC, updatedAt DESC")
    fun observePersonalTasks(uid: String): Flow<List<PersonalTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersonalTask(task: PersonalTaskEntity)

    @Query("DELETE FROM personal_tasks WHERE id = :taskId AND ownerUid = :uid")
    suspend fun deletePersonalTask(taskId: String, uid: String)

    @Query("SELECT * FROM private_collections WHERE ownerUid = :uid ORDER BY updatedAt DESC")
    fun observePrivateCollection(uid: String): Flow<List<PrivateCollectionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrivateCollectionItem(item: PrivateCollectionItemEntity)

    @Query("DELETE FROM private_collections WHERE id = :itemId AND ownerUid = :uid")
    suspend fun deletePrivateCollectionItem(itemId: String, uid: String)
}

@Dao
interface SharedSpaceDao {
    @Query("SELECT * FROM shared_notes WHERE coupleId = :coupleId ORDER BY updatedAt DESC")
    fun observeSharedNotes(coupleId: String): Flow<List<SharedNoteEntity>>

    @Query("SELECT * FROM shared_notes WHERE id = :noteId LIMIT 1")
    suspend fun getSharedNoteById(noteId: String): SharedNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedNote(note: SharedNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedNotes(notes: List<SharedNoteEntity>)

    @Query("DELETE FROM shared_notes WHERE id = :noteId")
    suspend fun deleteSharedNote(noteId: String)

    @Query("SELECT * FROM shared_diary WHERE coupleId = :coupleId ORDER BY entryDateMillis DESC")
    fun observeSharedDiary(coupleId: String): Flow<List<SharedDiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedDiary(entry: SharedDiaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedDiaries(entries: List<SharedDiaryEntity>)

    @Query("SELECT * FROM shared_tasks WHERE coupleId = :coupleId ORDER BY isCompleted ASC, COALESCE(dueDateMillis, 9223372036854775807) ASC")
    fun observeSharedTasks(coupleId: String): Flow<List<SharedTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedTask(task: SharedTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedTasks(tasks: List<SharedTaskEntity>)

    @Query("DELETE FROM shared_tasks WHERE id = :taskId")
    suspend fun deleteSharedTask(taskId: String)

    @Query("SELECT * FROM shared_events WHERE coupleId = :coupleId ORDER BY eventDateMillis ASC")
    fun observeSharedEvents(coupleId: String): Flow<List<SharedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedEvent(event: SharedEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedEvents(events: List<SharedEventEntity>)

    @Query("SELECT * FROM shared_bucket_list WHERE coupleId = :coupleId ORDER BY isCompleted ASC, updatedAt DESC")
    fun observeBucketList(coupleId: String): Flow<List<BucketListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBucketListItem(item: BucketListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBucketListItems(items: List<BucketListItemEntity>)

    @Query("SELECT * FROM shared_messages WHERE coupleId = :coupleId ORDER BY createdAt DESC")
    fun observeSharedMessages(coupleId: String): Flow<List<SharedMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedMessage(message: SharedMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSharedMessages(messages: List<SharedMessageEntity>)

    @Query(
        "SELECT * FROM memories WHERE coupleId = :coupleId AND (:searchQuery = '' OR caption LIKE '%' || :searchQuery || '%' OR albumTitle LIKE '%' || :searchQuery || '%') ORDER BY memoryDateMillis DESC"
    )
    fun observeMemories(coupleId: String, searchQuery: String = ""): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMemories(memories: List<MemoryEntity>)

    @Query("SELECT * FROM time_capsules WHERE coupleId = :coupleId ORDER BY unlockAtServerMillis ASC")
    fun observeTimeCapsules(coupleId: String): Flow<List<TimeCapsuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTimeCapsule(capsule: TimeCapsuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTimeCapsules(capsules: List<TimeCapsuleEntity>)

    @Query("SELECT * FROM surprise_items WHERE coupleId = :coupleId ORDER BY scheduledRevealServerMillis ASC")
    fun observeSurprises(coupleId: String): Flow<List<SurpriseItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSurprise(surprise: SurpriseItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSurprises(surprises: List<SurpriseItemEntity>)
}

@Dao
interface PendingSyncQueueDao {
    @Query("SELECT * FROM pending_sync_queue ORDER BY createdAt ASC")
    suspend fun getAllPendingOperations(): List<PendingSyncOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_sync_queue")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(operation: PendingSyncOperationEntity)

    @Query("DELETE FROM pending_sync_queue WHERE opId = :opId")
    suspend fun deleteOperation(opId: Long)

    @Query("UPDATE pending_sync_queue SET retryCount = retryCount + 1, lastError = :error WHERE opId = :opId")
    suspend fun markRetry(opId: Long, error: String?)
}
