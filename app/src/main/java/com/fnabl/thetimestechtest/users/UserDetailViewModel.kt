package com.fnabl.thetimestechtest.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fnabl.domain.repository.FollowRepository
import com.fnabl.domain.repository.UsersRepository
import com.fnabl.domain.users.UserDetailUiState
import com.fnabl.domain.users.reduceUserDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val usersRepository: UsersRepository,
    private val followRepository: FollowRepository,
) : ViewModel() {
    private val userId: Long =
        requireNotNull(savedStateHandle[USER_ID_KEY]) {
            "UserDetailViewModel requires '$USER_ID_KEY' in SavedStateHandle"
        }

    val state: StateFlow<UserDetailUiState> =
        combine(
            usersRepository.topUsers,
            followRepository.followedUserIds
        ) { topUsers, followedIds ->
            reduceUserDetailState(userId, topUsers, followedIds)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = UserDetailUiState.Loading,
        )

    fun toggleFollow() {
        viewModelScope.launch {
            val followedIds = followRepository.followedUserIds.first()
            if (userId in followedIds) {
                followRepository.unfollow(userId)
            } else {
                followRepository.follow(userId)
            }
        }
    }

    private companion object {
        const val USER_ID_KEY = "id"
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
