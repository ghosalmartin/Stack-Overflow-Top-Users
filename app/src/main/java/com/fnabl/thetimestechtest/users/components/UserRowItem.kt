package com.fnabl.thetimestechtest.users.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fnabl.thetimestechtest.R
import com.fnabl.thetimestechtest.users.UserRowUiModel

@Composable
fun UserRowItem(
    row: UserRowUiModel,
    onToggleFollow: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserAvatar(profileImageUrl = row.profileImageUrl)

        UserDetails(
            modifier = Modifier.weight(1f),
            row = row,
        )

        FollowToggle(
            isFollowed = row.isFollowed,
            onClick = { onToggleFollow(row.id) },
        )
    }
}

@Composable
private fun UserDetails(
    row: UserRowUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = row.displayName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.reputation_format, row.displayReputation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AnimatedVisibility(
            visible = row.isFollowed,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = stringResource(R.string.followed_indicator),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun FollowToggle(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isFollowed) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "follow-toggle-scale",
    )

    val followContentDescription = stringResource(R.string.follow_user)
    val unfollowContentDescription = stringResource(R.string.unfollow_user)

    IconToggleButton(
        checked = isFollowed,
        onCheckedChange = { onClick() },
        modifier = modifier.semantics {
            contentDescription = if (isFollowed) unfollowContentDescription else followContentDescription
        },
    ) {
        Icon(
            imageVector = if (isFollowed) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (isFollowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.scale(scale),
        )
    }
}
