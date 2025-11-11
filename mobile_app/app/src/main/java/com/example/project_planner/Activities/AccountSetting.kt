package com.example.project_planner.Activities

import CustomTextField
import DropdownItem
import SectionLabel
import SelectableDropdown
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.project_planner.R
import com.example.project_planner.RetrofitClient.apiService
import com.example.project_planner.Role
import com.example.project_planner.Technology
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

@SuppressLint("NewApi")
@Composable
fun LoadImageFromUri(uri: Uri?) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uri) {
        uri?.let {
            val source = ImageDecoder.createSource(context.contentResolver, it)
            bitmap = ImageDecoder.decodeBitmap(source)
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Selected Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}


suspend fun downloadImageToCache(context: Context, responseBody: ResponseBody): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val file = File.createTempFile("profile_image_", ".jpg", context.cacheDir)
            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",  // Убедись, что provider настроен в manifest
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun LoadProfilePhotoFromServer(
    userId: Int,
    onLoaded: (Uri?) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(userId) {
        try {
            val response = apiService.getUserPhoto(userId)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val uri = downloadImageToCache(context, body)
                    onLoaded(uri)
                }
            } else {
                // Обработка ошибки
                onLoaded(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onLoaded(null)
        }
    }
}


@Composable
fun ProfilePhotoEditor(
    currentImageUri: Uri? = null,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    userId: Int
) {
    var selectedImageUri by remember { mutableStateOf(currentImageUri) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            onImageSelected(it)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadProfilePhotoFromServer(
            userId = userId,
        ) { uri ->
            selectedImageUri = uri
            onImageSelected(uri ?: return@LoadProfilePhotoFromServer)
        }
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
                    tint =  MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            SectionLabel("Настройки")
            IconButton(onClick = onConfirmClick) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint =  MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                .background(Color.Transparent)
        ) {
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
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text(
                text = "Изменить фото профиля",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AccountEdit(
    userId: Int,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit = {},
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var worker_name by remember { mutableStateOf<String?>("") }
    var worker_lastname by remember { mutableStateOf<String?>("") }
    var worker_patronymic by remember { mutableStateOf<String?>("") }
    var worker_nickname by remember { mutableStateOf("") }
    var worker_email by remember { mutableStateOf("") }
    var worker_role by remember { mutableStateOf<List<Role>?>(emptyList()) }
    var worker_technology by remember { mutableStateOf<List<Technology>?>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRoles by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }
    var selectedTechnologies by remember {
        mutableStateOf<Map<Int, List<DropdownItem>>>(emptyMap())
    }
    var dropdown_roles by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }
    var dropdown_technologies by remember { mutableStateOf<List<DropdownItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUser(userId)
            val user = response
            worker_name = user.worker_name
            worker_lastname = user.worker_lastname
            worker_patronymic = user.worker_patronymic
            worker_nickname = user.worker_nickname
            worker_email = user.worker_email
            worker_role = user.roles
            worker_technology = user.technologies
            user.roles?.let {
                selectedRoles = it.map { role ->
                    DropdownItem(role.role_id, role.role_name)
                }
            }
            user.technologies?.let {
                selectedTechnologies = it
                    .map { technology ->
                        DropdownItem(
                            id = technology.technology_id,
                            text = technology.technology_name,
                            sabId = technology.role_id
                        )
                    }
                    .groupBy { it.sabId!! } // группируем по role_id
            }
            val response2 = apiService.getRoles()
            response2?.let {
                dropdown_roles = it.map { role ->
                    DropdownItem(role.role_id, role.role_name)
                }
            }
            val response3 = apiService.getTechnologies()
            response3?.let {
                dropdown_technologies = it.map { technology ->
                    DropdownItem(technology.technology_id, technology.technology_name, technology.role_id)
                }
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    if (!isLoading) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                .imePadding()
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
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current
                    val contentResolver = context.contentResolver
                    ProfilePhotoEditor(
                        onImageSelected = {
                            selectedImageUri = it
                        },
                        onBackClick = onClose,
                        onConfirmClick = {
                            coroutineScope.launch {
                                try {
                                    val rolesJson = selectedRoles.map { Role(it.id, it.text) }
                                    val technologiesJson = selectedTechnologies
                                        .flatMap { (roleId, items) ->
                                            items.map { item ->
                                                Technology(
                                                    technology_id = item.id,
                                                    technology_name = item.text,
                                                    role_id = roleId
                                                )
                                            }
                                        }
                                    val rolesRequestBody =
                                        rolesJson.toJsonRequestBody("application/json; charset=utf-8".toMediaType())
                                    val techsRequestBody =
                                        technologiesJson.toJsonRequestBody("application/json; charset=utf-8".toMediaType())
                                    val nameBody = worker_name.orEmpty()
                                        .toRequestBody("text/plain".toMediaType())
                                    val lastNameBody = worker_lastname.orEmpty()
                                        .toRequestBody("text/plain".toMediaType())
                                    val patronymicBody = worker_patronymic.orEmpty()
                                        .toRequestBody("text/plain".toMediaType())
                                    val nicknameBody =
                                        worker_nickname.toRequestBody("text/plain".toMediaType())
                                    val emailBody =
                                        worker_email.toRequestBody("text/plain".toMediaType())
                                    val imagePart = selectedImageUri?.let {
                                        val inputStream = contentResolver.openInputStream(it)
                                        val tempFile = File.createTempFile("upload", ".jpg")
                                        inputStream?.use { input ->
                                            tempFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        val requestFile =
                                            tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                                        MultipartBody.Part.createFormData(
                                            "photo",
                                            tempFile.name,
                                            requestFile
                                        )
                                    }
                                    val result = apiService.updateWorker(
                                        worker_id = userId,
                                        nickname = nicknameBody,
                                        name = nameBody,
                                        lastname = lastNameBody,
                                        patronymic = patronymicBody,
                                        email = emailBody,
                                        roles = rolesRequestBody,
                                        technologies = techsRequestBody,
                                        photo = imagePart
                                    )
                                    onClose()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Ошибка при обновлении: ${e.message}")
                                }
                            }
                        },
                        userId = userId
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Имя")
                        CustomTextField(
                            value = worker_name ?: "",
                            onValueChange = { if (it.length <= 120) worker_name = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Фамилия")
                        CustomTextField(
                            value = worker_lastname ?: "",
                            onValueChange = { if (it.length <= 120) worker_lastname = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Отчество")
                        CustomTextField(
                            value = worker_patronymic ?: "",
                            onValueChange = { if (it.length <= 120) worker_patronymic = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Имя пользователя")
                        CustomTextField(
                            value = worker_nickname,
                            onValueChange = { if (it.length <= 120) worker_nickname = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Адрес почты")
                        CustomTextField(
                            value = worker_email,
                            onValueChange = { if (it.length <= 120) worker_nickname = it },
                            placeholderText = "Не более 120 символов",
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Специальность")
                        SelectableDropdown(
                            allItems = dropdown_roles,
                            selectedItems = selectedRoles,
                            onItemSelected = { item ->
                                if (!selectedRoles.contains(item)) {
                                    selectedRoles = selectedRoles + item
                                }
                            },
                            onItemRemoved = { item ->
                                selectedRoles = selectedRoles - item
                            },
                            maxDropdownHeight = 140.dp,
                            text = "Выберите специальность"
                        )
                    }
                    selectedRoles.forEach { role ->
                        val techForRole = dropdown_technologies.filter { it.sabId == role.id }
                        val selectedForRole = selectedTechnologies[role.id] ?: emptyList()
                        SelectableDropdown(
                            allItems = techForRole,
                            selectedItems = selectedForRole,
                            onItemSelected = { tech ->
                                selectedTechnologies =
                                    selectedTechnologies + (role.id to (selectedForRole + tech))
                            },
                            onItemRemoved = { tech ->
                                selectedTechnologies =
                                    selectedTechnologies + (role.id to (selectedForRole - tech))
                            },
                            maxDropdownHeight = 140.dp,
                            text = "Технологии для ${role.text}"
                        )
                    }
                }
            }
        }
    }
}

inline fun <reified T> T.toJsonRequestBody(contentType: MediaType): RequestBody {
    val json = Gson().toJson(this)
    return json.toRequestBody(contentType)
}