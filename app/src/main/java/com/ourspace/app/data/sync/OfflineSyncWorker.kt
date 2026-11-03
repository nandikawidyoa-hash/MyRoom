package com.ourspace.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ourspace.app.di.OurSpaceContainer
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class OfflineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val syncQueueDao = OurSpaceContainer.syncQueueDao
        val sharedSpaceDao = OurSpaceContainer.sharedDao
        val firestore = FirebaseFirestore.getInstance()

        val operations = syncQueueDao.getAllPendingOperations()
        if (operations.isEmpty()) return Result.success()

        var anyTransientFailure = false

        for (op in operations) {
            runCatching {
                when (op.entityType) {
                    "SHARED_NOTE" -> {
                        val localNote = sharedSpaceDao.getSharedNoteById(op.entityId)
                        if (localNote != null) {
                            val docRef = firestore.collection("couples").document(localNote.coupleId)
                                .collection("sharedNotes").document(localNote.id)
                            firestore.runTransaction { tx ->
                                val snap = tx.get(docRef)
                                val remoteVersion = snap.getLong("version") ?: 0L
                                val remoteContent = snap.getString("content") ?: ""
                                if (snap.exists() && remoteVersion > op.baseVersion && remoteContent != localNote.content) {
                                    throw IllegalStateException("CONFLICT:$remoteContent")
                                }
                                tx.set(
                                    docRef,
                                    mapOf(
                                        "id" to localNote.id,
                                        "coupleId" to localNote.coupleId,
                                        "title" to localNote.title,
                                        "content" to localNote.content,
                                        "category" to localNote.category,
                                        "colorHex" to localNote.colorHex,
                                        "lastEditedByUid" to localNote.lastEditedByUid,
                                        "lastEditedByNickname" to localNote.lastEditedByNickname,
                                        "version" to localNote.version,
                                        "updatedAt" to localNote.updatedAt,
                                        "serverUpdatedAt" to FieldValue.serverTimestamp()
                                    )
                                )
                            }.await()
                            sharedSpaceDao.upsertSharedNote(localNote.copy(syncStatus = "SYNCED"))
                        }
                    }
                    else -> Unit
                }
                syncQueueDao.deleteOperation(op.opId)
            }.onFailure { err ->
                val msg = err.message.orEmpty()
                if (msg.startsWith("CONFLICT:")) {
                    val serverContent = msg.removePrefix("CONFLICT:")
                    val localNote = sharedSpaceDao.getSharedNoteById(op.entityId)
                    if (localNote != null) {
                        sharedSpaceDao.upsertSharedNote(
                            localNote.copy(
                                hasConflict = true,
                                conflictServerContent = serverContent,
                                syncStatus = "CONFLICT"
                            )
                        )
                    }
                    syncQueueDao.deleteOperation(op.opId)
                } else {
                    syncQueueDao.markRetry(op.opId, msg)
                    anyTransientFailure = true
                }
            }
        }

        return if (anyTransientFailure) Result.retry() else Result.success()
    }

    companion object {
        private const val UNIQUE_PERIODIC_WORK = "ourspace_periodic_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<OfflineSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
