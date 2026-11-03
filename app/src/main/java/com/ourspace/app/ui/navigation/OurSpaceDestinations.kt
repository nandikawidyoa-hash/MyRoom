package com.ourspace.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Complete Route Hierarchy for OUR SPACE (16+ distinct screens + 5 Bottom Nav tabs).
 */
sealed class ScreenRoute(val route: String) {
    // Auth & Pairing Flow
    data object Login : ScreenRoute("auth/login")
    data object Register : ScreenRoute("auth/register")
    data object ForgotPassword : ScreenRoute("auth/forgot_password")
    data object ProfileSetup : ScreenRoute("auth/profile_setup")
    data object Pairing : ScreenRoute("auth/pairing")

    // 5 Main Bottom Navigation Tabs
    data object Home : ScreenRoute("main/home")
    data object MySpace : ScreenRoute("main/my_space")
    data object OurSpace : ScreenRoute("main/our_space")
    data object Memories : ScreenRoute("main/memories")
    data object Profile : ScreenRoute("main/profile")

    // My Space (Personal Area) Sub-Screens
    data object PersonalNotes : ScreenRoute("my_space/notes")
    data object PersonalDiary : ScreenRoute("my_space/diary")
    data object PersonalTasks : ScreenRoute("my_space/tasks")
    data object PrivateCollection : ScreenRoute("my_space/collection")
    data object PrivateSecurityLock : ScreenRoute("my_space/security_lock")

    // Our Space (Shared Area) Sub-Screens
    data object SharedNotes : ScreenRoute("our_space/notes")
    data object SharedDiary : ScreenRoute("our_space/diary")
    data object SharedTasks : ScreenRoute("our_space/tasks")
    data object SharedCalendar : ScreenRoute("our_space/calendar")
    data object SharedBucketList : ScreenRoute("our_space/bucket_list")
    data object SharedMessages : ScreenRoute("our_space/messages")

    // Relationship & Surprise Sub-Screens
    data object RelationshipHub : ScreenRoute("our_space/relationship")
    data object TimeCapsule : ScreenRoute("our_space/time_capsule")
    data object SurpriseHub : ScreenRoute("our_space/surprise")

    // Settings
    data object Settings : ScreenRoute("profile/settings")
}

enum class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(ScreenRoute.Home.route, "Home", Icons.Default.Home),
    MY_SPACE(ScreenRoute.MySpace.route, "My Space", Icons.Default.Lock),
    OUR_SPACE(ScreenRoute.OurSpace.route, "Our Space", Icons.Default.Favorite),
    MEMORIES(ScreenRoute.Memories.route, "Memories", Icons.Default.PhotoLibrary),
    PROFILE(ScreenRoute.Profile.route, "Profile", Icons.Default.Person)
}
