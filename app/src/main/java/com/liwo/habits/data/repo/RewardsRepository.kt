package com.liwo.habits.data.repo

import androidx.room.withTransaction
import com.liwo.habits.data.db.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton
import com.liwo.habits.data.model.Redemption
import com.liwo.habits.data.model.Reward
import com.liwo.habits.util.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class RewardsRepository @Inject constructor(private val db: AppDatabase) {

    // Points = earned (from habits) - spent (from redemptions)
    fun observeRewardsState(): Flow<RewardsState> {
        val pointsEarnedFlow = db.habitLogDao().observeTotalPointsEarned()
        val pointsSpentFlow = db.redemptionDao().observeTotalSpent()

        val rewardsFlow = db.rewardDao().observeAll()
        val historyFlow = db.redemptionDao().observeRecent(limit = 500)

        return combine(pointsEarnedFlow, pointsSpentFlow, rewardsFlow, historyFlow) { earned, spent, rewards, history ->
            val earnedLong = earned
            val spentLong = spent
            val availableLong = earnedLong - spentLong

            // Clamp to Int range to avoid overflow crashes
            val available = availableLong.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
            val totalEarned = earnedLong.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()

            val redeemedRewardIds = history.asSequence().map { it.rewardId }.toSet()

            val rewardItems = rewards
                // ✅ hide single-redeem rewards once redeemed
                .filter { r -> r.isRecurring || !redeemedRewardIds.contains(r.id) }
                .map { r ->
                    val alreadyRedeemed = redeemedRewardIds.contains(r.id)
                    val canRedeem = r.isActive && available >= r.cost && (r.isRecurring || !alreadyRedeemed)
                    RewardItem(
                        reward = r,
                        alreadyRedeemed = alreadyRedeemed,
                        canRedeem = canRedeem,
                        needMore = (r.cost - available).coerceAtLeast(0)
                    )
                }

            RewardsState(pointsAvailable = available, pointsEarned = totalEarned, rewards = rewardItems)
        }
    }

    fun observeHistory(limit: Int): Flow<List<Redemption>> =
        db.redemptionDao().observeRecent(limit)

    suspend fun upsertReward(
        id: Long = 0,
        name: String,
        cost: Int,
        description: String?,
        isActive: Boolean,
        isRecurring: Boolean,
        sortOrder: Int? = null
    ) {
        val finalSort = sortOrder ?: ((db.rewardDao().maxSortOrder() ?: 0) + 1)

        db.rewardDao().upsert(
            Reward(
                id = id,
                name = name.trim(),
                cost = cost.coerceAtLeast(1),
                description = description?.trim(),
                isActive = isActive,
                isRecurring = isRecurring,
                sortOrder = finalSort
            )
        )
    }

    suspend fun deleteReward(reward: Reward) =
        db.rewardDao().delete(reward)

    suspend fun setActive(id: Long, active: Boolean) =
        db.rewardDao().setActive(id, active)

    /**
     * Transactional redeem:
     * - Re-check points inside transaction
     * - Re-check single-redeem count inside transaction
     * - Insert redemption inside the same transaction
     */
    suspend fun redeem(reward: Reward): Result<Unit> {
        return try {
            db.withTransaction {
                val latest = db.rewardDao().getById(reward.id)
                    ?: return@withTransaction Result.failure(IllegalStateException("Reward not found."))

                if (!latest.isActive) {
                    return@withTransaction Result.failure(IllegalStateException("Reward is inactive."))
                }

                val earned = db.habitLogDao().getTotalPointsEarned()
                val spent = db.redemptionDao().getTotalSpent()
                val available = earned - spent

                if (available < latest.cost.toLong()) {
                    val needMore = (latest.cost.toLong() - available).coerceAtLeast(0)
                    return@withTransaction Result.failure(
                        IllegalStateException("Not enough points. Need $needMore more.")
                    )
                }

                if (!latest.isRecurring) {
                    val count = db.redemptionDao().countForReward(latest.id)
                    if (count > 0) {
                        return@withTransaction Result.failure(
                            IllegalStateException("This reward is single redeem and was already redeemed.")
                        )
                    }
                }

                db.redemptionDao().insert(
                    Redemption(
                        rewardId = latest.id,
                        rewardName = latest.name,
                        cost = latest.cost,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )

                Result.success(Unit)
            }
        } catch (t: Throwable) {
            AppLogger.e("RewardsRepo", "redeem() failed for reward ${reward.id}", t)
            Result.failure(t)
        }
    }

    suspend fun undoRedemption(redemptionId: Long) {
        db.withTransaction {
            db.redemptionDao().deleteById(redemptionId)
        }
    }
}

data class RewardsState(
    val pointsAvailable: Int,
    val pointsEarned: Int,
    val rewards: List<RewardItem>
)

data class RewardItem(
    val reward: Reward,
    val alreadyRedeemed: Boolean,
    val canRedeem: Boolean,
    val needMore: Int
)