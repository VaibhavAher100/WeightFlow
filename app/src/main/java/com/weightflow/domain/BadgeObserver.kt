package com.weightflow.domain

import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * RFC #24 — Reactive badge observer.
 * Combines entries + profile Flows via [BadgeEngine]; surfaces newly unlocked badges
 * without re-notifying on cold start by tracking seen state in [UserPrefsDataStore].
 */
interface BadgeObserver {
    /** All badges the user has currently earned (re-evaluated on every data change). */
    val allEarnedBadges: Flow<Set<Badge>>

    /** Badges earned but not yet shown to the user (earned minus seen). */
    val newlyUnlockedBadges: Flow<Set<Badge>>

    /** Call after the UI has shown a badge notification to suppress future re-triggers. */
    suspend fun markSeen(badges: Set<Badge>)
}

class BadgeObserverImpl(
    private val weightRepository: WeightRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : BadgeObserver {

    override val allEarnedBadges: Flow<Set<Badge>> = combine(
        weightRepository.getEntriesNewestFirst(),
        userProfileRepository.getProfile(),
    ) { entries, profile ->
        if (profile == null) emptySet()
        else BadgeEngine.evaluate(entries, profile)
    }

    override val newlyUnlockedBadges: Flow<Set<Badge>> = combine(
        allEarnedBadges,
        userPrefsDataStore.seenBadgesFlow,
    ) { earned, seen ->
        earned - seen
    }

    override suspend fun markSeen(badges: Set<Badge>) {
        userPrefsDataStore.markBadgesSeen(badges)
    }
}
