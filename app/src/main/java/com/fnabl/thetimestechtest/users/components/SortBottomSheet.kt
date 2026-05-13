package com.fnabl.thetimestechtest.users.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fnabl.domain.users.SortOrder
import com.fnabl.domain.users.UserSort
import com.fnabl.domain.users.UserSortSelection
import com.fnabl.thetimestechtest.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    current: UserSortSelection,
    onApply: (UserSortSelection) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftSort by rememberSaveable(current) { mutableStateOf(current.sort) }
    var draftOrder by rememberSaveable(current) { mutableStateOf(current.order) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        SortSheetContent(
            selectedSort = draftSort,
            selectedOrder = draftOrder,
            onSortSelected = { draftSort = it },
            onOrderSelected = { draftOrder = it },
            onApply = { onApply(UserSortSelection(draftSort, draftOrder)) },
            onCancel = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheetContent(
    selectedSort: UserSort,
    selectedOrder: SortOrder,
    onSortSelected: (UserSort) -> Unit,
    onOrderSelected: (SortOrder) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.sort_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Column(modifier = Modifier.selectableGroup()) {
            UserSort.entries.forEach { option ->
                SortRadioRow(
                    sort = option,
                    selected = option == selectedSort,
                    onSelect = { onSortSelected(option) },
                )
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SortOrder.entries.forEachIndexed { index, order ->
                SegmentedButton(
                    selected = order == selectedOrder,
                    onClick = { onOrderSelected(order) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = SortOrder.entries.size,
                    ),
                    icon = {
                        Icon(
                            imageVector = if (order == SortOrder.ASC) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                ) {
                    Text(text = stringResource(order.labelRes()))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.sort_cancel))
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.sort_apply))
            }
        }
    }
}

@Composable
private fun SortRadioRow(
    sort: UserSort,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(sort.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun UserSort.labelRes(): Int =
    when (this) {
        UserSort.REPUTATION -> R.string.sort_by_reputation
        UserSort.NAME -> R.string.sort_by_name
        UserSort.CREATION -> R.string.sort_by_creation
        UserSort.MODIFIED -> R.string.sort_by_modified
    }

private fun SortOrder.labelRes(): Int =
    when (this) {
        SortOrder.ASC -> R.string.sort_order_ascending
        SortOrder.DESC -> R.string.sort_order_descending
    }
