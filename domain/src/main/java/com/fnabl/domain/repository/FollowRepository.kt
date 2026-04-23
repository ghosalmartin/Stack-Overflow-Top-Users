package com.fnabl.domain.repository

import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    val followedUserIds: Flow<Set<Long>>

    suspend fun follow(userId: Long)

    suspend fun unfollow(userId: Long)
}
