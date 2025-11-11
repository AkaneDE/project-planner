package com.example.project_planner.Activities
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.project_planner.ApiService
import com.example.project_planner.UserTaskResponse
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    taskId: Int,
    apiService: ApiService,
    onBackClick: () -> Unit,
    userId:Int
) {
    var configDetails by remember { mutableStateOf<UserTaskResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Запрос данных с сервера
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUserTask(taskId)
            configDetails = response
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Если данные еще загружаются, показываем индикатор загрузки
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorMessage ?: "Неизвестная ошибка",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    configDetails?.let { details ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Custom TopAppBar
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                title = {
                    Text(
                        text = "Конфигурация задачи",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Project Information
                ConfigSection(title = "Информация о проекте") {
                    ConfigItem(label = "Название проекта", value = details.project_name)
                    ConfigItem(label = "Описание проекта", value = details.project_description)
                    ConfigItem(label = "Руководитель", value = "${details.leader_first_name} ${details.leader_last_name}")
                }

                // Task Information
                ConfigSection(title = "Информация о задаче") {
                    ConfigItem(label = "ID задачи", value = details.task_id.toString())
                    ConfigItem(label = "ID проекта", value = details.project_id.toString())
                    ConfigItem(label = "Название задачи", value = details.task_name)
                    ConfigItem(label = "Описание задачи", value = details.task_description)
                    ConfigItem(label = "Срок выполнения", value = details.task_deadline)
                    ConfigItem(label = "Статус задачи", value = details.task_status)
                }

                details.chat_id?.let { Chat(it, userId, apiService) }


            }
        }
    }
}

@Composable
fun ConfigSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ConfigItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

