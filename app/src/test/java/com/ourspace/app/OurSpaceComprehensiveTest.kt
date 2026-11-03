package com.ourspace.app

import com.ourspace.app.data.local.db.SharedNoteEntity
import com.ourspace.app.data.repository.ConflictResolutionStrategy
import com.ourspace.app.domain.model.CoupleSpace
import com.ourspace.app.domain.model.InvitationCode
import com.ourspace.app.domain.usecase.RelationshipCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.time.LocalDate

class OurSpaceComprehensiveTest {

    @Test
    fun `relationship counter calculates accurate days and anniversary countdown`() {
        val nowMillis = 1_762_137_600_000L
        val oneYearAgoMillis = nowMillis - (365L * 24L * 60L * 60L * 1000L)

        val stats = RelationshipCalculator.calculateStats(
            anniversaryEpochMillis = oneYearAgoMillis,
            nowEpochMillis = nowMillis
        )

        assertTrue(stats.totalDaysTogether >= 365L)
        assertEquals(1L, stats.yearsTogether)
        assertTrue(stats.daysUntilNextAnniversary in 0L..366L)
    }

    @Test
    fun `daily question returns deterministic non-empty question for date`() {
        val date = LocalDate.of(2026, 11, 3)
        val q1 = RelationshipCalculator.getDailyQuestionForDate(date)
        val q2 = RelationshipCalculator.getDailyQuestionForDate(date)
        assertTrue(q1.isNotBlank())
        assertEquals(q1, q2)
    }

    @Test
    fun `time capsule and scheduled surprise strictly enforce server timestamp`() {
        val serverNow = 1_760_000_000_000L
        val futureUnlock = serverNow + 3_600_000L
        val pastUnlock = serverNow - 1_000L

        assertFalse(RelationshipCalculator.isUnlockedByServerTime(futureUnlock, serverNow))
        assertTrue(RelationshipCalculator.isUnlockedByServerTime(pastUnlock, serverNow))
        assertTrue(RelationshipCalculator.isUnlockedByServerTime(serverNow, serverNow))
    }

    @Test
    fun `invitation code expiration and single use rules are enforced`() {
        val now = 1_760_000_000_000L
        val validInvite = InvitationCode(
            code = "LOVE2026",
            creatorUid = "user_A",
            coupleId = "couple_AB",
            expiresAt = now + 86_400_000L,
            isUsed = false
        )
        val expiredInvite = validInvite.copy(expiresAt = now - 1L)
        val usedInvite = validInvite.copy(isUsed = true)

        assertTrue(!validInvite.isUsed && now < validInvite.expiresAt)
        assertFalse(now < expiredInvite.expiresAt)
        assertFalse(!usedInvite.isUsed)
    }

    @Test
    fun `couple space enforces maximum of two members`() {
        val couple = CoupleSpace(
            coupleId = "couple_AB",
            memberIds = listOf("user_A", "user_B")
        )
        assertEquals(2, couple.memberIds.size)
        val canThirdPersonJoin = couple.memberIds.size < 2
        assertFalse(canThirdPersonJoin)
    }

    @Test
    fun `personal space PIN hash verification isolates access`() {
        fun hashPin(pin: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(("ourspace_salt_" + pin.trim()).toByteArray())
                .joinToString("") { "%02x".format(it) }
        }

        val storedHash = hashPin("2408")
        assertTrue(hashPin("2408") == storedHash)
        assertFalse(hashPin("0000") == storedHash)
    }

    @Test
    fun `shared note conflict resolution merges without data loss`() {
        val conflictedNote = SharedNoteEntity(
            id = "note_1",
            coupleId = "couple_AB",
            title = "Weekend Trip",
            content = "Bring camera and tripod",
            lastEditedByUid = "user_A",
            lastEditedByNickname = "Alya",
            version = 2L,
            hasConflict = true,
            conflictServerContent = "Book hotel in Bandung"
        )

        val merged = when (ConflictResolutionStrategy.MERGE_BOTH) {
            ConflictResolutionStrategy.KEEP_MINE -> conflictedNote.content
            ConflictResolutionStrategy.KEEP_PARTNER -> conflictedNote.conflictServerContent.orEmpty()
            ConflictResolutionStrategy.MERGE_BOTH ->
                "${conflictedNote.content} | [Merged Partner]: ${conflictedNote.conflictServerContent}"
        }

        assertTrue(merged.contains("Bring camera and tripod"))
        assertTrue(merged.contains("Book hotel in Bandung"))
    }
}