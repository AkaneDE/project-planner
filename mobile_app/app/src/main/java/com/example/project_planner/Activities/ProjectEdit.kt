package com.example.project_planner.Activities

import CustomButton
import CustomTextField
import DateInputFields
import DropdownItem
import Label
import ScrollableTextWithCustomScrollbar
import SectionLabel
import SelectableDropdown
import SingleSelectableDropdown
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_planner.Category
import com.example.project_planner.NotificationItem2
import com.example.project_planner.ProjectContent
import com.example.project_planner.ProjectCreate
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.WorkerSearch
import com.example.project_planner.deleteProject
import com.example.project_planner.updateProject
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.OutputStream


@Composable
fun EditProjectScreen(
    projectId: Int,
    userId: Int,
    onClose: () -> Unit,
    onDelete:() -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var project_name by remember { mutableStateOf("") }
    var project_description by remember { mutableStateOf("") }
    var project_daystart by remember { mutableStateOf("") }
    var project_deadline by remember { mutableStateOf("") }

    var selectedItem by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedFriends by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var friends by remember { mutableStateOf<List<WorkerSearch>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val project = apiService.getProject(projectId)
            val lists = apiService.getCategoriesFriends(userId)

            project_name = project.project_name
            project_description = project.project_description ?: ""
            project_daystart = project.project_daystart
            project_deadline = project.project_deadline

            categories = lists.categories ?: emptyList()
            friends = lists.friends ?: emptyList()

            selectedItem = categories.find { it.category_name == project.category_name }
                ?.let { DropdownItem(it.category_id, it.category_name) }

            selectedFriends = project.workers?.filter { it.worker_id != userId }?.map {
                DropdownItem(it.worker_id, it.worker_nickname)
            } ?: emptyList()

        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        return
    }

    val dropdownCategory = categories.map { DropdownItem(it.category_id, it.category_name) }
    val dropdownFriends = friends
        .filter { it.worker_id != userId }
        .map { DropdownItem(it.worker_id, it.worker_nickname) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Label("Редактирование проекта", onClose)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Категория")
                SingleSelectableDropdown(
                    items = dropdownCategory,
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    placeholderText = "Выберите категорию"
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Название")
                CustomTextField(
                    value = project_name,
                    onValueChange = { if (it.length <= 30) project_name = it },
                    placeholderText = "Не более 30 символов",
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Описание проекта")
                CustomTextField(
                    value = project_description,
                    onValueChange = { if (it.length <= 2000) project_description = it },
                    placeholderText = "Не более 2000 символов",
                    height = 150.dp
                )
            }

            DateInputFields(
                startDate = project_daystart,
                onStartDateChange = { project_daystart = it },
                endDate = project_deadline,
                onEndDateChange = { project_deadline = it }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Команда")
                SelectableDropdown(
                    allItems = dropdownFriends,
                    selectedItems = selectedFriends,
                    onItemSelected = { item ->
                        if (!selectedFriends.contains(item)) {
                            selectedFriends = selectedFriends + item
                        }
                    },
                    onItemRemoved = { item ->
                        selectedFriends = selectedFriends - item
                    },
                    maxDropdownHeight = 200.dp,
                    text = "Выберите участника"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomButton("Сохранить", widthFraction = 0.47f) {
                    val updatedProject = ProjectCreate(
                        project_name = project_name,
                        project_description = project_description,
                        project_daystart = project_daystart,
                        project_deadline = project_deadline,
                        worker_id = userId,
                        category_id = selectedItem?.id ?: -1,
                        team = selectedFriends.map { it.id }
                    )

                    coroutineScope.launch {
                        val result = updateProject(apiService, projectId, updatedProject)
                        result.onSuccess {
                            onClose()
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar(
                                message = error.message ?: "Ошибка при обновлении проекта",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                var showDialog by remember { mutableStateOf(false) }
                CustomButton("Удалить",
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    borderColor = MaterialTheme.colorScheme.onPrimary) {
                    showDialog = true
                }


                if (showDialog) {
                    ConfirmationDialog(
                        showDialog = showDialog,
                        onDismiss = { showDialog = false },
                        onConfirm = {
                            showDialog = false
                            coroutineScope.launch {
                                val result = deleteProject(apiService, projectId)
                                result.onSuccess { onDelete() }
                                    .onFailure { error ->
                                        snackbarHostState.showSnackbar(
                                            message = error.message ?: "Ошибка при удалении",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                            }
                        },
                        titleText = "Удалить проект",
                        headerText = "Подтверждение",
                        bodyText = "Вы уверены, что хотите удалить проект? После подтверждения проект будет удалён навсегда",
                        confirmButtonText = "Удалить",
                        dismissButtonText = "Отмена"
                    )
                }


            }
        }
    }
}


@Composable
fun NotificationDialog(
    showDialog: Boolean,
    notifications: List<NotificationItem2>?,
    onDismiss: (List<NotificationItem2>) -> Unit,
    onReject: (NotificationItem2) -> Unit,
    onAccept: (NotificationItem2) -> Unit,
    dismissButtonText: String = "Закрыть"
) {
    if (showDialog) {
        val localNotifications = remember(notifications) {
            notifications?.map {
                it.copy(
                    viewed = mutableStateOf(it.viewed.value),
                    accepted = mutableStateOf(it.accepted.value)
                )
            }?.toMutableStateList() ?: mutableStateListOf()
        }

        AlertDialog(
            onDismissRequest = {
                onDismiss(localNotifications)
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(6.dp),
            title = {
                Text(
                    text = "Уведомления",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            text = {
                if (localNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет уведомлений",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(localNotifications, key = { it.notification_task_id }) { notification ->

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        if (!notification.viewed.value!!)
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                        else
                                            Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "От: ${notification.author}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                    Text(
                                        text = notification.notification_task_time,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notification.notification_task_text,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (notification.accepted.value==null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(onClick = {
                                            notification.accepted.value = true
                                            onAccept(notification)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Принять",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }

                                        IconButton(onClick = {
                                            notification.accepted.value = false
                                            onReject(notification)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Отклонить",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (notification.accepted.value!!) "Принято" else "Отклонено",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CustomButton(dismissButtonText, onClick = {
                        localNotifications.forEach { it.viewed.value = true }
                        onDismiss(localNotifications)
                    }, widthFraction = 0.9f)
                }
            },
            dismissButton = {}
        )
    }
}





@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    titleText: String,
    headerText: String,
    bodyText: String,
    confirmButtonText: String = "Подтвердить",
    dismissButtonText: String = "Отмена"
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(6.dp),
            title = {
                Column {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Text(
                        text = headerText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(end = 20.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            text = {
                Column {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = bodyText,
                            style = TextStyle(fontSize = 14.sp),
                            modifier = Modifier.padding(bottom = 4.dp, end = 10.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomButton(confirmButtonText, widthFraction = 0.47f) {
                        onConfirm()
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    CustomButton(dismissButtonText) {
                        onDismiss()
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
fun CompleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    titleText: String,
    confirmButtonText: String = "Завершить",
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(6.dp),
            title = {
                Box(contentAlignment = Alignment.Center, modifier =  Modifier.fillMaxWidth()) {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            },
            text = {

            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomButton(confirmButtonText, widthFraction = 0.47f) {
                        onConfirm()
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    CustomButton("Отменить") {
                        onDismiss()
                    }
                }
            },
            dismissButton = {

            }
        )
    }
}

@Composable
fun AddContentDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    titleText: String = "Добавить контент",
    onSave: (String, String, Uri?) -> Unit
) {
    var content_name by remember { mutableStateOf("") }
    var content_text by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp),
            title = {
                Column {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(end = 20.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            text = {
                Column {

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Название документа")
                        CustomTextField(
                            value = content_name,
                            onValueChange = { if (it.length <= 120) content_name = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Описание задачи")
                        CustomTextField(
                            value = content_text,
                            onValueChange = { if (it.length <= 2000) content_text = it },
                            placeholderText = "Не более 2000 символов",
                            height = 150.dp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedFileUri?.let {
                            SectionLabel("Выбранный файл: ${it.path}")
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomButton("Добавить", widthFraction = 0.47f) {
                        filePickerLauncher.launch("*/*")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    CustomButton("Сохранить") {
                        onSave(content_name, content_text, selectedFileUri)
                    }
                }

            },
            dismissButton = {}
        )
    }
}

@Composable
fun ContentDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    content: ProjectContent,
    onSave: (String, String) -> Unit
) {


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp),
            title = {
                Column {
                    Text(
                        text = content.content_name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Text(
                        text = "Автор: "+content.worker.worker_nickname,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(end = 20.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            text = {
                Column {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        ScrollableTextWithCustomScrollbar(
                            text = content.content_text ?: "Нет описания",
                        )

                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomButton("Удалить", widthFraction = 0.47f) {
                        onDismiss()
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    CustomButton("Открыть") {
                        onSave(content.content_filepath, content.content_name)
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun saveFileFromResponseBody(context: Context, responseBody: ResponseBody, fileName: String): Boolean {
    return try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                writeResponseBodyToStream(responseBody, outputStream)
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun writeResponseBodyToStream(body: ResponseBody, outputStream: OutputStream) {
    val buffer = ByteArray(4096)
    var bytesRead: Int
    val inputStream = body.byteStream()
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }
    outputStream.flush()
}