package com.fnabl.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UsersResponseDto(
    @SerialName("items") val items: List<UserDto> = emptyList(),
)

@Serializable
internal data class UserDto(
    @SerialName("user_id") val userId: Long,
    @SerialName("display_name") val displayName: String,
    @SerialName("reputation") val reputation: Int,
    @SerialName("profile_image") val profileImage: String,
)
