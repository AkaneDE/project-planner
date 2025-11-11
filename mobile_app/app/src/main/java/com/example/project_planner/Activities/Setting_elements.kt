package com.example.project_planner.Activities

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    userId: Int,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit, ) {
    AccountEdit(
        userId = userId,
        snackbarHostState = snackbarHostState,
        onClose = onBackClick
    )
}
