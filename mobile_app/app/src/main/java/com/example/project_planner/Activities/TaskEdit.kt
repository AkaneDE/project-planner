package com.example.project_planner.Activities

import CustomButton
import CustomTextField
import DateInputFields
import DropdownItem
import Label
import SectionLabel
import SelectableDropdown
import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.TaskDetailsIn
import com.example.project_planner.WorkerSearch
import com.example.project_planner.addTask
import com.example.project_planner.deleteTask
import com.example.project_planner.updateTask
import kotlinx.coroutines.launch


@SuppressLint("DefaultLocale", "MutableCollectionMutableState")
@Composable
fun CreateTaskScreen(
    projectId:Int,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit = {},
) {


    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var task_daystart by remember { mutableStateOf("") }
    var task_deadline by remember { mutableStateOf("") }
    var users by remember {
        mutableStateOf<MutableList<WorkerSearch>?>(
            mutableListOf()
        )
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedFriends by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }
    var dropdownFriends: List<DropdownItem>

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getTeam(projectId)
            users = users?.toMutableList()?.apply {
                if (response != null) {
                    addAll(response)
                }
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (!isLoading) {
        dropdownFriends = users?.map { DropdownItem(it.worker_id, it.worker_nickname) }!!
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

                    Label("Добавление задачи", onClose)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Название")
                        CustomTextField(
                            value = taskName,
                            onValueChange = { if (it.length <= 120) taskName = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Описание задачи")
                        CustomTextField(
                            value = taskDescription,
                            onValueChange = { if (it.length <= 2000) taskDescription = it },
                            placeholderText = "Не более 2000 символов",
                            height = 150.dp
                        )
                    }

                    DateInputFields(
                        startDate = task_daystart,
                        onStartDateChange = { task_daystart = it },
                        endDate = task_deadline,
                        onEndDateChange = { task_deadline = it }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Назначить ответсвенных")
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
                            text = "Выберите члена команды"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                val coroutineScope = rememberCoroutineScope()
                CustomButton("Добавить") {
                    val task = TaskDetailsIn(
                        task_name = taskName,
                        task_description = taskDescription,
                        task_daystart = task_daystart,
                        task_deadline = task_deadline,
                        team_members = selectedFriends.map { it.id })
                    coroutineScope.launch {
                        val result = addTask(apiService, task, projectId)
                        result.onSuccess {
                            onClose()
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar(
                                message = error.message ?: "Ошибка при добавлении",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale", "MutableCollectionMutableState")
@Composable
fun TaskScreen(
    projectId:Int,
    taskId: Int,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit = {},
    onDelete: () -> Unit = {}
) {


    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var task_daystart by remember { mutableStateOf("") }
    var task_deadline by remember { mutableStateOf("") }
    var users by remember {
        mutableStateOf<MutableList<WorkerSearch>?>(
            mutableListOf()
        )
    }

    var users_team by remember {
        mutableStateOf<MutableList<WorkerSearch>?>(
            mutableListOf()
        )
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedFriends by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }
    var dropdownFriends: List<DropdownItem>

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getTask(taskId)
            taskName = response.task_name
            taskDescription = response.task_description.toString()
            task_daystart = response.task_daystart
            task_deadline = response.task_deadline
            users_team = response.team_members?.toMutableList()
            val response2 = apiService.getTeam(projectId)
            users = response2?.toMutableList()
            users_team?.let {
                selectedFriends = it.map { worker ->
                    DropdownItem(worker.worker_id, worker.worker_nickname)
                }
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (!isLoading) {
        dropdownFriends = users?.map { DropdownItem(it.worker_id, it.worker_nickname) }!!

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

                    Label("Изменение задачи", onClose)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Название")
                        CustomTextField(
                            value = taskName,
                            onValueChange = { if (it.length <= 120) taskName = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Описание задачи")
                        CustomTextField(
                            value = taskDescription,
                            onValueChange = { if (it.length <= 2000) taskDescription = it },
                            placeholderText = "Не более 2000 символов",
                            height = 150.dp
                        )
                    }

                    DateInputFields(
                        startDate = task_daystart,
                        onStartDateChange = { task_daystart = it },
                        endDate = task_deadline,
                        onEndDateChange = { task_deadline = it }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Назначить ответсвенных")
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
                            text = "Выберите члена команды"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                val coroutineScope = rememberCoroutineScope()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomButton("Обновить",
                        widthFraction = 0.47f) {
                        val task = TaskDetailsIn(
                            taskName,
                            taskDescription,
                            task_daystart,
                            task_deadline,
                            selectedFriends.map { it.id },
                        )
                        coroutineScope.launch {
                            val result =
                                updateTask(apiService, taskId, task)
                            result.onSuccess {
                                onClose()
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar(
                                    message = error.message ?: "Ошибка при обновлении",
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
                                    val result = deleteTask(apiService, taskId)
                                    result.onSuccess {
                                        onDelete()
                                    }.onFailure { error ->
                                        snackbarHostState.showSnackbar(
                                            message = error.message ?: "Ошибка при удалении",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            titleText = "Удалить задачу",
                            headerText = "Подтверждение",
                            bodyText = "Вы уверены, что хотите удалить задачу? После подтверждения задача будет удалёна навсегда",
                            confirmButtonText = "Удалить",
                            dismissButtonText = "Отмена"
                        )
                    }
                }
            }
        }
    }
}







//@SuppressLint("DefaultLocale")
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun CreateTaskScreen(
//    idProject: Int,
//    onClose: () -> Unit = {},
//) {
//
//    var taskName by remember { mutableStateOf("") }
//    var taskDescription by remember { mutableStateOf("") }
//    var endDate by remember { mutableStateOf("") }
////    var notificationPeriod by remember { mutableStateOf("Нет") }
////    var notificationExpanded by remember { mutableStateOf(false) }
//    var selectedUsers by remember { mutableStateOf(setOf<String>()) }
//    var selectedEquipment by remember { mutableStateOf(setOf<String>()) }
////    var selectedColor by remember { mutableStateOf<Color?>(null) }
//
//
//    val users = listOf(
//        "Анастасия Лебедева, frontend-разработчик",
//        "Иван Соколов, руководитель отдела разработки",
//        "Мария Воронина, ведущий разработчик проекта",
//        "Дмитрий Иванов, разработчик мобильных приложений",
//        "Алексей Воронцов, backend-разработчик",
//        "Марина Полякова, UI/UX-дизайнер"
//    )
//
//    val equipment = listOf(
//        "HP EliteDesk 800 G6, PC-DEV-HPED800-1001",
//        "MacBook Pro 16\" (M1), PC-DEV-MBP16-2001",
//        "Dell XPS 13, PC-DEV-DXPS13-2002",
//        "Lenovo ThinkPad X1 Carbon, PC-DEV-TPX1-2001",
//        "Dell XPS 8940, PC-DEV-DXPS8940-1002",
//        "Apple iMac 27\" (M1), PC-DEV-IMAC27-1001"
//    )
//
//    val colors = listOf(
//        Color.Gray,
//        Color(0xFF808080),
//        Color.LightGray,
//        Color.DarkGray,
//        Color(0xFF696969),
//        Color(0xFFD3D3D3),
//        Color(0xFFC0C0C0)
//    )
//    val notificationOptions = listOf(
//        "Нет",
//        "за 5 дней",
//        "за 1 неделю",
//        "за 2 недели"
//    )
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            Box(modifier = Modifier.fillMaxWidth()) {
//                Text(
//                    text = "Изменение задачи",
//                    style = MaterialTheme.typography.headlineSmall,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//                IconButton(
//                    onClick = onClose,
//                    modifier = Modifier.align(Alignment.CenterEnd)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "Close"
//                    )
//                }
//            }
//
//            // Task name field
//            Column {
//                Text(
//                    text = "Название",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                OutlinedTextField(
//                    value = taskName,
//                    onValueChange = { if (it.length <= 30) taskName = it },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Не более 30 символов") },
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
//                    ),
//                    singleLine = true
//                )
//            }
//
//            // Task description field
//            Column {
//                Text(
//                    text = "Описание задачи",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                OutlinedTextField(
//                    value = taskDescription,
//                    onValueChange = { if (it.length <= 500) taskDescription = it },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(120.dp),
//                    placeholder = { Text("Не более 500 символов") },
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
//                    )
//                )
//            }
//
//            val context = LocalContext.current
//
//            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
//                // Start date field with date picker
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text(
//                        text = "Дата кончания",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                    OutlinedTextField(
//                        value = endDate,
//                        onValueChange = { },
//                        placeholder = {
//                            Text(
//                                "дд.мм.гггг",
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                val calendar = Calendar.getInstance()
//                                val year = calendar.get(Calendar.YEAR)
//                                val month = calendar.get(Calendar.MONTH)
//                                val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//                                DatePickerDialog(
//                                    context,
//                                    { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
//                                        endDate = String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
//                                    },
//                                    year,
//                                    month,
//                                    day
//                                ).show()
//                            },
//                        enabled = false,
//                        maxLines = 1,
//                        colors = OutlinedTextFieldDefaults.colors(
//                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
//                            focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
//                        ),
//                        shape = MaterialTheme.shapes.medium
//                    )
//                }
//                }
//
//            // Notification dropdown
//            Column {
////                Text(
////                    text = "Уведомить о сдаче",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = MaterialTheme.colorScheme.onSurfaceVariant
////                )
////                ExposedDropdownMenuBox(
////                    expanded = notificationExpanded,
////                    onExpandedChange = { notificationExpanded = it }
////                ) {
////                    OutlinedTextField(
////                        value = notificationPeriod,
////                        onValueChange = {},
////                        readOnly = true,
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .menuAnchor(),
////                        trailingIcon = {
////                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = notificationExpanded)
////                        },
////                        colors = OutlinedTextFieldDefaults.colors(
////                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
////                        )
////                    )
////
////                    ExposedDropdownMenu(
////                        expanded = notificationExpanded,
////                        onDismissRequest = { notificationExpanded = false }
////                    ) {
////                        notificationOptions.forEach { option ->
////                            DropdownMenuItem(
////                                text = { Text(option) },
////                                onClick = {
////                                    notificationPeriod = option
////                                    notificationExpanded = false
////                                }
////                            )
////                        }
////                    }
////                }
//                // Header with close button
//
//                // Users dropdown section
//                Column {
//                    Text(
//                        text = "Назначить ответственных",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    var usersExpanded by remember { mutableStateOf(false) }
//                    ExposedDropdownMenuBox(
//                        expanded = usersExpanded,
//                        onExpandedChange = { usersExpanded = it }
//                    ) {
//                        OutlinedTextField(
//                            value = "",
//                            onValueChange = {},
//                            readOnly = true,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .menuAnchor(),
//                            trailingIcon = {
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = usersExpanded)
//                            },
//                            colors = OutlinedTextFieldDefaults.colors(
//                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
//                                    alpha = 0.3f
//                                )
//                            )
//                        )
//
//                        ExposedDropdownMenu(
//                            expanded = usersExpanded,
//                            onDismissRequest = { usersExpanded = false }
//                        ) {
//                            users.forEach { user ->
//                                DropdownMenuItem(
//                                    text = { Text(user) },
//                                    onClick = {
//                                        selectedUsers = selectedUsers + user
//                                        usersExpanded = false
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//
//                    // Selected users chips
//                    FlowRow(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        selectedUsers.forEach { user ->
//                            InputChip(
//                                selected = false,
//                                onClick = { },
//                                label = { Text(user) },
//                                trailingIcon = {
//                                    IconButton(
//                                        onClick = { selectedUsers = selectedUsers - user }
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.Close,
//                                            contentDescription = "Remove user"
//                                        )
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
//
//                // Equipment dropdown section
//                Column {
//                    Text(
//                        text = "Занять оборудование",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    var equipmentExpanded by remember { mutableStateOf(false) }
//                    ExposedDropdownMenuBox(
//                        expanded = equipmentExpanded,
//                        onExpandedChange = { equipmentExpanded = it }
//                    ) {
//                        OutlinedTextField(
//                            value = "",
//                            onValueChange = {},
//                            readOnly = true,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .menuAnchor(),
//                            trailingIcon = {
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded)
//                            },
//                            colors = OutlinedTextFieldDefaults.colors(
//                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
//                                    alpha = 0.3f
//                                )
//                            )
//                        )
//
//                        ExposedDropdownMenu(
//                            expanded = equipmentExpanded,
//                            onDismissRequest = { equipmentExpanded = false }
//                        ) {
//                            equipment.forEach { item ->
//                                DropdownMenuItem(
//                                    text = { Text(item) },
//                                    onClick = {
//                                        selectedEquipment = selectedEquipment + item
//                                        equipmentExpanded = false
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//                    // Selected equipment chips
//                    FlowRow(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        selectedEquipment.forEach { item ->
//                            InputChip(
//                                selected = false,
//                                onClick = { },
//                                label = { Text(item) },
//                                trailingIcon = {
//                                    IconButton(
//                                        onClick = {
//                                            selectedEquipment = selectedEquipment - item
//                                        }
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.Close,
//                                            contentDescription = "Remove equipment"
//                                        )
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
////
////                // Color picker section
////                Column {
////                    Text(
////                        text = "Цвет задачи",
////                        style = MaterialTheme.typography.bodyMedium,
////                        color = MaterialTheme.colorScheme.onSurfaceVariant
////                    )
////
////                    Row(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .padding(top = 8.dp),
////                        horizontalArrangement = Arrangement.spacedBy(8.dp)
////                    ) {
////                        colors.forEach { color ->
////                            Box(
////                                modifier = Modifier
////                                    .size(40.dp)
////                                    .clip(CircleShape)
////                                    .background(color)
////                                    .border(
////                                        width = 2.dp,
////                                        color = if (selectedColor == color)
////                                            MaterialTheme.colorScheme.primary
////                                        else Color.Transparent,
////                                        shape = CircleShape
////                                    )
////                                    .clickable { selectedColor = color }
////                            )
////                        }
////
////                        // Add color button
////                        Box(
////                            modifier = Modifier
////                                .size(40.dp)
////                                .clip(CircleShape)
////                                .border(
////                                    width = 1.dp,
////                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
////                                    shape = CircleShape
////                                )
////                                .clickable { /* Handle add color */ },
////                            contentAlignment = Alignment.Center
////                        ) {
////                            Icon(
////                                imageVector = Icons.Default.Add,
////                                contentDescription = "Add color",
////                                tint = MaterialTheme.colorScheme.onSurface
////                            )
////                        }
////                    }
////                }
//
//                Spacer(modifier = Modifier.weight(5f))
//
//                // Create button
//                Button(
//                    onClick = { /* Implement task creation logic */ },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(48.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.secondary
//                    )
//                ) {
//                    Text(
//                        text = "Изменить",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//            }
//        }
//    }
//
//}