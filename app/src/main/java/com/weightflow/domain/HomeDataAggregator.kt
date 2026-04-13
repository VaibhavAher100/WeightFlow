package com.weightflow.domain

import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * RFC #29: Single dependency for HomeViewModel — combines WeightRepository,
 * UserProfileRepository, and UserPrefsDataStore into one HomeData flow.
 */
interface HomeDataAggregator {
    val homeData: Flow<HomeData>
}

class HomeDataAggregatorImpl(
    private val weightRepository: WeightRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : HomeDataAggregator {

    override val homeData: Flow<HomeData> = combine(
        weightRepository.getEntriesNewestFirst(),
        userProfileRepository.getProfile(),
        userPrefsDataStore.weightUnit,
    ) { entries, profile, unit ->
        HomeData(entries, profile, unit)
    }
}
