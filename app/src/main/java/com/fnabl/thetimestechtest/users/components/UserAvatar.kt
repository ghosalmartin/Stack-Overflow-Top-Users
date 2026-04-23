package com.fnabl.thetimestechtest.users.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

private val AvatarSize = 56.dp

@Composable
fun UserAvatar(
    profileImageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = profileImageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(AvatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clearAndSetSemantics {},
    )
}
