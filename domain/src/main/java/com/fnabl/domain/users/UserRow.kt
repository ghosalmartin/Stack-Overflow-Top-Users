package com.fnabl.domain.users

data class UserRow(
    val id: Long,
    val displayName: String,
    val reputation: Int,
    val profileImageUrl: String,
    val isFollowed: Boolean,
)
