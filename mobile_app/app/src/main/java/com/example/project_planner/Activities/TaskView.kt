package com.example.project_planner.Activities

import CustomButton
import ScrollableTextWithCustomScrollbar
import SectionLabel
import TeamMemberItem
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.project_planner.ApiService
import com.example.project_planner.ProjectContent
import com.example.project_planner.RetrofitClient
import com.example.project_planner.TaskDetailsOut
import com.example.project_planner.completeTask
import com.example.project_planner.createFilePart
import com.example.project_planner.createPartFromString
import com.example.project_planner.socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import org.json.JSONObject


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailsScreen(
    taskId: Int,
    apiService: ApiService,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    userId: Int
) {
    var taskDetails by remember { mutableStateOf<TaskDetailsOut?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var addContent by remember { mutableStateOf(false) }
    var readContent by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf<ProjectContent?>(null) }
    var showDialogTask by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        try {
            val response = apiService.getTask(taskId)
            taskDetails = response
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


    if (taskDetails != null) {
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TaskHeader(
                        taskName = taskDetails!!.task_name,
                        task_daystart = taskDetails!!.task_daystart,
                        task_deadline = taskDetails!!.task_deadline,
                        onBackClick = onBackClick,
                        onEditClick = onEditClick,
                        showEditIcon = true
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Описание задачи")
                        ScrollableTextWithCustomScrollbar(
                            text = taskDetails!!.task_description ?: "Нет описания",
                        )
                    }


                    ProjectContentSection(
                        contents = taskDetails!!.task_content,
                        onItemClick = {readContent = true
                                      content = it},
                        onAddClick = { addContent = true })
                    val context = LocalContext.current

                    if (readContent) {
                        content?.let {
                            ContentDialog(
                                showDialog = readContent,
                                onDismiss = { readContent = !readContent },
                                content = it,
                                onSave = {url, name ->
                                    coroutineScope.launch {
                                        val response = apiService.downloadFile(url)
                                        if (response.isSuccessful && response.body() != null) {
                                            saveFileFromResponseBody(context, response.body()!!, name)
                                            snackbarHostState.showSnackbar(
                                                "Файл сохранён"
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                "Ошибка при скачивании"
                                            )
                                        }
                                    }

                                }

                            )
                        }
                    }

                    if (addContent) {
                        AddContentDialog(
                            addContent,
                            onDismiss = { addContent = false },
                            onSave = { name, text, uri ->

                                coroutineScope.launch {
                                    if (taskDetails!!.team_members?.any { it.worker_id == userId } == true) {
                                        validateAndCreateParts(context, name, text, uri)
                                            .onSuccess { filePart ->
                                                val result = runCatching {
                                                    RetrofitClient.apiService.uploadTaskContent(
                                                        workerId = userId,
                                                        taskId = taskId,
                                                        title = createPartFromString(name),
                                                        text = createPartFromString(text),
                                                        file = filePart
                                                    )
                                                }
                                                taskDetails = apiService.getTask(taskId)

                                                result.onSuccess {
                                                    addContent = false
                                                }.onFailure {
                                                    snackbarHostState.showSnackbar(
                                                        it.message ?: "Ошибка при отправке"
                                                    )
                                                }
                                                addContent = false
                                            }
                                            .onFailure {
                                                snackbarHostState.showSnackbar(
                                                    it.message ?: "Ошибка при подготовке данных"
                                                )
                                            }

                                    } else {
                                        snackbarHostState.showSnackbar(
                                            "Вы не назначены на задачу"
                                        )
                                    }
                                }

                            }
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Ответственные")
                        taskDetails!!.team_members.orEmpty().forEach { member ->
                            TeamMemberItem(
                                nickname = member.worker_nickname,
                                name = member.worker_name,
                                lastname = member.worker_lastname,
                                onClick = {}
                            )
                        }
                    }

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (taskDetails!!.task_status) {
                        CustomButton(
                            "Завершить",
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            borderColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            showDialogTask = true
                        }
                    }
                    if (showDialogTask) {
                        CompleteDialog(
                            showDialog = showDialogTask,
                            onDismiss = { showDialogTask = !showDialogTask },
                            onConfirm = {
                                showDialogTask = false
                                coroutineScope.launch {
                                    val result = completeTask(RetrofitClient.apiService, taskId)
                                    result.onSuccess {
                                        taskDetails = apiService.getTask(taskId)

                                    }.onFailure {
                                        snackbarHostState.showSnackbar(
                                            it.message ?: "Ошибка при завершении"
                                        )
                                    }
                                }
                            },
                            titleText = "Завершить задачу"
                        )
                    }
                }
            }
        }
    }
}

