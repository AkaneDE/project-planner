package com.example.project_planner.Activities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.project_planner.ApiService





@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation(
    userId: Int,
    snackbarHostState: SnackbarHostState,
    selectedTab: MutableState<Int>,
    navController: NavHostController,
    showBottomBar: MutableState<Boolean>,
    apiService: ApiService,
    modifier: Modifier = Modifier,
    innerPadding: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "project_list",
        modifier = modifier.then(innerPadding)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        composable("project_list") {
            selectedTab.value = 0
            showBottomBar.value = true
            ProjectListScreen(
                userId = userId,
                onProjectDetailsClick = { projectId ->
                    navController.navigate("project_details/$projectId")
                },
                snackbarHostState = snackbarHostState,
            )
        }
        composable("create_project") {
            selectedTab.value = 1
            showBottomBar.value = false
            CreateProjectScreen(
                userId = userId,
                onClose = {
                    navController.popBackStack()
                },
                snackbarHostState = snackbarHostState,
            )
        }
        // Экран профиля
        composable("profile") {
            showBottomBar.value = true
            selectedTab.value = 2
            ProfileScreen(
                userId = userId,
                onSettingsClick = {  navController.navigate("profile/settings") },
                onFriendsClick = {
                    navController.navigate("profile/friends")
                },
                onProgressClick1 = { /* TODO */ },
                onProgressClick2 = { /* TODO */ }
            )
        }
        // Экран профиля
        composable("profile/friends") {
            showBottomBar.value = false
            FriendsScreen(
                user_id = userId,
                onBackClick = {
                    navController.popBackStack()
                },
                onFriendClick = { friendId ->
                    navController.navigate("profile/friends/$friendId")
                },
                snackbarHostState
            )
        }
        // Экран профиля
        composable("profile/friends/{friendId}") {backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId")?.toIntOrNull() ?: 0
            showBottomBar.value = false
            FriendProfileScreen(friendId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        // Экран профиля
        composable("profile/settings") {
            showBottomBar.value = false
            SettingsScreen(
                userId = userId,
                snackbarHostState = snackbarHostState,
                onBackClick = {
                navController.popBackStack()
            },)
        }
        // Экран деталей проекта
        composable("project_details/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            showBottomBar.value = false
            ProjectDetailsScreen(
                projectId = projectId,
                userId = userId,
                apiService = apiService,
                onBackClick = { navController.popBackStack()},
                onEditClick = { navController.navigate("project_edit/$projectId") },
                onTaskDetailsClick = { taskId ->
                    navController.navigate("project_details/$projectId/task_details/$taskId")
                },
                onAddTask = {
                    navController.navigate("project_details/$projectId/task_add")
                },
                snackbarHostState = snackbarHostState,
            )
        }
        // Экран редактирования проекта
        composable("project_edit/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            EditProjectScreen(
                projectId = projectId,
                userId = userId,
                snackbarHostState = snackbarHostState,
                onClose = { navController.popBackStack()},
                onDelete = {navController.popBackStack()
                    navController.popBackStack()}
            )
        }
        // Экран деталей задачи
        composable("project_details/{projectId}/task_details/{taskId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
            showBottomBar.value = false
            TaskDetailsScreen(
                taskId = taskId,
                apiService = apiService,
                onBackClick = { navController.popBackStack()},
                onEditClick = {
                    navController.navigate("project_details/$projectId/task_edit/$taskId")
                },
                snackbarHostState,
                userId
            )
        }
        // Экран создания задачи
        composable("project_details/{projectId}/task_add") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            showBottomBar.value = false
            CreateTaskScreen(
                projectId = projectId,
                snackbarHostState = snackbarHostState,
                onClose = { navController.popBackStack()}
            )
        }
        // Экран редактирования задачи
        composable("project_details/{projectId}/task_edit/{taskId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
            showBottomBar.value = false
            TaskScreen(
                projectId = projectId,
                taskId = taskId,
                snackbarHostState = snackbarHostState,
                onClose = { navController.popBackStack()},
                onDelete = { navController.popBackStack()
                    navController.popBackStack() }
            )
        }
    }
}
