package com.example.project_planner.Activities

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_planner.R
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.WorkerSearch
import com.example.project_planner.addFriend
import com.example.project_planner.findFriend
import kotlinx.coroutines.launch


@Composable
fun FriendsScreen(
    user_id: Int,
    onBackClick: () -> Unit,
    onFriendClick: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var isSearchMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var friends by remember {
        mutableStateOf<MutableList<WorkerSearch>?>(
            mutableListOf()
        )
    }
    var user = remember { mutableStateOf<WorkerSearch?>(null) }
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getFriends(user_id)
            friends = response
        } catch (e: Exception) {
            // Показ ошибки через Snackbar
            snackbarHostState.showSnackbar(
                message = e.message ?: "Ошибка",
                duration = SnackbarDuration.Short
            )
        }
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
            if (isSearchMode) {
                SearchTopBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearchClick = {
                        coroutineScope.launch {
                            val result = findFriend(apiService, query)
                            result.onSuccess { userResponse ->
                                user.value = userResponse
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar(
                                    message = error.message ?: "Ошибка",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    onBackClick = { isSearchMode = false }
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (friends?.any { it.worker_nickname == user.value?.worker_nickname } == true) {
                        user.value?.let {
                            FriendListItem(
                                friend = it,
                                onClick = {  /* TODO */ },
                                onClickDelete = {})
                        }
                    } else {
                        user.value?.let {
                            SearchUserItem(user = it, onAddClick = {
                                coroutineScope.launch {
                                    val result =
                                        addFriend(apiService, user_id, user.value!!.worker_id)
                                    result.onSuccess { userResponse ->

                                    }.onFailure { error ->
                                        snackbarHostState.showSnackbar(
                                            message = error.message ?: "Ошибка",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            })
                        }
                    }
                }
            } else {
                FriendsTopBar(
                    onBackClick = onBackClick,
                    onSearchClick = { isSearchMode = true }
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    friends?.let { list ->
                        items(list) { friend ->
                            FriendListItem(friend = friend, onClick = onFriendClick,
                                onClickDelete = {
                                    coroutineScope.launch {
                                        apiService.deleteFriend(user_id, friend.worker_id)
                                    }
                                    friends = friends?.toMutableList()?.apply { remove(friend) }
                                })
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun FriendsTopBar(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(
            text = "Друзья",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSecondary
        )
        IconButton(onClick = onSearchClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Поиск",
                color = MaterialTheme.colorScheme.background, // Цвет текста-плейсхолдера
            ) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = MaterialTheme.colorScheme.background,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.weight(1.5f)
        )
        IconButton(onClick = onSearchClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FriendListItem(
    friend: WorkerSearch,
    onClick: (Int) -> Unit,
    onClickDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick(friend.worker_id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarIcon(userId = friend.worker_id)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = listOfNotNull(
                    friend.worker_nickname,
                    friend.worker_name,
                    friend.worker_lastname
                ).joinToString(" "),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(modifier = Modifier
            .clickable { onClickDelete() })
        {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SearchUserItem(user: WorkerSearch, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarIcon(userId = user.worker_id)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = listOfNotNull(
                    user.worker_nickname,
                    user.worker_name,
                    user.worker_lastname
                ).joinToString(" "),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(onClick = onAddClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add friend",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AvatarIcon(userId: Int) {
    var selectedImageUri by remember { mutableStateOf< Uri?>(null) }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        LoadProfilePhotoFromServer(
            userId = userId,
        ) { uri ->
            selectedImageUri = uri
        }
        if (selectedImageUri != null) {
            LoadImageFromUri(selectedImageUri)
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