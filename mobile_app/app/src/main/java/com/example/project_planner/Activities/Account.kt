package com.example.project_planner.Activities

import SectionLabel
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_planner.NotificationAcceptRequest
import com.example.project_planner.NotificationItem
import com.example.project_planner.NotificationItem2
import com.example.project_planner.R
import com.example.project_planner.RetrofitClient
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.ViewedNotificationsRequest
import com.example.project_planner.WorkerProfileResponse
import com.example.project_planner.createPartFromString
import kotlinx.coroutines.launch


//Переход к друзьям
@Composable
fun OutlinedNavigationButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun NotificationIcon(unread: Boolean, onClick: () -> Unit) {
    Box {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        if (unread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-7).dp, y = (10).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center
            ) {
            }
        }
    }
}

@Composable
fun SettingsIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun UserAvatar(image: Uri?) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            LoadImageFromUri(image)
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Default Avatar",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
fun UserNameBlock(name: String?, nickname: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (name != null) {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = nickname,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun UserProfileHeader(
    name: String?,
    image: Uri?,
    unread: Boolean,
    nickname: String,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
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
            NotificationIcon(
                onClick = onNotificationsClick,
                unread = unread,
            )
            SettingsIcon(onClick = onSettingsClick)
        }
        Spacer(modifier = Modifier.height(16.dp))
        UserAvatar(image)
        Spacer(modifier = Modifier.height(8.dp))
        UserNameBlock(name = name, nickname = nickname)
    }
}

@Composable
fun ProgressItem(
    count: Int,
    label: String,
    onProgressClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable{onProgressClick()}
    ) {
        Text(
            text = count.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color =MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color =MaterialTheme.colorScheme.onPrimary
        )
    }
}


@Composable
fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}




@Composable
fun CollapsibleTagBlock(
    title: String,
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary) // Цвет как на изображении
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,

            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(tags) { tag ->
                    TagChip(text = tag)
                }
            }
        }
    }
}



@Composable
fun ProfileScreen(
    userId: Int,
    onSettingsClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onProgressClick1: () -> Unit,
    onProgressClick2: () -> Unit,

) {
    var user by remember { mutableStateOf<WorkerProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) } // Управление состоянием диалога
    var notifications by remember { mutableStateOf<List<NotificationItem2>?>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUser(userId)
            user = response
            val res = apiService.getNotifications(userId)
            if (res != null) {
                notifications = res.map {
                    NotificationItem2(
                        notification_task_id = it.notification_task_id,
                        notification_task_text = it.notification_task_text,
                        notification_task_time = it.notification_task_time,
                        author = it.author,
                        viewed = mutableStateOf(it.viewed),
                        accepted = mutableStateOf(it.accepted)
                    )
                }
            }
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
                UserProfileHeader(
                    name = listOfNotNull(it.worker_name, it.worker_lastname, it.worker_patronymic)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(" "),
                    image = selectedImageUri,
                    nickname = it.worker_nickname,
                    onNotificationsClick =
                    {
                        showDialog = true
                    },
                    onSettingsClick = onSettingsClick,
                    unread = it.has_unread_notifications
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
                OutlinedNavigationButton(
                    text = "Друзья",
                    onClick = onFriendsClick
                )
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
                if (showDialog) {
                    val coroutineScope = rememberCoroutineScope()
                    NotificationDialog(
                        showDialog = showDialog,
                        notifications = notifications,
                        onDismiss = { updatedNotifications ->
                            coroutineScope.launch {
                                apiService.markNotificationsAsViewed(
                                    notificationIds = ViewedNotificationsRequest(
                                        updatedNotifications.map { it.notification_task_id })
                                )
                            }
                            showDialog = false
                        },
                        onAccept = { updatedNotification ->
                            coroutineScope.launch {
                                apiService.updateNotificationAccepted(
                                    NotificationAcceptRequest(
                                        updatedNotification.notification_task_id,
                                        true
                                    )
                                )
                            }
                        },
                        onReject = { updatedNotification ->
                            coroutineScope.launch {
                                apiService.updateNotificationAccepted(
                                    NotificationAcceptRequest(
                                        updatedNotification.notification_task_id,
                                        false
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}



