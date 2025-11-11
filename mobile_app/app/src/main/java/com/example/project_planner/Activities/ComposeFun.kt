package com.example.project_planner.Activities

import CustomTextField
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.project_planner.Category
import com.example.project_planner.Project
import com.example.project_planner.R


@Composable
fun ProjectList(onProjectDetailsClick: (Int) -> Unit, projects: MutableList<Project>) {
    // Список проектов, отображаемый вертикально
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp), // Расстояние между проектами
    ) {
        items(projects) { project -> // Перебор списка проектов
            ProjectCard(project, onProjectClick = onProjectDetailsClick)
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onProjectClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onProjectClick(project.project_id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.project_name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Text(
                    text = project.project_deadline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (project.project_progress.toFloat()/100.0).toFloat())
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

@Composable
fun FilterRow(
    categories: MutableList<Category>,
    onFilterSelected: (Category) -> Unit,
    onAddFilter: (String) -> Unit
) {
    var isAddingFilter by remember { mutableStateOf(false) }
    var newFilterText by remember { mutableStateOf("") }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        item {
            if (isAddingFilter) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillParentMaxWidth()
                ) {
                    CustomTextField(
                        value = newFilterText,
                        onValueChange = { newFilterText = it },
                        placeholderText = "Введите название категории",
                        width = 0.85f
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newFilterText.isNotBlank()) {
                                onAddFilter(newFilterText)
                                newFilterText = ""
                            }
                            isAddingFilter = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                FilterChip(
                    selected = false,
                    onClick = { isAddingFilter = true },
                    label = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = "Add filter",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
        items(categories) { category ->
            FilterChip(
                selected = category.isSelected.value,
                onClick = { onFilterSelected(category) },
                label = {
                    Text(
                        text = category.category_name,
                        style = MaterialTheme.typography.bodyMedium
                    )                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onSecondary
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (isSearching) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
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
                    modifier = Modifier.weight(2f)
                )
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(24.dp)) // для симметрии с иконкой
                Text(
                    text = "Мои проекты",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(30.dp))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onItemSelected(0) }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (selectedItem == 0) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.background
                )
            }
            IconButton(onClick = { onItemSelected(2) }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = if (selectedItem == 2) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.background
                )
            }
        }
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary)
                .clickable { onItemSelected(1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}








