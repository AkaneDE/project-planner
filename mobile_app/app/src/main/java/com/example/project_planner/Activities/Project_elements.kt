package com.example.project_planner.Activities

import SectionLabel
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_planner.Category
import com.example.project_planner.Project
import com.example.project_planner.ProjectContent
import com.example.project_planner.R
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.addCategory
import kotlinx.coroutines.launch


@Composable
fun ProjectListScreen(
    userId: Int,
    onProjectDetailsClick: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var allProjects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var assigned by remember { mutableStateOf<List<Project>>(emptyList()) }
    var filteredProjects by remember { mutableStateOf<List<Project>>(emptyList()) }
    val categories = remember {
        mutableStateListOf(
            Category(-1, "Все", mutableStateOf(true)),
            Category(0, "Назначенные", mutableStateOf(false))
        )
    }

    var isLoading by remember { mutableStateOf(true) }

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getProjects(worker_id = userId)
            allProjects = response.projects
            val response2 = apiService.getProjectsAssigned(worker_id = userId)
            assigned = response2.projects
            filteredProjects = allProjects

            response.categories.forEach {
                categories.add(Category(it.category_id, it.category_name))
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Ошибка загрузки данных")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                TopBar(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        searchQuery = query
                        filteredProjects = filterProjects(allProjects, assigned, query, categories)
                    },
                    onSearchToggle = {
                        isSearching = !isSearching
                        if (!isSearching) {
                            searchQuery = ""
                            filteredProjects = filterProjects(allProjects,assigned, "", categories)
                        }
                    }
                )

                FilterRow(
                    categories = categories,
                    onFilterSelected = { selected ->
                        categories.forEach {
                            it.isSelected.value = it.category_id == selected.category_id
                        }
                        filteredProjects = filterProjects(allProjects,assigned, searchQuery, categories)
                    },
                    onAddFilter = { newCategoryName ->
                        coroutineScope.launch {
                            val result = addCategory(apiService, newCategoryName, userId)
                            result.onSuccess { newCat ->
                                categories.forEach { it.isSelected.value = false }
                                categories.add(Category(newCat.category_id, newCat.category_name, mutableStateOf(true)))
                                filteredProjects = filterProjects(allProjects,assigned, searchQuery, categories)
                            }.onFailure {
                                snackbarHostState.showSnackbar(
                                    message = it.message ?: "Ошибка при добавлении категории",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )

                ProjectList(onProjectDetailsClick = onProjectDetailsClick, projects = filteredProjects.toMutableList())
            }
        }
    }
}

fun filterProjects(
    all: List<Project>,
    assigned: List<Project>,
    query: String,
    categories: List<Category>
): List<Project> {
    val selectedCategory = categories.find { it.isSelected.value }

    val base = when (selectedCategory?.category_name) {
        "Все" -> all
        "Назначенные" -> assigned
        else -> all.filter { it.category_id == selectedCategory?.category_id }
    }

    return if (query.isNotBlank()) {
        base.filter { it.project_name.contains(query, ignoreCase = true) }
    } else {
        base
    }
}




@Composable
fun ProjectHeader(
    projectName: String,
    categoryName: String?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    showEditIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = projectName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }


            if (showEditIcon) {

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_change),
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

            } else {
                Spacer(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
            }
        }
        if (!categoryName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = categoryName,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Composable
fun TaskHeader(
    taskName: String,
    task_daystart: String,
    task_deadline: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    showEditIcon: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center)
                    .fillMaxWidth(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = taskName,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (showEditIcon) {

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_change),
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

            } else {
                Spacer(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$task_daystart — $task_deadline",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}


@Composable
fun ProjectContentCard(
    content: ProjectContent,
    onClick: (ProjectContent) -> Unit
) {
    val iconResId = when (content.content_type) {
        "image" -> R.drawable.ic_img
        "doc" -> R.drawable.ic_dox
        "pdf" -> R.drawable.ic_pdf
        "mp4" -> R.drawable.ic_mp4
        "mp3" -> R.drawable.ic_mp3
        "xls", "xlsx", "csv" -> R.drawable.ic_xlx
        else -> R.drawable.ic_uni
    }
    var readContent by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(100.dp)
            .padding(end = 12.dp)
            .clickable { onClick(content) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content.content_name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProjectContentSection(
    contents: List<ProjectContent>?,
    onItemClick: (ProjectContent) -> Unit,
    onAddClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("Материалы")
            IconButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_c),
                    contentDescription = "Добавить",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .height(120.dp)
                    .padding(horizontal = 10.dp)
            ) {
                if (!contents.isNullOrEmpty()) {
                    items(contents) { content ->
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            ProjectContentCard(content = content, onClick = onItemClick)
                        }
                    }
                }
                else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Материалов пока нет",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                }
            }
        }
    }
}
