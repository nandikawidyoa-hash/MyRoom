# OUR SPACE — Two Hearts. One Little Universe. 💞

Aplikasi Android Native eksklusif untuk pasangan (*Private Couple Application*), dibangun dengan **Kotlin**, **Jetpack Compose**, **Material 3 (Soft Romantic Design System)**, **MVVM Clean Architecture**, **Room Offline-First Caching**, **WorkManager**, **Hilt DI**, dan **Firebase (Auth, Firestore, Storage, FCM, App Check, Cloud Functions)**.

## Fitur Utama

1. **Visual Design System — Soft Romantic**:
   - Palet warna lembut (`#F6DCE8` Soft Pink, `#E8DFF5` Lavender, `#FFF8ED` Cream, `#D99AB5` Rose, `#493A46` Dark Text, `#998A96` Muted Text).
   - **5 Tema Penuh**: *Soft Romantic (Light)*, *Velvet Night (Dark)*, *Blush Rose (Pink)*, *Dreamy Lilac (Lavender)*, dan *Warm Ivory (Cream)*.
2. **Authentication & Atomic Couple Pairing**:
   - Register, Login, Forgot Password, Profile Setup, pembuatan **Invitation Code** 8 karakter (masa berlaku 24 jam, sekali pakai), verifikasi transaksi atomik maksimal 2 anggota per pasangan, dan konfirmasi eksplisit (`UNPAIR`) sebelum memutuskan tautan.
3. **My Space (Ruang Pribadi Terisolasi)**:
   - *Personal Notes* (kategori, warna kustom, pin, favorit, pencarian, autosave).
   - *Personal Diary* (rich text sederhana, pilihan mood, lampiran foto).
   - *Personal Tasks* (prioritas, tenggat waktu, checklist).
   - *Private Collection* (draft surat, foto pribadi, folder pribadi).
   - *Private Security*: Kunci PIN SHA-256 & **Biometric Lock** (`androidx.biometric`) dengan *auto-lock timeout*.
4. **Our Space (Ruang Bersama Real-Time)**:
   - *Shared Notes* dengan deteksi konflik versi & resolusi konflik (*Gunakan Versiku*, *Versi Pasangan*, atau *Gabungkan Keduanya*).
   - *Shared Diary*, *Shared Tasks & Shopping List*, *Shared Calendar*, *Shared Bucket List* (dengan slider progres), dan *Love Messages / Digital Letters* (dengan reaksi emoji & status baca).
5. **Memories, Relationship & Surprise Features**:
   - Galeri foto/video dengan kompresi otomatis & pembuatan thumbnail, album, *Memory of the Day*, *Relationship Counter*, *Anniversary Countdown*, *Daily Couple Questions*, **Digital Time Capsule**, dan **Surprise Hub** yang divalidasi dengan *Server Timestamp*.

## Cara Menjalankan & Push ke GitHub Tanpa Error

1. Buka folder `OurSpace/` di **Android Studio** dan jalankan `Run ('app')` atau `gradle assembleDebug`.
2. Untuk push ke GitHub dan otomatis menghasilkan APK di tab **GitHub Actions**:
   ```bash
   git init
   git add .
   git commit -m "feat: complete OUR SPACE Android native couple application"
   git branch -M main
   git remote add origin https://github.com/<username>/<repo-name>.git
   git push -u origin main
   ```
