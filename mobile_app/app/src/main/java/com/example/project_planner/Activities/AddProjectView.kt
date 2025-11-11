package com.example.project_planner.Activities

import CustomButton
import CustomTextField
import DateInputFields
import DropdownItem
import Label
import SectionLabel
import SelectableDropdown
import SingleSelectableDropdown
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_planner.Category
import com.example.project_planner.ProjectCreate
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.WorkerSearch
import com.example.project_planner.addProject
import kotlinx.coroutines.launch

@Composable
fun CreateProjectScreen(
    userId: Int,
    onClose: () -> Unit = {},
    snackbarHostState: SnackbarHostState, // Передаем Snackbar

) {
    var category_id by remember { mutableStateOf(-1) }
    var project_name by remember { mutableStateOf("") }
    var project_description by remember { mutableStateOf("") }
    var project_daystart by remember { mutableStateOf("") }
    var project_deadline by remember { mutableStateOf("") }
    var categories by remember {
        mutableStateOf<MutableList<Category>?>(
            mutableListOf()
        )
    }
    var selectedItem by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedFriends by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }
    var workers by remember {
        mutableStateOf<MutableList<WorkerSearch>?>(
            mutableListOf()
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dropdownCategory: List<DropdownItem>
    var dropdownFriends: List<DropdownItem>
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getCategoriesFriends(userId)
            categories = response.categories?.toMutableList()
            workers =response.friends?.toMutableList()
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    if (!isLoading) {
        dropdownCategory = categories?.map { DropdownItem(it.category_id, it.category_name) }!!
        dropdownFriends = workers?.map { DropdownItem(it.worker_id, it.worker_nickname) }!!
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                .imePadding() // <-- Это ключ к адаптации под клавиатуру

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Label("Создание проекта", onClose)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Категория")
                    SingleSelectableDropdown(
                        items = dropdownCategory,
                        selectedItem = selectedItem,
                        onItemSelected = {
                            selectedItem = it
                            if (selectedItem == null || selectedItem!!.id == 0) selectedItem = null
                            else selectedItem = it
                        },
                        placeholderText = "Выберите категорию"
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Название")
                    CustomTextField(
                        value = project_name,
                        onValueChange = { if (it.length <= 120) project_name = it },
                        placeholderText = "Не более 120 символов",
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
                        text = "Выберите друга"
                    )
                }
                val coroutineScope = rememberCoroutineScope()
                CustomButton("Создать") {
                    category_id = selectedItem?.id ?: category_id
                    val project = ProjectCreate(project_name, project_description, project_daystart, project_deadline, worker_id = userId, category_id, selectedFriends.map { it.id } )
                    coroutineScope.launch {
                        val result = addProject(apiService, project)
                        result.onSuccess {
                            onClose()
                        }.onFailure { error ->
                            // Показ ошибки через Snackbar
                            snackbarHostState.showSnackbar(
                                message = error.message ?: "Ошибка при добавлении проекта",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun GetUsers(
//    selectedUsers: List<WorkerSearch>,
//    onUsersChange: (List<WorkerSearch>) -> Unit,
//    users: List<WorkerSearch>
//) {
//    Column {
//        Text(
//            text = "Назначить ответственных",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        var usersExpanded by remember { mutableStateOf(false) }
//        ExposedDropdownMenuBox(
//
//            expanded = usersExpanded,
//            onExpandedChange = { usersExpanded = it }
//        ) {
//            OutlinedTextField(
//                value = if (selectedUsers.isNotEmpty()) "${selectedUsers.size} выбрано" else "",
//                onValueChange = {},
//                readOnly = true,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .menuAnchor(),
//                trailingIcon = {
//                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = usersExpanded)
//                },
//                colors = OutlinedTextFieldDefaults.colors(
//                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
//                        alpha = 0.3f
//                    )
//                )
//            )
//
//            ExposedDropdownMenu(
//                expanded = usersExpanded,
//                onDismissRequest = { usersExpanded = false }
//            ) {
//                // Исключаем выбранных пользователей из списка
//                val availableUsers = users.filterNot { user -> selectedUsers.contains(user) }
//
//                availableUsers.forEach { user ->
//                    DropdownMenuItem(
//                        text = { Text("${user.worker_name}, ${user.worker_nickname}") },
//                        onClick = {
//                            onUsersChange(selectedUsers + user)
//                            usersExpanded = false
//                        }
//                    )
//                }
//            }
//        }
//
//        // Selected users chips
//        FlowRow(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            selectedUsers.forEach { user ->
//                InputChip(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    selected = false,
//                    onClick = { },
//                    label = { Text("${user.worker_name}, ${user.worker_nickname}") },
//                    trailingIcon = {
//                        IconButton(
//                            onClick = { onUsersChange(selectedUsers - user) }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "Remove user"
//                            )
//                        }
//                    }
//                )
//            }
//        }
//    }
//}


