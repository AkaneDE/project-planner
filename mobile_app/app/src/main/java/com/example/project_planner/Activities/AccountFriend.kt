package com.example.project_planner.Activities
import SectionLabel
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.WorkerProfileResponse


@Composable
fun FriendProfileHeader(
    name: String?,
    image: Uri?,
    nickname: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        UserAvatar(image)
        Spacer(modifier = Modifier.height(8.dp))
        UserNameBlock(name = name, nickname = nickname)
    }
}

@Composable
fun FriendProfileScreen(
    userId: Int,
    onBackClick: () -> Unit,
    onProgressClick1: () -> Unit = {},
    onProgressClick2: () -> Unit = {},
    ) {
    var user by remember { mutableStateOf<WorkerProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUser(userId)
            user = response
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }
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
    user?.let {
        LoadProfilePhotoFromServer(
            userId = userId,
        ) { uri ->
            selectedImageUri = uri
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                FriendProfileHeader(
                    name = listOfNotNull(it.worker_name, it.worker_lastname, it.worker_patronymic)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(" "),
                    image = selectedImageUri,
                    nickname = it.worker_nickname,
                    onBackClick
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    it.active_projects_count?.let { it1 ->
                        ProgressItem(
                            count = it1,
                            label = "В процессе",
                            onProgressClick1
                        )
                    }
                    it.finished_projects_count?.let { it1 ->
                        ProgressItem(
                            count = it1,
                            label = "Выполнено",
                            onProgressClick2
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                val groupedTechnologies = it.technologies
                    ?.groupBy { tech ->
                        it.roles?.find { role -> role.role_id == tech.role_id }?.role_name ?: ""
                    }
                if (groupedTechnologies != null) {
                    SectionLabel("Специальность")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        groupedTechnologies.forEach { (role, techList) ->
                            item {
                                CollapsibleTagBlock(
                                    title = role,
                                    tags = techList.map { it.technology_name }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
