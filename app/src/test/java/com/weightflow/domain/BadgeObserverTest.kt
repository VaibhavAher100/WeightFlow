package com.weightflow.domain

import app.cash.turbine.test
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: RFC #24 — BadgeObserver wraps BadgeEngine reactively.
 * Written before BadgeObserverImpl exists.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BadgeObserverTest {

    private val weightRepo = mockk<WeightRepository>()
    private val profileRepo = mockk<UserProfileRepository>()
    private val dataStore = mockk<UserPrefsDataStore>(relaxed = true)

    private val baseProfile = UserProfile(
        id = 1L,
        displayName = "Vaibhav",
        goalWeightKg = 75.0,
        targetDate = null,
        heightCm = 175.0,
        maintenanceMode = false,
        maintenanceRangeKg = 1.0,
        maintenanceModeActivatedAt = null,
        achievedAt = null,
    )

    private fun entry(kg: Double, epochDay: Long = 0L) = WeightEntry(
        id = 0L,
        timestamp = epochDay * 86_400_000L,
        weightKg = kg,
        note = "",
    )

    // ── allEarnedBadges ──────────────────────────────────────────────────────

    @Test
    fun `allEarnedBadges emits empty set when profile is null`() = runTest {
        every { weightRepo.getEntriesNewestFirst() } returns MutableStateFlow(emptyList())
        every { profileRepo.getProfile() } returns MutableStateFlow(null)
        every { dataStore.seenBadgesFlow } returns MutableStateFlow(emptySet())

        val observer = BadgeObserverImpl(weightRepo, profileRepo, dataStore)

        observer.allEarnedBadges.test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `allEarnedBadges emits FIRST_WEIGH_IN after first entry`() = runTest {
        every { weightRepo.getEntriesNewestFirst() } returns MutableStateFlow(listOf(entry(80.0)))
        every { profileRepo.getProfile() } returns MutableStateFlow(baseProfile)
        every { dataStore.seenBadgesFlow } returns MutableStateFlow(emptySet())

        val observer = BadgeObserverImpl(weightRepo, profileRepo, dataStore)

        observer.allEarnedBadges.test {
            val badges = awaitItem()
            assertTrue(Badge.FIRST_WEIGH_IN in badges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `allEarnedBadges reacts to new entry being added`() = runTest {
        val entriesFlow = MutableStateFlow(emptyList<WeightEntry>())
        every { weightRepo.getEntriesNewestFirst() } returns entriesFlow
        every { profileRepo.getProfile() } returns MutableStateFlow(baseProfile)
        every { dataStore.seenBadgesFlow } returns MutableStateFlow(emptySet())

        val observer = BadgeObserverImpl(weightRepo, profileRepo, dataStore)

        observer.allEarnedBadges.test {
            assertTrue(awaitItem().isEmpty())          // no entries → empty
            entriesFlow.value = listOf(entry(80.0))    // add entry
            assertTrue(awaitItem().contains(Badge.FIRST_WEIGH_IN))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── newlyUnlockedBadges ──────────────────────────────────────────────────

    @Test
    fun `newlyUnlockedBadges excludes already-seen badges`() = runTest {
        every { weightRepo.getEntriesNewestFirst() } returns MutableStateFlow(listOf(entry(80.0)))
        every { profileRepo.getProfile() } returns MutableStateFlow(baseProfile)
        // FIRST_WEIGH_IN already seen
        every { dataStore.seenBadgesFlow } returns MutableStateFlow(setOf(Badge.FIRST_WEIGH_IN))

        val observer = BadgeObserverImpl(weightRepo, profileRepo, dataStore)

        observer.newlyUnlockedBadges.test {
            val newBadges = awaitItem()
            assertFalse(Badge.FIRST_WEIGH_IN in newBadges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `newlyUnlockedBadges includes unseen earned badges`() = runTest {
        every { weightRepo.getEntriesNewestFirst() } returns MutableStateFlow(listOf(entry(80.0)))
        every { profileRepo.getProfile() } returns MutableStateFlow(baseProfile)
        every { dataStore.seenBadgesFlow } returns MutableStateFlow(emptySet())

        val observer = BadgeObserverImpl(weightRepo, profileRepo, dataStore)

        observer.newlyUnlockedBadges.test {
            val newBadges = awaitItem()
            assertTrue(Badge.FIRST_WEIGH_IN in newBadges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── markSeen ─────────────────────────────────────────────────────────────

    @Test
    fun `markSeen delegates to dataStore markBadgesSeen`() = runTest {
        every { weightRepo.getEntriesNewestFirst() } returns MutableStateFlow(emptyList())
        every { profileRepo.getProfile() } returns MutableStateFlow(null)
        every { dataStore.seenBadgesFlow } returns MutableStateFlow(emptySet())

        val observer = BadgeObserverImpl(weightRepo, profileRepo, dataStore)

        val badges = setOf(Badge.FIRST_WEIGH_IN, Badge.GOAL_SET)
        observer.markSeen(badges)

        coVerify { dataStore.markBadgesSeen(badges) }
    }
}
