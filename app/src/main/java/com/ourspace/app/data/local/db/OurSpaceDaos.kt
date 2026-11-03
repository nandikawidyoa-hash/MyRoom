package com.ourspace.app.data.local.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

class PersonalSpaceDao {
    private val notesFlow = MutableStateFlow<List<PersonalNoteEntity>>(emptyList())
    private val diaryFlow = MutableStateFlow<List<PersonalDiaryEntity>>(emptyList())
    private val tasksFlow = MutableStateFlow<List<PersonalTaskEntity>>(emptyList())
    private val collectionsFlow = MutableStateFlow<List<PrivateCollectionItemEntity>>(emptyList())

    fun observePersonalNotes(uid: String, query: String, category: String): Flow<List<PersonalNoteEntity>> =
        notesFlow.map { list ->
            list.filter { it.ownerUid == uid }
                .filter { category == "All" || it.category.equals(category, ignoreCase = true) }
                .filter {
                    query.isBlank() ||
                        it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
                }
                .sortedWith(compareByDescending<PersonalNoteEntity> { it.isPinned }.thenByDescending { it.updatedAt })
        }

    suspend fun upsertPersonalNote(note: PersonalNoteEntity) {
        notesFlow.update { current ->
            current.filterNot { it.id == note.id } + note
        }
    }

    suspend fun deletePersonalNote(noteId: String, uid: String) {
        notesFlow.update { current ->
            current.filterNot { it.id == noteId && it.ownerUid == uid }
        }
    }

    fun observePersonalDiary(uid: String): Flow<List<PersonalDiaryEntity>> =
        diaryFlow.map { list ->
            list.filter { it.ownerUid == uid }
                .sortedByDescending { it.entryDateMillis }
        }

    suspend fun upsertPersonalDiary(entry: PersonalDiaryEntity) {
        diaryFlow.update { current ->
            current.filterNot { it.id == entry.id } + entry
        }
    }

    suspend fun deletePersonalDiary(entryId: String, uid: String) {
        diaryFlow.update { current ->
            current.filterNot { it.id == entryId && it.ownerUid == uid }
        }
    }

    fun observePersonalTasks(uid: String): Flow<List<PersonalTaskEntity>> =
        tasksFlow.map { list ->
            list.filter { it.ownerUid == uid }
                .sortedWith(
                    compareBy<PersonalTaskEntity> { it.isCompleted }
                        .thenBy { it.dueDateMillis ?: Long.MAX_VALUE }
                        .thenByDescending { it.updatedAt }
                )
        }

    suspend fun upsertPersonalTask(task: PersonalTaskEntity) {
        tasksFlow.update { current ->
            current.filterNot { it.id == task.id } + task
        }
    }

    suspend fun deletePersonalTask(taskId: String, uid: String) {
        tasksFlow.update { current ->
            current.filterNot { it.id == taskId && it.ownerUid == uid }
        }
    }

    fun observePrivateCollection(uid: String): Flow<List<PrivateCollectionItemEntity>> =
        collectionsFlow.map { list ->
            list.filter { it.ownerUid == uid }
                .sortedByDescending { it.updatedAt }
        }

    suspend fun upsertPrivateCollectionItem(item: PrivateCollectionItemEntity) {
        collectionsFlow.update { current ->
            current.filterNot { it.id == item.id } + item
        }
    }

    suspend fun deletePrivateCollectionItem(itemId: String, uid: String) {
        collectionsFlow.update { current ->
            current.filterNot { it.id == itemId && it.ownerUid == uid }
        }
    }
}

class SharedSpaceDao {
    private val notesFlow = MutableStateFlow<List<SharedNoteEntity>>(emptyList())
    private val diaryFlow = MutableStateFlow<List<SharedDiaryEntity>>(emptyList())
    private val tasksFlow = MutableStateFlow<List<SharedTaskEntity>>(emptyList())
    private val eventsFlow = MutableStateFlow<List<SharedEventEntity>>(emptyList())
    private val bucketFlow = MutableStateFlow<List<BucketListItemEntity>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<SharedMessageEntity>>(emptyList())
    private val memoriesFlow = MutableStateFlow<List<MemoryEntity>>(emptyList())
    private val capsulesFlow = MutableStateFlow<List<TimeCapsuleEntity>>(emptyList())
    private val surprisesFlow = MutableStateFlow<List<SurpriseItemEntity>>(emptyList())

