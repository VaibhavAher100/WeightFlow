package com.weightflow.ui.i18n

import androidx.annotation.StringRes
import com.weightflow.R
import com.weightflow.domain.Badge

data class BadgeRes(@StringRes val nameRes: Int, @StringRes val descRes: Int)

object BadgeStrings {
    fun resFor(badge: Badge): BadgeRes = when (badge) {
        Badge.FIRST_WEIGH_IN -> BadgeRes(R.string.badge_first_weigh_in_name, R.string.badge_first_weigh_in_desc)
        Badge.GOAL_SET -> BadgeRes(R.string.badge_goal_set_name, R.string.badge_goal_set_desc)
        Badge.SEVEN_DAY_STREAK -> BadgeRes(R.string.badge_seven_day_streak_name, R.string.badge_seven_day_streak_desc)
        Badge.THIRTY_DAY_STREAK -> BadgeRes(R.string.badge_thirty_day_streak_name, R.string.badge_thirty_day_streak_desc)
        Badge.HUNDRED_DAY_STREAK -> BadgeRes(R.string.badge_hundred_day_streak_name, R.string.badge_hundred_day_streak_desc)
        Badge.TEN_LOGS -> BadgeRes(R.string.badge_ten_logs_name, R.string.badge_ten_logs_desc)
        Badge.FIFTY_LOGS -> BadgeRes(R.string.badge_fifty_logs_name, R.string.badge_fifty_logs_desc)
        Badge.THREE_SIXTY_FIVE_LOGS -> BadgeRes(R.string.badge_three_sixty_five_logs_name, R.string.badge_three_sixty_five_logs_desc)
        Badge.HALFWAY_THERE -> BadgeRes(R.string.badge_halfway_there_name, R.string.badge_halfway_there_desc)
        Badge.GOAL_CRUSHER -> BadgeRes(R.string.badge_goal_crusher_name, R.string.badge_goal_crusher_desc)
        Badge.COMEBACK -> BadgeRes(R.string.badge_comeback_name, R.string.badge_comeback_desc)
        Badge.STEADY_STATE -> BadgeRes(R.string.badge_steady_state_name, R.string.badge_steady_state_desc)
    }
}
