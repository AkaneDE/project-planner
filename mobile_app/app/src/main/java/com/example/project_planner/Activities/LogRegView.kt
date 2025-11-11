package com.example.project_planner.Activities

import CustomButton
import CustomTextField
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.project_planner.AuthViewModel
import com.example.project_planner.R
import com.example.project_planner.RetrofitClient
import com.example.project_planner.login
import com.example.project_planner.registration
import com.example.project_planner.userLogin
import com.example.project_planner.userRegistration
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainContent() {
    val viewModel: AuthViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val isAuthorized = viewModel.userId != -1
    val navController = rememberNavController()
    val selectedTab = remember { mutableStateOf(0) }
    val showBottomBar = remember { mutableStateOf(isAuthorized) }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            if (isAuthorized && showBottomBar.value) {
                BottomNavigationBar(
                    selectedItem = selectedTab.value,
                    onItemSelected = { tabIndex ->
                        selectedTab.value = tabIndex
                        when (tabIndex) {
                            0 -> navController.navigate("project_list")
                            1 -> navController.navigate("create_project")
                            2 -> navController.navigate("profile")
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isAuthorized) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    AuthChoiceScreen(snackbarHostState, viewModel)
                }
            } else {
                AppNavigation(
                    userId = viewModel.userId,
                    selectedTab = selectedTab,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    showBottomBar = showBottomBar,
                    innerPadding = Modifier.padding(0.dp),
                    apiService = RetrofitClient.apiService
                )
            }
        }
    }
}

@Composable
fun AuthChoiceScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: AuthViewModel
) {
    val username = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    var showSheet = remember { mutableStateOf(false) }
    var fields = remember{ mutableStateOf(listOf("имя пользователя" to username, "почта" to email, "пароль" to password))}
    var title = remember { mutableStateOf("Войти в аккаунт") }
    var textButton = remember { mutableStateOf("Войти") }
    val coroutineScope = rememberCoroutineScope()
    var onLoginClick = remember { mutableStateOf<() -> Unit>({}) }
    val isDarkTheme = isSystemInDarkTheme()
    val paint = if (isDarkTheme) {
        painterResource(id = R.drawable.mreg)
    } else {
        painterResource(id = R.drawable.mlog)
    }
    val apiService = RetrofitClient.apiService
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = paint,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(1f)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomButton(
                text = "Войти в аккаунт",
                backgroundColor = MaterialTheme.colorScheme.onPrimary,
                textColor = MaterialTheme.colorScheme.primary,
                widthFraction = 0.6f
            ) {
                showSheet.value = true
                fields.value = listOf("почта" to email, "пароль" to password)
                title.value = "Вход в аккаунт"
                textButton.value = "Войти"
                onLoginClick.value = {
                    coroutineScope.launch {
                        val user = userLogin(
                            email.value,
                            password.value)
                        val result = login(apiService, user)
                        result.onSuccess { userResponse ->
                            showSheet.value = false
                            viewModel.updateUserId(userResponse.worker_id)
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar(
                                message = error.message ?: "Ошибка при входе",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            CustomButton(
                text = "Создать аккаунт",
                backgroundColor = MaterialTheme.colorScheme.onPrimary,
                textColor = MaterialTheme.colorScheme.primary,
                widthFraction = 0.6f
            ) {
                showSheet.value = true
                fields.value =
                    listOf("имя пользователя" to username, "почта" to email, "пароль" to password)
                title.value = "Создание аккаунта"
                textButton.value = "Создать"
                onLoginClick.value = {
                    coroutineScope.launch {
                        val user =
                            userRegistration(username.value, password.value, email.value)
                        val result = registration(apiService, user)
                        result.onSuccess { userResponse ->
                            showSheet.value = false
                            viewModel.updateUserId(userResponse.worker_id)
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar(
                                message = error.message ?: "Ошибка при добавлении пользователя",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }
    AuthBottomSheet(
        isVisible = showSheet.value,
        onDismiss = { showSheet.value = false },
        title = title.value,
        textButton = textButton.value,
        fields = fields.value,
        onButtonClick = onLoginClick.value,
        snackbarHostState
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    textButton: String,
    fields: List<Pair<String, MutableState<String>>>,
    onButtonClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    if (isVisible) {
        LaunchedEffect(Unit) {
            coroutineScope.launch { sheetState.show() }
        }
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .fillMaxHeight(0.9f),
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    fields.forEach { (placeholder, state) ->
                        CustomTextField(
                            value = state.value,
                            onValueChange = { state.value = it },
                            placeholderText = placeholder,
                            colorsScheme = true,
                            width = 0.9f,
                            visualTransformation = if (placeholder.equals("пароль", ignoreCase = true)) {
                                PasswordVisualTransformation()
                            } else {
                                VisualTransformation.None
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomButton(
                        text = textButton,
                        backgroundColor = MaterialTheme.colorScheme.onPrimary,
                        textColor = MaterialTheme.colorScheme.primary,
                        widthFraction = 0.4f
                    ) {
                        onButtonClick()
                    }
                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}


