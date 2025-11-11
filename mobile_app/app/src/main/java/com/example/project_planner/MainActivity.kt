package com.example.project_planner

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.project_planner.Activities.MainContent
import com.example.project_planner.ui.theme.Project_PlannerTheme



class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        enableEdgeToEdge()
        setContent {
            Project_PlannerTheme {
                MainContent()
            }
        }
    }
}

class AuthViewModel : ViewModel() {
    var userId by mutableStateOf(-1)
        private set
    fun updateUserId(id: Int) {
        userId = id
    }
}
