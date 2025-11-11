import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

//Текст подзаголовков
@Composable
fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.onSecondary ) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}
//Текст заголовков
@Composable
fun Label(text: String,
          onClose:() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ScrollableTextWithCustomScrollbar(
    text: String,
) {
    val paragraphs = text.split("\n\n")
    val listState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp, max = 100.dp)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .simpleVerticalScrollbar(listState),
            state = listState
        ) {
            items(paragraphs) { paragraph ->
                Text(
                    text = paragraph,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(bottom = 4.dp, end = 10.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun CustomButton(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    textColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.onPrimary,
    widthFraction: Float = 1f,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(50),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .defaultMinSize(minHeight = 50.dp)
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontSize = 14.sp)
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    height: Dp = 50.dp,
    colorsScheme: Boolean = false,
    width: Float = 1f,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var colorp = MaterialTheme.colorScheme.onPrimary
    var color = TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onBackground,        // Цвет текста, когда поле активно (в фокусе)
        unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,    // Цвет текста, когда поле неактивно
        focusedIndicatorColor = Color.Transparent,                     // Цвет нижней линии в фокусе (скрыт)
        unfocusedIndicatorColor = Color.Transparent,                   // Цвет нижней линии без фокуса (скрыт)
        disabledIndicatorColor = Color.Transparent,                    // Цвет нижней линии при отключённом состоянии
        focusedContainerColor = MaterialTheme.colorScheme.secondary,   // Цвет фона при фокусе
        unfocusedContainerColor = MaterialTheme.colorScheme.secondary, // Цвет фона без фокуса
        cursorColor = MaterialTheme.colorScheme.onBackground              // Цвет курсора
    )
    //Для регистрации и входа
    if (colorsScheme) {
        colorp = MaterialTheme.colorScheme.background
        color = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,        // Цвет текста, когда поле активно (в фокусе)
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,    // Цвет текста, когда поле неактивно
            focusedIndicatorColor = Color.Transparent,                     // Цвет нижней линии в фокусе (скрыт)
            unfocusedIndicatorColor = Color.Transparent,                   // Цвет нижней линии без фокуса (скрыт)
            disabledIndicatorColor = Color.Transparent,                    // Цвет нижней линии при отключённом состоянии
            focusedContainerColor = MaterialTheme.colorScheme.primary,   // Цвет фона при фокусе
            unfocusedContainerColor = MaterialTheme.colorScheme.primary, // Цвет фона без фокуса
            cursorColor = MaterialTheme.colorScheme.onBackground              // Цвет курсора
        )
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholderText,
                color = colorp,
                modifier = Modifier.padding(vertical = 0.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            )
        },
        colors = color,
        textStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 16.sp
        ),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth(width)
            .padding(vertical = 3.dp)
            .height(height),
        visualTransformation = visualTransformation
    )
}

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 3.dp,
    trackColor: Color = MaterialTheme.colorScheme.background,
    indicatorColor: Color = MaterialTheme.colorScheme.onPrimary,
    minHeightFraction: Float = 0.1f,
    trackHeightFraction: Float = 0.8f
): Modifier {
    val alpha = 1f
    return drawWithContent {
        drawContent()
        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return@drawWithContent
        val totalItems = layoutInfo.totalItemsCount
        val containerHeight = size.height
        val trackHeight = containerHeight * trackHeightFraction
        val trackTopOffset = (containerHeight - trackHeight) / 2f
        val visibleFraction = visibleItems.size / totalItems.toFloat()
        val indicatorHeight = maxOf(trackHeight * visibleFraction, trackHeight * minHeightFraction)
        val totalScrollableItems = totalItems - visibleItems.size
        val scrollProgress = state.firstVisibleItemIndex / totalScrollableItems.toFloat().coerceAtLeast(1f)
        val indicatorOffsetY = trackTopOffset + (trackHeight - indicatorHeight) * scrollProgress
        drawRect(
            color = trackColor,
            topLeft = Offset(
                x = size.width - width.toPx(),
                y = trackTopOffset
            ),
            size = Size(width.toPx(), trackHeight),
            alpha = alpha
        )
        drawRect(
            color = indicatorColor,
            topLeft = Offset(
                x = size.width - width.toPx(),
                y = indicatorOffsetY
            ),
            size = Size(width.toPx(), indicatorHeight),
            alpha = alpha
        )
    }
}
data class DropdownItem(val id: Int, val text: String, val sabId: Int? = null)
@Composable
fun SelectableDropdown(
    allItems: List<DropdownItem>,
    selectedItems: List<DropdownItem>,
    onItemSelected: (DropdownItem) -> Unit,
    onItemRemoved: (DropdownItem) -> Unit,
    maxDropdownHeight: Dp = 150.dp,
    text: String
) {
    var expanded by remember { mutableStateOf(false) }
    val availableItems = allItems.filterNot { selectedItems.contains(it) }
    Column(modifier = Modifier
        ) {
        Column( modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.secondary)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 25.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
            ) {
                DropdownHeader(
                    expanded = expanded,
                    text = text,
                    onExpandToggle = { expanded = !expanded }
                )
                if (expanded) {
                    HorizontalDivider(
                        modifier = Modifier.padding(end = 30.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.background // Светло-синий как на картинке
                    )
                }
            }
            if (expanded) {
                DropdownList(
                    items = availableItems,
                    maxHeight = maxDropdownHeight,
                    onItemClick = {
                        onItemSelected(it)
                    }
                )
            }
        }
        SelectedItemsRow(
            selectedItems = selectedItems,
            onItemRemoved = onItemRemoved
        )
    }
}