    fun observeSharedNotes(coupleId: String): Flow<List<SharedNoteEntity>> =
        notesFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedByDescending { it.updatedAt }
        }

    suspend fun getSharedNoteById(noteId: String): SharedNoteEntity? =
        notesFlow.value.find { it.id == noteId }

    suspend fun upsertSharedNote(note: SharedNoteEntity) {
        notesFlow.update { current ->
            current.filterNot { it.id == note.id } + note
        }
    }

    suspend fun upsertSharedNotes(notes: List<SharedNoteEntity>) {
        notesFlow.update { current ->
            val newIds = notes.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + notes
        }
    }

    suspend fun deleteSharedNote(noteId: String) {
        notesFlow.update { current ->
            current.filterNot { it.id == noteId }
        }
    }

    fun observeSharedDiary(coupleId: String): Flow<List<SharedDiaryEntity>> =
        diaryFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedByDescending { it.entryDateMillis }
        }

    suspend fun upsertSharedDiary(entry: SharedDiaryEntity) {
        diaryFlow.update { current ->
            current.filterNot { it.id == entry.id } + entry
        }
    }

    suspend fun upsertSharedDiaries(entries: List<SharedDiaryEntity>) {
        diaryFlow.update { current ->
            val newIds = entries.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + entries
        }
    }

    fun observeSharedTasks(coupleId: String): Flow<List<SharedTaskEntity>> =
        tasksFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedWith(
                    compareBy<SharedTaskEntity> { it.isCompleted }
                        .thenBy { it.dueDateMillis ?: Long.MAX_VALUE }
                )
        }

    suspend fun upsertSharedTask(task: SharedTaskEntity) {
        tasksFlow.update { current ->
            current.filterNot { it.id == task.id } + task
        }
    }

    suspend fun upsertSharedTasks(tasks: List<SharedTaskEntity>) {
        tasksFlow.update { current ->
            val newIds = tasks.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + tasks
        }
    }

    suspend fun deleteSharedTask(taskId: String) {
        tasksFlow.update { current ->
            current.filterNot { it.id == taskId }
        }
    }

    fun observeSharedEvents(coupleId: String): Flow<List<SharedEventEntity>> =
        eventsFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedBy { it.eventDateMillis }
        }

    suspend fun upsertSharedEvent(event: SharedEventEntity) {
        eventsFlow.update { current ->
            current.filterNot { it.id == event.id } + event
        }
    }

    suspend fun upsertSharedEvents(events: List<SharedEventEntity>) {
        eventsFlow.update { current ->
            val newIds = events.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + events
        }
    }

    fun observeBucketList(coupleId: String): Flow<List<BucketListItemEntity>> =
        bucketFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedWith(
                    compareBy<BucketListItemEntity> { it.isCompleted }
                        .thenByDescending { it.updatedAt }
                )
        }

    suspend fun upsertBucketListItem(item: BucketListItemEntity) {
        bucketFlow.update { current ->
            current.filterNot { it.id == item.id } + item
        }
    }

    suspend fun upsertBucketListItems(items: List<BucketListItemEntity>) {
        bucketFlow.update { current ->
            val newIds = items.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + items
        }
    }

    fun observeSharedMessages(coupleId: String): Flow<List<SharedMessageEntity>> =
        messagesFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedByDescending { it.createdAt }
        }

    suspend fun upsertSharedMessage(message: SharedMessageEntity) {
        messagesFlow.update { current ->
            current.filterNot { it.id == message.id } + message
        }
    }

    suspend fun upsertSharedMessages(messages: List<SharedMessageEntity>) {
        messagesFlow.update { current ->
            val newIds = messages.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + messages
        }
    }

    fun observeMemories(coupleId: String, searchQuery: String = ""): Flow<List<MemoryEntity>> =
        memoriesFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .filter {
                    searchQuery.isBlank() ||
                        it.caption.contains(searchQuery, ignoreCase = true) ||
                        it.albumTitle.contains(searchQuery, ignoreCase = true)
                }
                .sortedByDescending { it.memoryDateMillis }
        }

    suspend fun upsertMemory(memory: MemoryEntity) {
        memoriesFlow.update { current ->
            current.filterNot { it.id == memory.id } + memory
        }
    }

    suspend fun upsertMemories(memories: List<MemoryEntity>) {
        memoriesFlow.update { current ->
            val newIds = memories.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + memories
        }
    }

    fun observeTimeCapsules(coupleId: String): Flow<List<TimeCapsuleEntity>> =
        capsulesFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedBy { it.unlockAtServerMillis }
        }

    suspend fun upsertTimeCapsule(capsule: TimeCapsuleEntity) {
        capsulesFlow.update { current ->
            current.filterNot { it.id == capsule.id } + capsule
        }
    }

    suspend fun upsertTimeCapsules(capsules: List<TimeCapsuleEntity>) {
        capsulesFlow.update { current ->
            val newIds = capsules.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + capsules
        }
    }

    fun observeSurprises(coupleId: String): Flow<List<SurpriseItemEntity>> =
        surprisesFlow.map { list ->
            list.filter { it.coupleId == coupleId }
                .sortedBy { it.scheduledRevealServerMillis }
        }

    suspend fun upsertSurprise(surprise: SurpriseItemEntity) {
        surprisesFlow.update { current ->
            current.filterNot { it.id == surprise.id } + surprise
        }
    }

    suspend fun upsertSurprises(surprises: List<SurpriseItemEntity>) {
        surprisesFlow.update { current ->
            val newIds = surprises.map { it.id }.toSet()
            current.filterNot { it.id in newIds } + surprises
        }
    }
}

class PendingSyncQueueDao {
    private val queueFlow = MutableStateFlow<List<PendingSyncOperationEntity>>(emptyList())
    private val nextOpId = AtomicLong(1L)

    suspend fun getAllPendingOperations(): List<PendingSyncOperationEntity> =
        queueFlow.value.sortedBy { it.createdAt }

    fun observePendingCount(): Flow<Int> =
        queueFlow.map { it.size }

    suspend fun enqueue(operation: PendingSyncOperationEntity) {
        val assigned = if (operation.opId <= 0L) operation.copy(opId = nextOpId.getAndIncrement()) else operation
        queueFlow.update { current ->
            current.filterNot { it.opId == assigned.opId } + assigned
        }
    }

    suspend fun deleteOperation(opId: Long) {
        queueFlow.update { current ->
            current.filterNot { it.opId == opId }
        }
    }

    suspend fun markRetry(opId: Long, error: String?) {
        queueFlow.update { current ->
            current.map {
                if (it.opId == opId) it.copy(retryCount = it.retryCount + 1, lastError = error) else it
            }
        }
    }
}
