# OUR SPACE — Firestore & Room Database Architecture

## 1. Struktur Koleksi Cloud Firestore

- `users/{userId}`: Profil pengguna (`uid`, `email`, `displayName`, `nickname`, `photoUrl`, `coupleId`, `partnerUid`, `fcmToken`).
  - `users/{userId}/privateNotes/{noteId}`: Catatan pribadi (diisolasi ketat dengan aturan `request.auth.uid == userId`).
  - `users/{userId}/privateDiary/{entryId}`: Jurnal harian pribadi.
  - `users/{userId}/privateTasks/{taskId}`: Daftar tugas pribadi.
  - `users/{userId}/privateCollection/{itemId}`: Koleksi draft surat & folder pribadi.
- `invitationCodes/{code}`: Kode pairing 8 karakter dengan atribut `creatorUid`, `coupleId`, `expiresAt`, `isUsed`, `usedByUid`.
- `couples/{coupleId}`: Dokumen ruang pasangan (`memberIds` maksimal 2 UID, `memberNicknames`, `anniversaryDateEpochMillis`).
  - `couples/{coupleId}/sharedNotes/{noteId}`: Catatan kolaboratif dengan atribut `version` untuk deteksi konflik perubahan.
  - `couples/{coupleId}/sharedDiary/{entryId}`: Jurnal bersama & mood.
  - `couples/{coupleId}/tasks/{taskId}`: Tugas bersama & daftar belanja.
  - `couples/{coupleId}/events/{eventId}`: Kalender pasangan, anniversary, ulang tahun.
  - `couples/{coupleId}/bucketList/{itemId}`: Daftar impian bersama & persentase progres (`0..100`).
  - `couples/{coupleId}/memories/{memoryId}`: Metadata foto/video kenangan beserta URL thumbnail dari Firebase Storage.
  - `couples/{coupleId}/letters/{letterId}`: Love notes, surat digital, reaksi emoji, dan status baca.
  - `couples/{coupleId}/timeCapsules/{capsuleId}`: Pesan masa depan yang dikunci hingga `request.time.toMillis() >= resource.data.unlockAtServerMillis`.
  - `couples/{coupleId}/surprises/{surpriseId}`: Kejutan terjadwal yang dikunci hingga waktu server tercapai.