@Composable
fun SingleSelectableDropdown(
    items: List<DropdownItem>,
    selectedItem: DropdownItem?,
    onItemSelected: (DropdownItem) -> Unit,
    maxDropdownHeight: Dp = 150.dp,
    placeholderText: String = "Выберите элемент"
) {
    var expanded by remember { mutableStateOf(false) }
    val allItemsWithNone = listOf(DropdownItem(
        id = 0, text ="Нет"
    )) + items
    Column(modifier = Modifier
        .clip(RoundedCornerShape(25.dp))
        .background(MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 25.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
        ) {
            DropdownHeaderSingle(
                expanded = expanded,
                text = selectedItem?.text ?: placeholderText,
                color = if (selectedItem != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onPrimary,
                onExpandToggle = { expanded = !expanded }
            )
            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(end = 30.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.background // Светло-синий как на картинке
                )
            }
        }
        if (expanded) {
            val scrollState = rememberLazyListState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDropdownHeight)
                    .padding(start = 15.dp, end = 20.dp)
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .simpleVerticalScrollbar(scrollState)
                ) {
                    items(allItemsWithNone) { item ->
                        DropdownMenuItem(
                            text = { Text(text = item.text,
                                style = TextStyle(
                                    fontSize = 14.sp,      // Размер шрифта
                                )
                            ) },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownHeaderSingle(
    expanded: Boolean,
    text: String,
    color: Color,
    onExpandToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = color,
                style = TextStyle(
                    fontSize = 14.sp,
                )
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Раскрыть/Свернуть",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onExpandToggle() },
                tint = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun DropdownHeader(
    expanded: Boolean,
    text: String,
    onExpandToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimary,
                style = TextStyle(
                    fontSize = 14.sp,
                )
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Раскрыть/Свернуть",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onExpandToggle() },
                tint = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun DropdownList(
    items: List<DropdownItem>,
    maxHeight: Dp,
    onItemClick: (DropdownItem) -> Unit
) {
    val scrollState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .padding(start = 15.dp, end = 20.dp)
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .simpleVerticalScrollbar(scrollState)
        ) {
            items(items) { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = TextStyle(
                                fontSize = 14.sp,
                            )
                        )
                    },
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedItemsRow(
    selectedItems: List<DropdownItem>,
    onItemRemoved: (DropdownItem) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(top = 10.dp)
    ) {
        selectedItems.forEach { item ->
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(50))
                            .fillMaxWidth(0.9f)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = item.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = TextStyle(
                                fontSize = 14.sp,
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onItemRemoved(item) }
                            .fillMaxWidth(0.1f),
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleDropdown(
    items: List<DropdownItem>,
    onItemSelected: (DropdownItem) -> Unit,
    headerText: String,
    maxDropdownHeight: Dp = 150.dp
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(start = 25.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = headerText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = TextStyle(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Раскрыть/Свернуть",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.background
                )
            }
            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(end = 30.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.background
                )
            }
        }

        if (expanded) {
            val scrollState = rememberLazyListState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDropdownHeight)
                    .padding(start = 15.dp, end = 20.dp)
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .simpleVerticalScrollbar(scrollState)
                ) {
                    items(items) { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item.text,
                                    style = TextStyle(fontSize = 14.sp)
                                )
                            },
                            onClick = {
                                onItemSelected(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateInputFields(
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit
) {
    val context = LocalContext.current
    val fieldShape = RoundedCornerShape(50.dp)
    val fieldColor = MaterialTheme.colorScheme.secondary
    val placeholderColor = MaterialTheme.colorScheme.onPrimary
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        @SuppressLint("DefaultLocale")
        @Composable
        fun DateField(label: String, value: String, onDateChange: (String) -> Unit) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(label)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(fieldShape)
                        .background(fieldColor)
                        .clickable {
                            val calendar = Calendar.getInstance()
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    val formattedDate = String.format(
                                        "%02d.%02d.%04d",
                                        selectedDay,
                                        selectedMonth + 1,
                                        selectedYear
                                    )
                                    onDateChange(formattedDate)
                                },
                                year,
                                month,
                                day
                            ).show()
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (value.isNotEmpty()) value else "дд.мм.гггг",
                        color = if (value.isNotEmpty())
                            MaterialTheme.colorScheme.onBackground
                        else placeholderColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        DateField("Дата начала", startDate, onStartDateChange)
        DateField("Срок сдачи", endDate, onEndDateChange)
    }
}

@Composable
fun TeamMemberItem(
    nickname: String,
    name: String?,
    lastname: String?,
    onClick: () -> Unit
) {
    val fullNameWithNickname = buildString {
        if (!name.isNullOrBlank()) append(name).append(" ")
        if (!lastname.isNullOrBlank()) append(lastname)
        if (isNotEmpty()) append(", ")
        append(nickname)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(50))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = fullNameWithNickname,
            style = TextStyle(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
