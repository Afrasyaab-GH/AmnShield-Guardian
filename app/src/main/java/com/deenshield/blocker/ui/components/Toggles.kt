package com.deenshield.blocker.ui.components

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable

@Composable
fun ToggleItem(title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle != null) Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) }
    )
}