suspend fun validateAndCreateParts(
    context: Context,
    name: String,
    text: String,
    uri: Uri?
): Result<MultipartBody.Part> {
    if (name.isBlank()) return Result.failure(IllegalArgumentException("Введите название документа!"))
    if (uri == null) return Result.failure(IllegalArgumentException("Файл не выбран!"))
    return Result.success(createFilePart(context, uri, "file")!!)
}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun Chat(chatId: Int, currentUserId: Int, apiService: ApiService) {
//    // Состояние для чата
//    var chatMessage by remember { mutableStateOf("") }
//    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
//    // Инициализация Socket.IO
//    LaunchedEffect(chatId) {
//        if (socket != null) {
//            socket.emit("join_chat", chatId)
//        }
//
//        // Обработчик новых сообщений
//        if (socket != null) {
//            socket.on("new_message") { args ->
//                val json = args[0] as JSONObject
//                val newMessage = ChatMessage(
//                    user_id = json.getInt("user_id"),
//                    sender_name = json.getString("sender_name"),
//                    message = json.getString("message_text"),
//                    timestamp = json.getString("timestamp"),
//                    isCurrentUser = json.getInt("user_id") == currentUserId
//                )
//                chatMessages = chatMessages + newMessage
//            }
//        }
//
//        // Загрузка начальных сообщений
//        withContext(Dispatchers.IO) {
//            val messages = apiService.getMessages(chatId)
//            chatMessages = messages.map { message ->
//                message.copy(isCurrentUser = message.user_id == currentUserId)
//            }
//        }
//
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        Text(
//            text = "Обсуждение",
//            style = MaterialTheme.typography.titleMedium,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(300.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            contentPadding = PaddingValues(vertical = 8.dp)
//        ) {
//            items(chatMessages) { message ->
//                ChatMessage(message = message)
//            }
//        }
//        // Chat Input
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            tonalElevation = 3.dp,
//            color = MaterialTheme.colorScheme.surface
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                TextField(
//                    value = chatMessage,
//                    onValueChange = { chatMessage = it },
//                    modifier = Modifier.weight(1f),
//                    placeholder = { Text("Введите сообщение") },
//                    colors = TextFieldDefaults.textFieldColors(
//                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent
//                    ),
//                    shape = MaterialTheme.shapes.medium,
//                    singleLine = true
//                )
//                IconButton(
//                    onClick = {
//                        if (chatMessage.isNotBlank()) {
//                            val newMessageRequest = ChatMessageRequest(
//                                user_id = currentUserId,
//                                message_text = chatMessage
//                            )
//                            CoroutineScope(Dispatchers.IO).launch {
//                                // Отправка сообщения через Retrofit
//                                val sentMessage = apiService.sendMessage(chatId, newMessageRequest)
//                                chatMessages = chatMessages + sentMessage.copy(isCurrentUser = true)
//
//                                // Очистка ввода
//                                withContext(Dispatchers.Main) {
//                                    chatMessage = ""
//                                }
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(
//                            MaterialTheme.colorScheme.primary,
//                            CircleShape
//                        )
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "Send",
//                        tint = MaterialTheme.colorScheme.onPrimary
//                    )
//                }
//            }
//        }
//    }
//}
//
//data class ChatMessage(
//    val user_id: Int,
//    val sender_name: String,
//    val message: String,
//    val timestamp: String,
//    val isCurrentUser: Boolean = false
//)
//
//
//@Composable
//private fun ChatMessage(
//    message: ChatMessage,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier.fillMaxWidth(),
//        horizontalAlignment = if (message.isCurrentUser) Alignment.End else Alignment.Start
//    ) {
//        Surface(
//            shape = MaterialTheme.shapes.medium,
//            color = if (message.isCurrentUser)
//                MaterialTheme.colorScheme.primaryContainer
//            else
//                MaterialTheme.colorScheme.surfaceVariant,
//            modifier = Modifier.widthIn(max = 280.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = message.sender_name,
//                        style = MaterialTheme.typography.labelMedium,
//                        color = if (message.isCurrentUser)
//                            MaterialTheme.colorScheme.onPrimaryContainer
//                        else
//                            MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = message.timestamp,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = if (message.isCurrentUser)
//                            MaterialTheme.colorScheme.onPrimaryContainer
//                        else
//                            MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//                Text(
//                    text = message.message,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = if (message.isCurrentUser)
//                        MaterialTheme.colorScheme.onPrimaryContainer
//                    else
//                        MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}



//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun TaskDetailsScreen(
//    taskId: Int,
//    projectId: Int,
//    onBackClick: () -> Unit,
//    onEditClick: () -> Unit
//) {
////    var expandedSubtaskId by remember { mutableStateOf<Int?>(null) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        // Custom TopAppBar
//        TopAppBar(
//            navigationIcon = {
//                IconButton(onClick = onBackClick) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = "Back",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            },
//            title = {
//                Text(
//                    text = stages[taskId].name,
//                    style = MaterialTheme.typography.titleLarge,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//            },
//            actions = {
//                IconButton(onClick = onEditClick) {
//                    Icon(
//                        imageVector = Icons.Default.Edit,
//                        contentDescription = "Edit",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.background
//            )
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .verticalScroll(rememberScrollState())
//                .padding(24.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            // Deadline
//            Text(
//                text = "До ${stages[taskId].deadline}",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            // Task Description
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                Text(
//                    text = "Описание задачи",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Text(
//                    text = stages[taskId].description,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
////            // Subtasks
////            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
////                Text(
////                    text = "Подзадачи",
////                    style = MaterialTheme.typography.titleMedium,
////                    color = MaterialTheme.colorScheme.onSurface
////                )
////
////                subTasks.forEachIndexed { index, subtask ->
////                    Surface(
////                        modifier = Modifier.fillMaxWidth(),
////                        color = MaterialTheme.colorScheme.surfaceVariant,
////                        shape = MaterialTheme.shapes.medium
////                    ) {
////                        Column {
////                            Row(
////                                modifier = Modifier
////                                    .fillMaxWidth()
////                                    .clickable {
////                                        expandedSubtaskId = if (expandedSubtaskId == index) null else index
////                                    }
////                                    .padding(16.dp),
////                                horizontalArrangement = Arrangement.SpaceBetween,
////                                verticalAlignment = Alignment.CenterVertically
////                            ) {
////                                Row(
////                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
////                                    verticalAlignment = Alignment.CenterVertically
////                                ) {
////                                    RadioButton(
////                                        selected = false,
////                                        onClick = { /* Handle completion */ },
////                                        colors = RadioButtonDefaults.colors(
////                                            selectedColor = MaterialTheme.colorScheme.primary,
////                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
////                                        )
////                                    )
////                                    Text(
////                                        text = subtask,
////                                        style = MaterialTheme.typography.bodyMedium,
////                                        color = MaterialTheme.colorScheme.onSurface
////                                    )
////                                }
////                                Icon(
////                                    imageVector = if (expandedSubtaskId == index)
////                                        Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
////                                    contentDescription = "Toggle details",
////                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
////                                )
////                            }
////
////                            if (expandedSubtaskId == index) {
////                                Column(
////                                    modifier = Modifier.padding(16.dp),
////                                    verticalArrangement = Arrangement.spacedBy(8.dp)
////                                ) {
////                                    Text(
////                                        text = "- Разработать макеты основных экранов.",
////                                        style = MaterialTheme.typography.bodyMedium,
////                                        color = MaterialTheme.colorScheme.onSurfaceVariant
////                                    )
////                                    Text(
////                                        text = "- Утвердить дизайн с клиентами/заказчиками.",
////                                        style = MaterialTheme.typography.bodyMedium,
////                                        color = MaterialTheme.colorScheme.onSurfaceVariant
////                                    )
////                                    Text(
////                                        text = "До 10.12.2024",
////                                        style = MaterialTheme.typography.bodySmall,
////                                        color = MaterialTheme.colorScheme.onSurfaceVariant
////                                    )
////                                }
////                            }
////                        }
////                    }
////                }
////
////                // Add subtask button
////                Surface(
////                    modifier = Modifier.fillMaxWidth(),
////                    color = MaterialTheme.colorScheme.surfaceVariant,
////                    shape = MaterialTheme.shapes.medium
////                ) {
////                    Row(
////                        modifier = Modifier
////                            .clickable { /* Handle add subtask */ }
////                            .padding(16.dp),
////                        horizontalArrangement = Arrangement.Center,
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        Icon(
////                            imageVector = Icons.Default.Add,
////                            contentDescription = "Add subtask",
////                            tint = MaterialTheme.colorScheme.onSurfaceVariant
////                        )
////                        Spacer(modifier = Modifier.width(8.dp))
////                        Text(
////                            text = "Добавить подзадачу",
////                            style = MaterialTheme.typography.bodyMedium,
////                            color = MaterialTheme.colorScheme.onSurfaceVariant
////                        )
////                    }
////                }
////            }
//
////            // Files section
////            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
////                Row(
////                    modifier = Modifier.fillMaxWidth(),
////                    horizontalArrangement = Arrangement.SpaceBetween,
////                    verticalAlignment = Alignment.CenterVertically
////                ) {
////                    Text(
////                        text = "Файлы",
////                        style = MaterialTheme.typography.titleMedium,
////                        color = MaterialTheme.colorScheme.onSurface
////                    )
////                    IconButton(onClick = { /* Handle add file */ }) {
////                        Icon(
////                            imageVector = Icons.Default.Add,
////                            contentDescription = "Add file",
////                            tint = MaterialTheme.colorScheme.onSurfaceVariant
////                        )
////                    }
////                }
////
////                LazyRow(
////                    horizontalArrangement = Arrangement.spacedBy(8.dp)
////                ) {
////                    items(files) { file ->
////                        Surface(
////                            modifier = Modifier.size(100.dp),
////                            shape = MaterialTheme.shapes.medium,
////                            color = MaterialTheme.colorScheme.surfaceVariant
////                        ) {
////                            Column(
////                                modifier = Modifier.padding(8.dp),
////                                horizontalAlignment = Alignment.CenterHorizontally,
////                                verticalArrangement = Arrangement.SpaceBetween
////                            ) {
////                                Icon(
////                                    imageVector = Icons.Default.Delete,
////                                    contentDescription = "Delete file",
////                                    modifier = Modifier.align(Alignment.End),
////                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
////                                )
////                                Text(
////                                    text = file,
////                                    style = MaterialTheme.typography.bodySmall,
////                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
////                                    textAlign = TextAlign.Center
////                                )
////                            }
////                        }
////                    }
////                }
////            }
//
//            // Responsible persons
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                Text(
//                    text = "Ответственные",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                responsible.forEach { (name, role) ->
//                    Surface(
//                        modifier = Modifier.fillMaxWidth(),
//                        color = MaterialTheme.colorScheme.surfaceVariant,
//                        shape = MaterialTheme.shapes.medium
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(16.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = name,
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurface
//                            )
//                            Text(
//                                text = role,
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Equipment
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                Text(
//                    text = "Используемое оборудование",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                FlowRow(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    equipment.forEach { item ->
//                        Surface(
//                            color = MaterialTheme.colorScheme.surfaceVariant,
//                            shape = MaterialTheme.shapes.medium
//                        ) {
//                            Text(
//                                text = item,
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

// Тестовые данные для файлов
val files = listOf(
    "Техническое задание.pdf",
    "Договор.docx",
    "Макет_проекта.png",
    "Презентация.pptx"
)

// Тестовые данные для ответственных
val responsible = listOf(
    "Иван Иванов" to "Руководитель проекта",
    "Мария Петрова" to "Дизайнер",
    "Алексей Сидоров" to "Разработчик",
    "Ольга Смирнова" to "Тестировщик"
)

// Тестовые данные для оборудования
val equipment = listOf(
    "Ноутбук Dell XPS 13",
    "Монитор LG UltraFine 27'",
    "Графический планшет Wacom Intuos",
    "Принтер HP LaserJet Pro"
)

// Тестовые данные для подзадач
val subTasks = listOf(
    "Составить план проекта",
    "Подготовить презентацию",
    "Согласовать дизайн-макет",
    "Разработать функционал авторизации",
    "Протестировать основные модули"
)
