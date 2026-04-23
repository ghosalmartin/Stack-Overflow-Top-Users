package com.fnabl.thetimestechtest.users

import com.fnabl.domain.users.UserRow
import java.text.NumberFormat
import java.util.Locale

data class UserRowUiModel(
    val id: Long,
    val displayName: String,
    val displayReputation: String,
    val profileImageUrl: String,
    val isFollowed: Boolean,
)

fun UserRow.toUiModel(): UserRowUiModel =
    UserRowUiModel(
        id = id,
        displayName = displayName,
        displayReputation = NumberFormat.getNumberInstance(Locale.getDefault()).format(reputation),
        profileImageUrl = profileImageUrl,
        isFollowed = isFollowed,
    )
