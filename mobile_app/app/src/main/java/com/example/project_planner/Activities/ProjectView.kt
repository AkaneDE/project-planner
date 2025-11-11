package com.example.project_planner.Activities

import CustomButton
import DropdownItem
import ScrollableTextWithCustomScrollbar
import SectionLabel
import SimpleDropdown
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.project_planner.ApiService
import com.example.project_planner.ProjectContent
import com.example.project_planner.ProjectOut
import com.example.project_planner.R
import com.example.project_planner.RetrofitClient
import com.example.project_planner.completeProject
import com.example.project_planner.completeTask
import com.example.project_planner.createPartFromString
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ProjectDetailsScreen(
    projectId: Int,
    userId: Int,
    apiService: ApiService,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onTaskDetailsClick: (Int) -> Unit,
    onAddTask: () -> Unit,
    snackbarHostState: SnackbarHostState,

) {
    var project by remember { mutableStateOf<ProjectOut?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var addContent by remember { mutableStateOf(false) }
    var readContent by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf<ProjectContent?>(null) }
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getProject(projectId)
            project = response
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
    if (project != null) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

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
                    ProjectHeader(
                        projectName = project!!.project_name,
                        categoryName = project!!.category_name,
                        onBackClick = onBackClick,
                        onEditClick = onEditClick,
                        showEditIcon = if (project!!.worker_id == userId) true else false
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Описание проекта")
                        ScrollableTextWithCustomScrollbar(
                            text = project!!.project_description ?: "Нет описания",
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SectionLabel("Этапы создания проекта")
                            if (project!!.project_status && project!!.worker_id == userId) {
                                IconButton(onClick = { onAddTask() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_add_c),
                                        contentDescription = "Добавить этап",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        project!!.tasks
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f,
                                    fill = false
                                )
                        ) {
                            items(project!!.tasks ?: emptyList()) { task ->
                                StageItem(
                                    stageName = task.task_name,
                                    stageId = task.task_id,
                                    taskClick = onTaskDetailsClick,
                                    isCompleted = task.task_status,
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val dueDate = project!!.project_deadline
                        SectionLabel("Сроки сдачи")
                        SectionLabel("до $dueDate", MaterialTheme.colorScheme.onPrimary)
                    }
                    ProjectContentSection(
                        contents = project!!.contents,
                        onItemClick = {
                            readContent = true
                            content = it
                        },
                        onAddClick = { addContent = true })
                    val context = LocalContext.current
                    if (readContent) {
                        content?.let {
                            ContentDialog(
                                showDialog = readContent,
                                onDismiss = { readContent = !readContent },
                                content = it,
                                onSave = { url, name ->
                                    coroutineScope.launch {
                                        val response = apiService.downloadFile(url)
                                        if (response.isSuccessful && response.body() != null) {
                                            saveFileFromResponseBody(
                                                context,
                                                response.body()!!,
                                                name
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                url
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
                                    if (project!!.workers?.any { it.worker_id == userId } == true) {
                                        validateAndCreateParts(context, name, text, uri)
                                            .onSuccess { filePart ->
                                                val result = runCatching {
                                                    RetrofitClient.apiService.uploadProjectContent(
                                                        workerId = userId,
                                                        projectId = projectId,
                                                        title = createPartFromString(name),
                                                        text = createPartFromString(text),
                                                        file = filePart
                                                    )
                                                }
                                                project = apiService.getProject(projectId)

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
                    val dropdownFriends =
                        project!!.workers?.map { DropdownItem(it.worker_id, it.worker_nickname) }
                            ?: emptyList()
                    SimpleDropdown(
                        items = dropdownFriends,
                        headerText = "Команда",
                        onItemSelected = { selectedItem ->
                            println("Выбран: ${selectedItem.text}")
                        }
                    )
                    if (project!!.worker_id == userId) {
                        if (project!!.project_status) {
                            var showDialog by remember { mutableStateOf(false) }
                            CustomButton("Завершить проект") { showDialog = true }
                            if (showDialog) {
                                CompleteDialog(
                                    showDialog = showDialog,
                                    onDismiss = { showDialog = !showDialog },
                                    onConfirm = {
                                        showDialog = false
                                        coroutineScope.launch {
                                            val result = completeProject(apiService, projectId)
                                            result.onSuccess {
                                                project = apiService.getProject(projectId)
                                            }.onFailure {
                                                snackbarHostState.showSnackbar(
                                                    it.message ?: "Ошибка при завершении"
                                                )
                                            }
                                        }
                                    },
                                    titleText = "Завершить проект"
                                )
                            }
                        }
//                         else {
//                            if (project!!.report_title == null) {
//                                var showDialog by remember { mutableStateOf(false) }
//                                var documentName by remember { mutableStateOf("") }
//                                var documentDescription by remember { mutableStateOf("") }
//                                var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
//
//                                val filePickerLauncher = rememberLauncherForActivityResult(
//                                    contract = ActivityResultContracts.GetContent(),
//                                    onResult = { uri ->
//                                        selectedFileUri = uri
//                                    }
//                                )
//                                CustomButton("Добавить документацию") { showDialog = true }
//
//
//                                if (showDialog) {
//                                    Dialog(onDismissRequest = { showDialog = false }) {
//                                        Surface(
//                                            shape = RoundedCornerShape(16.dp),
//                                            modifier = Modifier.padding(16.dp),
//                                            tonalElevation = 8.dp
//                                        ) {
//                                            Column(
//                                                modifier = Modifier.padding(16.dp),
//                                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                                            ) {
//                                                Text(
//                                                    text = "Добавить документацию",
//                                                    style = MaterialTheme.typography.titleMedium
//                                                )
//
//                                                // Name Input
//                                                OutlinedTextField(
//                                                    value = documentName,
//                                                    onValueChange = { documentName = it },
//                                                    label = { Text("Название документа") },
//                                                    modifier = Modifier.fillMaxWidth()
//                                                )
//
//                                                // Description Input
//                                                OutlinedTextField(
//                                                    value = documentDescription,
//                                                    onValueChange = { documentDescription = it },
//                                                    label = { Text("Описание документа") },
//                                                    modifier = Modifier.fillMaxWidth()
//                                                )
//
//                                                // File Picker
//                                                Button(
//                                                    onClick = { filePickerLauncher.launch("*/*") },
//                                                    modifier = Modifier.fillMaxWidth()
//                                                ) {
//                                                    Text("Выбрать файл")
//                                                }
//
//                                                // Show selected file URI
//                                                selectedFileUri?.let {
//                                                    Text(
//                                                        text = "Выбранный файл: ${it.path}",
//                                                        style = MaterialTheme.typography.bodySmall
//                                                    )
//                                                }
//
//                                                Spacer(modifier = Modifier.height(16.dp))
//                                                val coroutineScope = rememberCoroutineScope()
//
//                                                // Buttons: Confirm or Cancel
//                                                Row(
//                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                                    modifier = Modifier.fillMaxWidth()
//                                                ) {
//                                                    val context = LocalContext.current
//
//                                                    Button(
//                                                        onClick = {
//
//                                                            coroutineScope.launch {
//
//                                                                // Проверяем, что пользователь ввел название и выбрал файл
//                                                                if (documentName.isBlank()) {
//                                                                    snackbarHostState.showSnackbar("Ошибка: введите название документа!")
//                                                                    return@launch
//                                                                }
//
//                                                                if (selectedFileUri == null) {
//                                                                    snackbarHostState.showSnackbar("Ошибка: файл не выбран!")
//                                                                    return@launch
//                                                                }
//
//                                                                // Создаем части для запроса
//                                                                val projectIdPart =
//                                                                    createPartFromString(projectId.toString()) // Замените "123" на реальный project_id
//                                                                val titlePart =
//                                                                    createPartFromString(
//                                                                        documentName
//                                                                    )
//                                                                val descriptionPart =
//                                                                    createPartFromString(
//                                                                        documentDescription
//                                                                    )
//                                                                val filePart = createFilePart(
//                                                                    context,
//                                                                    selectedFileUri!!,
//                                                                    "file"
//                                                                ) // Гарантируем, что URI не null
//
//                                                                // Выполняем запрос
//                                                                val result = runCatching {
//                                                                    if (filePart != null) {
////                                                                    RetrofitClient.apiService.uploadReport(
////                                                                        projectId = projectIdPart,
////                                                                        title = titlePart,
////                                                                        text = descriptionPart,
////                                                                        file = filePart
////                                                                    )
//                                                                    }
//                                                                }
//
//                                                                // Обрабатываем результат
//                                                                result.onSuccess { response ->
//                                                                    snackbarHostState.showSnackbar("Документация успешно отправлена!")
//                                                                }.onFailure { error ->
//                                                                    snackbarHostState.showSnackbar(
//                                                                        message = error.message
//                                                                            ?: "Ошибка при отправке документации",
//                                                                        duration = SnackbarDuration.Short
//                                                                    )
//                                                                }
//
//                                                                showDialog =
//                                                                    false // Закрываем диалог после завершения операции
//                                                                apiService.getProject(projectId)
//
//                                                            }
//                                                        },
//                                                        modifier = Modifier.weight(1f)
//                                                    ) {
//                                                        Text("Сохранить")
//                                                    }
//
//
//                                                    OutlinedButton(
//                                                        onClick = { showDialog = false },
//                                                        modifier = Modifier.weight(1f)
//                                                    ) {
//                                                        Text("Отмена")
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//
//                            } else {
//                                Button(
//                                    onClick = {
//                                    },
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 16.dp),
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = MaterialTheme.colorScheme.surface,
//                                        contentColor = MaterialTheme.colorScheme.onSurface
//                                    ),
//                                    border = BorderStroke(
//                                        1.dp,
//                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//                                    ),
//                                    shape = RoundedCornerShape(12.dp)
//                                ) {
//                                    Text("Документация добавлена")
//                                }
//                            }
//
//                        }

                    }
                }
            }
        }
    }
}
@Composable
fun StageItem(
    stageName: String,
    stageId: Int,
    isCompleted: Boolean,
    taskClick: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    var showDialogTask by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(isCompleted) }
    Surface(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable { if (completed) showDialogTask = true },
                contentAlignment = Alignment.Center
            ) {
                if (!completed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_circle_filled),
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_circle_outline),
                        contentDescription = "Not Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stageName,
                color = if (completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { taskClick(stageId) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Open Stage",
                    tint = Color(0xFF6A90B6)
                )
            }
        }
    }
    if (showDialogTask) {
        CompleteDialog(
            showDialog = showDialogTask,
            onDismiss = { showDialogTask = !showDialogTask },
            onConfirm = {
                showDialogTask = false
                coroutineScope.launch {
                    val result = completeTask(RetrofitClient.apiService, stageId)
                    result.onSuccess {
                        completed = false
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


