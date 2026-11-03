package com.ourspace.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ourspace.app.data.local.db.PendingSyncQueueDao
import com.ourspace.app.data.local.db.PersonalSpaceDao
import com.ourspace.app.data.local.db.SharedSpaceDao
import com.ourspace.app.data.local.preferences.UserPreferencesRepository
import com.ourspace.app.data.repository.AuthPairingRepository
import com.ourspace.app.data.repository.PersonalSpaceRepository
import com.ourspace.app.data.repository.SharedSpaceRepository
import com.ourspace.app.ui.viewmodel.OurSpaceMainViewModel

object OurSpaceContainer {
    val personalDao: PersonalSpaceDao by lazy { PersonalSpaceDao() }
    val sharedDao: SharedSpaceDao by lazy { SharedSpaceDao() }
    val syncQueueDao: PendingSyncQueueDao by lazy { PendingSyncQueueDao() }

    @Volatile
    private var instance: OurSpaceMainViewModel? = null

    fun getViewModel(context: Context): OurSpaceMainViewModel {
        return instance ?: synchronized(this) {
            instance ?: createViewModel(context.applicationContext).also { instance = it }
        }
    }

    private fun createViewModel(appContext: Context): OurSpaceMainViewModel {
        val prefsRepo = UserPreferencesRepository(appContext)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val authRepo = AuthPairingRepository(auth, firestore, storage, prefsRepo)
        val personalRepo = PersonalSpaceRepository(personalDao, syncQueueDao, firestore)
        val sharedRepo = SharedSpaceRepository(appContext, sharedDao, syncQueueDao, firestore, storage)

        return OurSpaceMainViewModel(authRepo, personalRepo, sharedRepo, prefsRepo)
    }
}
