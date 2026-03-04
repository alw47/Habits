package com.liwo.habits.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.model.Redemption
import com.liwo.habits.data.model.Reward
import com.liwo.habits.data.repo.RewardsRepository
import com.liwo.habits.data.repo.RewardsState
import com.liwo.habits.util.AppLogger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val repo = RewardsRepository(db)

    val state: StateFlow<RewardsState> =
        repo.observeRewardsState()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                RewardsState(pointsAvailable = 0, rewards = emptyList())
            )

    val history: StateFlow<List<Redemption>> =
        repo.observeHistory(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveReward(
        id: Long = 0,
        name: String,
        cost: Int,
        description: String?,
        isActive: Boolean,
        isRecurring: Boolean
    ) {
        viewModelScope.launch {
            try {
                repo.upsertReward(
                    id = id,
                    name = name,
                    cost = cost,
                    description = description,
                    isActive = isActive,
                    isRecurring = isRecurring
                )
                AppLogger.i("Rewards", "Reward saved: $name")
            } catch (t: Throwable) {
                AppLogger.e("Rewards", "Failed to save reward: $name", t)
            }
        }
    }

    fun deleteReward(reward: Reward, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.deleteReward(reward)
                AppLogger.i("Rewards", "Reward deleted: ${reward.name}")
            } catch (t: Throwable) {
                AppLogger.e("Rewards", "Failed to delete reward: ${reward.name}", t)
                onError(t.message ?: "Could not delete reward.")
            }
        }
    }

    fun setActive(id: Long, active: Boolean, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.setActive(id, active)
                AppLogger.i("Rewards", "Reward $id active=$active")
            } catch (t: Throwable) {
                AppLogger.e("Rewards", "Failed to set reward $id active=$active", t)
                onError(t.message ?: "Could not update reward.")
            }
        }
    }

    fun redeem(reward: Reward, onError: (String) -> Unit, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repo.redeem(reward)
                if (res.isFailure) {
                    onError(res.exceptionOrNull()?.message ?: "Could not redeem.")
                } else {
                    AppLogger.i("Rewards", "Redeemed: ${reward.name} (${reward.cost} pts)")
                    onSuccess()
                }
            } catch (t: Throwable) {
                AppLogger.e("Rewards", "Redeem failed for: ${reward.name}", t)
                onError(t.message ?: "Redeem failed.")
            }
        }
    }

    fun undo(redemptionId: Long, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.undoRedemption(redemptionId)
                AppLogger.i("Rewards", "Undo redemption: $redemptionId")
            } catch (t: Throwable) {
                AppLogger.e("Rewards", "Failed to undo redemption: $redemptionId", t)
                onError(t.message ?: "Could not undo redemption.")
            }
        }
    }
}