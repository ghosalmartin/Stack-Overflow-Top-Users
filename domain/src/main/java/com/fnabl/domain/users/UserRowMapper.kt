package com.fnabl.domain.users

import com.fnabl.domain.model.User

fun User.toRow(isFollowed: Boolean): UserRow =
    UserRow(
        id = id,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImageUrl,
        isFollowed = isFollowed,
    )

fun List<User>.toRows(followedUserIds: Set<Long>): List<UserRow> = map { it.toRow(isFollowed = it.id in followedUserIds) }
