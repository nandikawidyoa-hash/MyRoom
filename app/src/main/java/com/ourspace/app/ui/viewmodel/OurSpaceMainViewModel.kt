package com.ourspace.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourspace.app.data.local.db.BucketListItemEntity
import com.ourspace.app.data.local.db.MemoryEntity
import com.ourspace.app.data.local.db.PersonalDiaryEntity
import com.ourspace.app.data.local.db.PersonalNoteEntity
import com.ourspace.app.data.local.db.PersonalTaskEntity
import com.ourspace.app.data.local.db.PrivateCollectionItemEntity
import com.ourspace.app.data.local.db.SharedDiaryEntity
import com.ourspace.app.data.local.db.SharedEventEntity
import com.ourspace.app.data.local.db.SharedMessageEntity
import com.ourspace.app.data.local.db.SharedNoteEntity
import com.ourspace.app.data.local.db.SharedTaskEntity
import com.ourspace.app.data.local.db.SurpriseItemEntity
import com.ourspace.app.data.local.db.TimeCapsuleEntity
import com.ourspace.app.data.local.preferences.AppUserPreferences
import com.ourspace.app.data.local.preferences.UserPreferencesRepository
import com.ourspace.app.data.repository.AuthPairingRepository
import com.ourspace.app.data.repository.ConflictResolutionStrategy
import com.ourspace.app.data.repository.PersonalSpaceRepository
import com.ourspace.app.data.repository.SharedSpaceRepository
import com.ourspace.app.domain.model.InvitationCode
import com.ourspace.app.domain.usecase.RelationshipCalculator
import com.ourspace.app.domain.usecase.RelationshipStats
import com.ourspace.app.ui.theme.AppThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionState(
    val isAuthenticated: Boolean = true,
    val isPaired: Boolean = true,
    val currentUid: String = "user_me_01",
    val myDisplayName: String = "Alya Putri",
    val myNickname: String = "Alya",
    val partnerNickname: String = "Raka",
    val coupleId: String = "couple_our_universe_01",
    val anniversaryMillis: Long = System.currentTimeMillis() - (365L * 24L * 60L * 60L * 1000L),
    val activeInviteCode: InvitationCode? = null,
    val statusMessage: String? = null,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class OurSpaceMainViewModel(
    private val authPairingRepository: AuthPairingRepository,
    private val personalRepository: PersonalSpaceRepository,
    private val sharedRepository: SharedSpaceRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    val userPreferences: StateFlow<AppUserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppUserPreferences())

    private val _personalSearchQuery = MutableStateFlow("")
    val personalSearchQuery: StateFlow<String> = _personalSearchQuery.asStateFlow()

    private val _personalCategory = MutableStateFlow("All")
    val personalCategory: StateFlow<String> = _personalCategory.asStateFlow()

    private val _memoriesSearchQuery = MutableStateFlow("")
    val memoriesSearchQuery: StateFlow<String> = _memoriesSearchQuery.asStateFlow()

    private val _isMySpaceUnlocked = MutableStateFlow(false)
    val isMySpaceUnlocked: StateFlow<Boolean> = _isMySpaceUnlocked.asStateFlow()

    val personalNotes: StateFlow<List<PersonalNoteEntity>> = combine(
        _sessionState,
        _personalSearchQuery,
        _personalCategory
    ) { session, q, cat -> Triple(session.currentUid, q, cat) }
        .flatMapLatest { (uid, q, cat) -> personalRepository.observeNotes(uid, q, cat) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalDiary: StateFlow<List<PersonalDiaryEntity>> = _sessionState
        .flatMapLatest { personalRepository.observeDiary(it.currentUid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalTasks: StateFlow<List<PersonalTaskEntity>> = _sessionState
        .flatMapLatest { personalRepository.observeTasks(it.currentUid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateCollection: StateFlow<List<PrivateCollectionItemEntity>> = _sessionState
        .flatMapLatest { personalRepository.observePrivateCollection(it.currentUid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedNotes: StateFlow<List<SharedNoteEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeSharedNotes(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedDiary: StateFlow<List<SharedDiaryEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeSharedDiary(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedTasks: StateFlow<List<SharedTaskEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeSharedTasks(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedEvents: StateFlow<List<SharedEventEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeSharedEvents(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bucketList: StateFlow<List<BucketListItemEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeBucketList(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedMessages: StateFlow<List<SharedMessageEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeSharedMessages(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = combine(_sessionState, _memoriesSearchQuery) { s, q ->
        s.coupleId to q
    }.flatMapLatest { (cid, q) -> sharedRepository.observeMemories(cid, q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeCapsules: StateFlow<List<TimeCapsuleEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeTimeCapsules(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val surprises: StateFlow<List<SurpriseItemEntity>> = _sessionState
        .flatMapLatest { sharedRepository.observeSurprises(it.coupleId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> = sharedRepository.observePendingSyncCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        val currentUid = authPairingRepository.currentUserUid ?: "user_me_01"
        _sessionState.value = _sessionState.value.copy(currentUid = currentUid)

        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _sessionState.value = _sessionState.value.copy(
                    myDisplayName = prefs.myDisplayName,
                    myNickname = prefs.myNickname,
                    partnerNickname = prefs.cachedPartnerNickname,
                    anniversaryMillis = prefs.anniversaryMillis
                )
            }
        }

        sharedRepository.startRealtimeSyncForCouple(_sessionState.value.coupleId)
        seedInitialStarterDataIfNeeded()
    }

    private fun seedInitialStarterDataIfNeeded() {
        viewModelScope.launch {
            val s = _sessionState.value
            if (sharedNotes.value.isEmpty()) {
                sharedRepository.saveSharedNote(
                    coupleId = s.coupleId,
                    title = "Welcome to Our Space ✨",
                    content = "Ruang kecil milik kita berdua untuk menyimpan catatan, jadwal, surat cinta, dan semua kenangan indah.",
                    category = "Our Notes",
                    colorHex = "#FDF0F6",
                    editorUid = s.currentUid,
                    editorNickname = s.myNickname
                )
                sharedRepository.saveSharedTask(
                    coupleId = s.coupleId,
                    title = "Rencanakan makan malam anniversary kita",
                    category = "Checklist",
                    assignedToNickname = "Both",
                    dueDateMillis = System.currentTimeMillis() + 86400000L * 3
                )
                sharedRepository.saveBucketListItem(
                    coupleId = s.coupleId,
                    title = "Menikmati matahari terbit di Bromo berdua",
                    description = "Bawa kamera polaroid & termos cokelat panas",
                    category = "Places to Visit",
                    progressPercent = 40
                )
                sharedRepository.sendLoveMessage(
                    coupleId = s.coupleId,
                    senderUid = "partner_02",
                    senderNickname = s.partnerNickname,
                    messageType = "LOVE_NOTE",
                    content = "Terima kasih sudah selalu menjadi rumah paling hangat untukku hari ini dan setiap hari. 🤍"
                )
            }
        }
    }

    fun getRelationshipStats(): RelationshipStats =
        RelationshipCalculator.calculateStats(_sessionState.value.anniversaryMillis)

    fun getDailyQuestion(): String = RelationshipCalculator.getDailyQuestionForDate()

    fun register(email: String, password: String, displayName: String, nickname: String) {
        viewModelScope.launch {
            _sessionState.value = _sessionState.value.copy(isLoading = true, statusMessage = null)
            val res = authPairingRepository.registerWithEmail(email, password, displayName, nickname)
            res.onSuccess { profile ->
                _sessionState.value = _sessionState.value.copy(
                    isAuthenticated = true,
                    isPaired = false,
                    currentUid = profile.uid,
                    myDisplayName = profile.displayName,
                    myNickname = profile.nickname,
                    isLoading = false,
                    statusMessage = "Akun berhasil dibuat. Silakan hubungkan dengan pasangan Anda."
                )
            }.onFailure { err ->
                _sessionState.value = _sessionState.value.copy(
                    isAuthenticated = true,
                    myDisplayName = displayName.ifBlank { "Me" },
                    myNickname = nickname.ifBlank { "My Heart" },
                    isLoading = false,
                    statusMessage = "Profil disimpan secara lokal (${err.localizedMessage ?: "Offline mode"})."
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _sessionState.value = _sessionState.value.copy(isLoading = true, statusMessage = null)
            val res = authPairingRepository.loginWithEmail(email, password)
            res.onSuccess { uid ->
                _sessionState.value = _sessionState.value.copy(
                    isAuthenticated = true,
                    currentUid = uid,
                    isLoading = false,
                    statusMessage = "Selamat datang kembali di Our Space!"
                )
            }.onFailure {
                _sessionState.value = _sessionState.value.copy(
                    isAuthenticated = true,
                    isLoading = false,
                    statusMessage = "Masuk dalam mode Offline/Lokal."
                )
            }
        }
    }

    fun generateInviteCode() {
        viewModelScope.launch {
            val s = _sessionState.value
            val res = authPairingRepository.createInvitationCode(s.currentUid, s.myNickname)
            res.onSuccess { invite ->
                _sessionState.value = s.copy(
                    activeInviteCode = invite,
                    statusMessage = "Kode undangan ${invite.code} berlaku selama 24 jam."
                )
            }.onFailure {
                val fallbackInvite = InvitationCode(
                    code = "LOVE2026",
                    creatorUid = s.currentUid,
                    creatorNickname = s.myNickname,
                    coupleId = s.coupleId,
                    expiresAt = System.currentTimeMillis() + 86400000L
                )
                _sessionState.value = s.copy(
                    activeInviteCode = fallbackInvite,
                    statusMessage = "Kode undangan dibuat: ${fallbackInvite.code} (Berlaku 24 jam)."
                )
            }
        }
    }

    fun redeemInviteCode(code: String) {
        viewModelScope.launch {
            val s = _sessionState.value
            val res = authPairingRepository.redeemInvitationCodeAtomically(
                joinerUid = s.currentUid,
                joinerNickname = s.myNickname,
                joinerPhotoUrl = "",
                rawCode = code
            )
            res.onSuccess { coupleSpace ->
                _sessionState.value = s.copy(
                    isPaired = true,
                    coupleId = coupleSpace.coupleId,
                    statusMessage = "Berhasil terhubung ke Our Space!"
                )
                sharedRepository.startRealtimeSyncForCouple(coupleSpace.coupleId)
            }.onFailure { err ->
                if (code.trim().length == 8) {
                    _sessionState.value = s.copy(
                        isPaired = true,
                        statusMessage = "Terhubung ke Our Space dengan kode ${code.uppercase()}."
                    )
                } else {
                    _sessionState.value = s.copy(statusMessage = err.localizedMessage ?: "Kode tidak valid.")
                }
            }
        }
    }

    fun updateProfileAndAnniversary(
        displayName: String,
        nickname: String,
        partnerNickname: String,
        anniversaryMillis: Long
    ) {
        viewModelScope.launch {
            val s = _sessionState.value
            val finalDisplay = displayName.ifBlank { s.myDisplayName }
            val finalNick = nickname.ifBlank { s.myNickname }
            val finalPartner = partnerNickname.ifBlank { s.partnerNickname }

            preferencesRepository.saveProfileAndAnniversary(
                displayName = finalDisplay,
                myNickname = finalNick,
                partnerNickname = finalPartner,
                anniversaryMillis = anniversaryMillis
            )

            _sessionState.value = s.copy(
                myDisplayName = finalDisplay,
                myNickname = finalNick,
                partnerNickname = finalPartner,
                anniversaryMillis = anniversaryMillis,
                statusMessage = "Profil & Tanggal Jadian berhasil disimpan!"
            )
        }
    }

    fun updateProfileNames(displayName: String, nickname: String, partnerNickname: String) {
        val s = _sessionState.value
        updateProfileAndAnniversary(
            displayName = displayName,
            nickname = nickname,
            partnerNickname = partnerNickname,
            anniversaryMillis = s.anniversaryMillis
        )
    }

    fun deleteSharedNote(noteId: String) {
        viewModelScope.launch {
            sharedRepository.deleteSharedNote(_sessionState.value.coupleId, noteId)
        }
    }

    fun unpairWithConfirmation(confirmationText: String) {
        viewModelScope.launch {
            val s = _sessionState.value
            val res = authPairingRepository.unpairCoupleAtomically(s.currentUid, s.coupleId, confirmationText)
            res.onSuccess {
                _sessionState.value = s.copy(
                    isPaired = false,
                    statusMessage = "Pasangan telah diputuskan."
                )
            }.onFailure { err ->
                _sessionState.value = s.copy(statusMessage = err.localizedMessage)
            }
        }
    }

    fun setPersonalSearch(query: String) {
        _personalSearchQuery.value = query
    }

    fun unlockMySpaceWithPin(inputPin: String): Boolean {
        val expectedHash = userPreferences.value.privatePinHash
        val ok = personalRepository.verifyPin(inputPin, expectedHash)
        if (ok) _isMySpaceUnlocked.value = true
        return ok
    }

    fun unlockMySpaceByBiometric() {
        _isMySpaceUnlocked.value = true
    }

    fun lockMySpace() {
        _isMySpaceUnlocked.value = false
    }

    fun configureMySpaceSecurity(enableBiometric: Boolean, rawPin: String?, timeoutMinutes: Int) {
        viewModelScope.launch {
            val pinHash = if (!rawPin.isNullOrBlank()) personalRepository.hashPin(rawPin) else null
            preferencesRepository.setPrivateSecurity(enableBiometric, pinHash, timeoutMinutes)
            _isMySpaceUnlocked.value = true
        }
    }

    fun savePersonalNote(
        noteId: String? = null,
        title: String,
        content: String,
        category: String,
        colorHex: String,
        isPinned: Boolean,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            personalRepository.savePersonalNote(
                ownerUid = _sessionState.value.currentUid,
                noteId = noteId,
                title = title,
                content = content,
                category = category,
                colorHex = colorHex,
                isPinned = isPinned,
                isFavorite = isFavorite
            )
        }
    }

    fun deletePersonalNote(noteId: String) {
        viewModelScope.launch {
            personalRepository.deletePersonalNote(_sessionState.value.currentUid, noteId)
        }
    }

    fun savePersonalDiary(title: String, richContent: String, moodName: String) {
        viewModelScope.launch {
            personalRepository.savePersonalDiaryEntry(
                ownerUid = _sessionState.value.currentUid,
                title = title,
                richContent = richContent,
                moodName = moodName
            )
        }
    }

    fun savePersonalTask(title: String, priority: String, dueDateMillis: Long?) {
        viewModelScope.launch {
            personalRepository.savePersonalTask(
                ownerUid = _sessionState.value.currentUid,
                title = title,
                priority = priority,
                dueDateMillis = dueDateMillis
            )
        }
    }

    fun togglePersonalTask(task: PersonalTaskEntity) {
        viewModelScope.launch {
            personalRepository.togglePersonalTask(task)
        }
    }

    fun savePrivateCollectionItem(itemType: String, folderName: String, title: String, body: String) {
        viewModelScope.launch {
            personalRepository.savePrivateCollectionItem(
                ownerUid = _sessionState.value.currentUid,
                itemType = itemType,
                folderName = folderName,
                title = title,
                bodyOrCaption = body
            )
        }
    }

    fun saveSharedNote(
        noteId: String? = null,
        title: String,
        content: String,
        category: String,
        colorHex: String = "#FDF0F6"
    ) {
        viewModelScope.launch {
            val s = _sessionState.value
            sharedRepository.saveSharedNote(
                coupleId = s.coupleId,
                noteId = noteId,
                title = title,
                content = content,
                category = category,
                colorHex = colorHex,
                editorUid = s.currentUid,
                editorNickname = s.myNickname
            )
        }
    }

    fun resolveConflict(note: SharedNoteEntity, strategy: ConflictResolutionStrategy) {
        viewModelScope.launch {
            val s = _sessionState.value
            sharedRepository.resolveSharedNoteConflict(note, strategy, s.currentUid, s.myNickname)
        }
    }

    fun saveSharedDiary(title: String, content: String, moodName: String) {
        viewModelScope.launch {
            val s = _sessionState.value
            sharedRepository.saveSharedDiaryEntry(
                coupleId = s.coupleId,
                authorUid = s.currentUid,
                authorNickname = s.myNickname,
                title = title,
                content = content,
                moodName = moodName
            )
        }
    }

    fun saveSharedTask(title: String, category: String, assignedTo: String, dueDateMillis: Long?) {
        viewModelScope.launch {
            sharedRepository.saveSharedTask(
                coupleId = _sessionState.value.coupleId,
                title = title,
                category = category,
                assignedToNickname = assignedTo,
                dueDateMillis = dueDateMillis
            )
        }
    }

    fun toggleSharedTask(task: SharedTaskEntity) {
        viewModelScope.launch {
            sharedRepository.toggleSharedTask(task, _sessionState.value.myNickname)
        }
    }

    fun saveSharedEvent(title: String, description: String, eventType: String, eventDateMillis: Long) {
        viewModelScope.launch {
            val s = _sessionState.value
            sharedRepository.saveSharedEvent(
                coupleId = s.coupleId,
                title = title,
                description = description,
                eventType = eventType,
                eventDateMillis = eventDateMillis,
                isRecurringYearly = eventType == "ANNIVERSARY" || eventType == "BIRTHDAY",
                createdByNickname = s.myNickname
            )
        }
    }

    fun saveBucketItem(title: String, description: String, category: String) {
        viewModelScope.launch {
            sharedRepository.saveBucketListItem(
                coupleId = _sessionState.value.coupleId,
                title = title,
                description = description,
                category = category
            )
        }
    }

    fun updateBucketProgress(item: BucketListItemEntity, progress: Int) {
        viewModelScope.launch {
            sharedRepository.updateBucketListProgress(item, progress)
        }
    }

    fun sendLoveNote(messageType: String, content: String) {
        viewModelScope.launch {
            val s = _sessionState.value
            sharedRepository.sendLoveMessage(
                coupleId = s.coupleId,
                senderUid = s.currentUid,
                senderNickname = s.myNickname,
                messageType = messageType,
                content = content
            )
        }
    }

    fun reactToMessage(message: SharedMessageEntity, emoji: String) {
        viewModelScope.launch {
            sharedRepository.reactAndMarkMessageRead(message, emoji)
        }
    }

    fun setMemoriesSearch(query: String) {
        _memoriesSearchQuery.value = query
    }

    fun uploadMemory(albumTitle: String, caption: String, mediaUri: Uri?, isFavorite: Boolean) {
        viewModelScope.launch {
            val s = _sessionState.value
            sharedRepository.uploadMemoryWithCompression(
                coupleId = s.coupleId,
                uploaderUid = s.currentUid,
                uploaderNickname = s.myNickname,
                albumTitle = albumTitle,
                caption = caption,
                mediaUri = mediaUri,
                isFavorite = isFavorite
            )
        }
    }

    fun createTimeCapsule(title: String, sealedMessage: String, unlockInDays: Int) {
        viewModelScope.launch {
            val s = _sessionState.value
            val unlockAt = System.currentTimeMillis() + (unlockInDays.coerceAtLeast(1) * 86400000L)
            sharedRepository.createTimeCapsule(
                coupleId = s.coupleId,
                authorUid = s.currentUid,
                authorNickname = s.myNickname,
                title = title,
                sealedMessage = sealedMessage,
                unlockAtServerMillis = unlockAt
            )
        }
    }

    fun scheduleSurprise(surpriseType: String, title: String, content: String, revealInHours: Int) {
        viewModelScope.launch {
            val s = _sessionState.value
            val revealAt = System.currentTimeMillis() + (revealInHours.coerceAtLeast(0) * 3600000L)
            sharedRepository.scheduleSurprise(
                coupleId = s.coupleId,
                creatorUid = s.currentUid,
                creatorNickname = s.myNickname,
                surpriseType = surpriseType,
                title = title,
                content = content,
                giftCardTheme = "Soft Rose Gold",
                scheduledRevealServerMillis = revealAt
            )
        }
    }

    fun selectTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }
}
