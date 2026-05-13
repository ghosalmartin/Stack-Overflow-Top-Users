package com.fnabl.domain.model

data class User(
    val id: Long,
    val displayName: String,
    val reputation: Int,
    val profileImageUrl: String,
    val websiteUrl: String?,
    val location: String?,
    val creationDate: Long,
    val lastModifiedDate: Long?,
)
