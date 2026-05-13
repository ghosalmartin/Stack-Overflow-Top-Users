package com.fnabl.data.remote.dto

import com.fnabl.domain.model.User

internal fun UserDto.toDomain(): User =
    User(
        id = userId,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImage,
        websiteUrl = websiteUrl,
        location = location,
        creationDate = creationDate,
        lastModifiedDate = lastModifiedDate,
    )

internal fun List<UserDto>.toDomain(): List<User> = map { it.toDomain() }
