package com.example.project_planner

import okhttp3.MediaType.Companion.toMediaType
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.io.File
import java.io.FileOutputStream

// Определяем интерфейс для взаимодействия с сервером
interface ApiService {
    @POST("login")
    suspend fun login(@Body user: userLogin): UserResponse

    @POST("registration")
    suspend fun registration(@Body user: userRegistration): UserResponse

    @GET("/files/{filename}")
    suspend fun downloadFile(@Path("filename") filename: String): Response<ResponseBody>

    @GET("projects/{worker_id}")
    suspend fun getProjects( @Path("worker_id") worker_id: Int ): ProjectsResponse

    @GET("/projects/assigned/{worker_id}")
    suspend fun getProjectsAssigned( @Path("worker_id") worker_id: Int ): ProjectsResponse

    @POST("project")
    suspend fun addProject(@Body project: ProjectCreate): Project

    @GET("roles")
    suspend fun getRoles(): MutableList<Role>?

    @GET("technologies")
    suspend fun getTechnologies(): MutableList<Technology>?

    @GET("/worker/by-nickname/{nickname}")
    suspend fun getWorker(@Path("nickname") nickname: String): WorkerSearch

    @POST("friends")
    suspend fun addFriend(
        @Body request: FriendRequest
    ): Response<Unit>

    @GET("/workers/by-user/{userId}")
    suspend fun getFriends(@Path("userId") userId: Int): MutableList<WorkerSearch>?

    @GET("/categories-and-friends/{userId}")
    suspend fun getCategoriesFriends(@Path("userId") userId: Int): Project_catecory_friends

    @GET("/project/{projectId}/workers")
    suspend fun getTeam( @Path("projectId") projectId: Int): MutableList<WorkerSearch>?

    @POST("task")
    suspend fun addTask(
        @Body task: TaskDetailsIn,
        @Query("project_id") projectId: Int
    ): TaskResponse

    @GET("worker/{workerId}")
    suspend fun getUser( @Path("workerId") workerId: Int): WorkerProfileResponse

    @Multipart
    @PUT("/worker/{id}")
    suspend fun updateWorker(
        @Path("id") worker_id: Int,
        @Part("nickname") nickname: RequestBody,
        @Part("name") name: RequestBody,
        @Part("lastname") lastname: RequestBody,
        @Part("patronymic") patronymic: RequestBody,
        @Part("email") email: RequestBody,
        @Part("roles") roles: RequestBody,
        @Part("technologies") technologies: RequestBody,
        @Part photo: MultipartBody.Part? = null
    ): Response<Unit>

    @GET("/worker/{id}/photo")
    @Streaming
    suspend fun getUserPhoto(@Path("id") userId: Int): Response<ResponseBody>

    @GET("task/{taskId}")
    suspend fun getTask( @Path("taskId") taskId: Int): TaskDetailsOut

    @GET("notifications/{worker_id}")
    suspend fun getNotifications( @Path("worker_id") worker_id: Int): MutableList<NotificationItem>?

    @PUT("notifications/viewed")
    suspend fun markNotificationsAsViewed(
        @Body notificationIds: ViewedNotificationsRequest
    ): Response<Unit>

    @Multipart
    @POST("/content-task/{worker_id}/{task_id}")
    suspend fun uploadTaskContent(
        @Path("worker_id") workerId: Int,
        @Path("task_id") taskId: Int,
        @Part("title") title: RequestBody,
        @Part("text") text: RequestBody,
        @Part file: MultipartBody.Part
    ): ContentTaskResponse

    @Multipart
    @POST("/content-project/{worker_id}/{project_id}")
    suspend fun uploadProjectContent(
        @Path("worker_id") workerId: Int,
        @Path("project_id") projectId: Int,
        @Part("title") title: RequestBody,
        @Part("text") text: RequestBody,
        @Part file: MultipartBody.Part
    ): ContentTaskResponse

    @DELETE("friends/{userId}/{workerId}")
    suspend fun deleteFriend(
        @Path("userId") userId: Int,
        @Path("workerId") workerId: Int
    ): Response<Unit>

    @GET("/tasksnotinp/{taskId}")
    suspend fun getUserTask(
        @Path("taskId") taskId: Int
    ): UserTaskResponse

    @GET("project/{projectId}")
    suspend fun getProject( @Path("projectId") projectId: Int): ProjectOut

    @POST("category")
    suspend fun addCategory(@Body addCategoryRequest: AddCategoryRequest): Category

    @PUT("project/{projectId}")
    suspend fun updateProject(
        @Path("projectId") projectId: Int,
        @Body updatedProject: ProjectCreate
    ): Project2

    @PUT("project/{projectId}/toggle-status")
    suspend fun projectComplete(
        @Path("projectId") projectId: Int): ContentTaskResponse

    @DELETE("project/{projectId}")
    suspend fun deleteProject(@Path("projectId") projectId: Int)

    @DELETE("task/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: Int)

    @PUT("/notifications/accept")
    suspend fun updateNotificationAccepted(
        @Body request: NotificationAcceptRequest
    ): Response<Unit>

    @PUT("task/{id}")
    suspend fun updateTask(
        @Path("id") taskId: Int,
        @Body task: TaskDetailsIn
    ):TaskResponse

    @PUT("task/{taskId}/toggle-status")
    suspend fun taskComplete(
        @Path("taskId") taskId: Int): TaskResponse
}


suspend fun login(apiService: ApiService, user: userLogin): Result<UserResponse> {
    return try {
        val userResponse = apiService.login(user)
        Result.success(userResponse)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun registration(apiService: ApiService, user: userRegistration): Result<UserResponse> {
    return try {
        val newUser = apiService.registration(user)
        Result.success(newUser)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun addProject(apiService: ApiService, project: ProjectCreate): Result<Project> {
    return try {
        // Выполняем запрос к API
        val newProject = apiService.addProject(project)
        Result.success(newProject)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun findFriend(apiService: ApiService, nickname: String): Result<WorkerSearch> {
    return try {
        val user = apiService.getWorker(nickname)
        Result.success(user)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun addFriend(apiService: ApiService, userId: Int, workerId: Int): Result<Unit> {
    return try {
        val request = FriendRequest(user_id = userId, worker_id = workerId)
        apiService.addFriend(request)
        Result.success(Unit)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

fun handleApiError(e: Exception): Result<Nothing> {
    return if (e is retrofit2.HttpException) {
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            JSONObject(errorBody ?: "").optString("error", "Неизвестная ошибка")
        } catch (parseException: Exception) {
            "Неизвестная ошибка2"
        }
        Result.failure(Exception(errorMessage))
    } else {
        Result.failure(e)
    }
}

fun createPartFromString(value: String): RequestBody {
    return RequestBody.create("text/plain".toMediaType(), value)
}

fun createFilePart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
    return try {
        // Получаем имя файла из URI
        val fileName = getFileName(context, uri)

        // Копируем содержимое URI во временный файл
        val tempFile = File(context.cacheDir, fileName)
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)

        // Закрываем потоки
        inputStream?.close()
        outputStream.close()

        // Создаем MultipartBody.Part
        val requestFile = RequestBody.create(
            "application/octet-stream".toMediaTypeOrNull(),
            tempFile
        )
        MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var fileName = "temp_file"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            fileName = cursor.getString(nameIndex)
        }
    }
    return fileName
}

suspend fun addCategory(apiService: ApiService, categoryName: String, user_id: Int): Result<Category> {
    return try {
        val newCategory = apiService.addCategory(AddCategoryRequest(category_name = categoryName, user_id))
        Result.success(newCategory)
    } catch (e: Exception) {
        handleApiError(e)

    }
}

suspend fun updateProject(apiService: ApiService, projectId: Int, updatedProject: ProjectCreate): Result<Project2> {
    return try {
        // Выполняем запрос к API для обновления проекта
        val updatedProjectResponse = apiService.updateProject(projectId, updatedProject)
        Result.success(updatedProjectResponse)
    } catch (e: Exception) {
        handleApiError(e)

    }
}

suspend fun deleteProject(apiService: ApiService, projectId: Int): Result<Unit> {
    return try {
        // Выполняем запрос к API для удаления проекта
        apiService.deleteProject(projectId)
        Result.success(Unit) // Успешно возвращаем пустой результат
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun deleteTask(apiService: ApiService, taskId: Int): Result<Unit> {
    return try {
        // Выполняем запрос к API для удаления проекта
        apiService.deleteTask(taskId)
        Result.success(Unit) // Успешно возвращаем пустой результат
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun completeProject(apiService: ApiService, projectId: Int): Result<ContentTaskResponse> {
    return try {
        // Выполняем запрос к API
        val updatedProject = apiService.projectComplete(projectId)
        Result.success(updatedProject)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun completeTask(apiService: ApiService, taskId: Int): Result<TaskResponse> {
    return try {
        val updatedTask = apiService.taskComplete(taskId)
        Result.success(updatedTask)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun addTask(apiService: ApiService, task: TaskDetailsIn, projectId: Int): Result<TaskResponse> {
    return try {
        val response = apiService.addTask(task, projectId)
        Result.success(response)
    } catch (e: Exception) {
        handleApiError(e)
    }
}

suspend fun updateTask(apiService: ApiService, taskId: Int, task: TaskDetailsIn): Result<TaskResponse> {
    return try {
        val response = apiService.updateTask(taskId, task)
        Result.success(response)
    } catch (e: Exception) {
        handleApiError(e)
    }
}


class ContentTaskResponse {}

data class NotificationResponseRequest(
    val is_accepted: Boolean
)

data class ApiResponse(
    val message: String
)

data class UserTaskResponse(
    val project_name: String,
    val project_description: String,
    val leader_first_name: String,
    val leader_last_name: String,
    val task_id: Int,
    val project_id: Int,
    val task_name: String,
    val task_description: String,
    val task_deadline: String, // Формат даты может быть уточнен
    val task_status: String,
    val chat_id: Int?,
    val chat_name: String?
)

data class AddCategoryRequest(
    val category_name: String,
    val user_id: Int
)

data class Project2(
    val project_id: Int,
    val project_name: String,
    val project_description: String?,
    val project_status: Boolean,
    val project_date_start: String,
    val project_date_end: String,
    val leader_first_name: String?,
    val leader_last_name: String?,
    val category_name: String?
)

data class TaskResponse(val taskId: Int)

data class WorkerSearch(
    val worker_id: Int,
    val worker_nickname: String,
    val worker_name: String,
    val worker_lastname: String,
    val worker_filepath: String
)

data class FriendRequest(
    val user_id: Int,
    val worker_id: Int
)

data class userRegistration(
    val worker_nickname: String,
    val worker_password: String,
    val worker_email: String
)

data class userLogin(
    val worker_email: String,
    val worker_password: String
)

data class UserResponse(
    val message: String,
    val worker_id: Int)

data class Project(
    val project_id: Int,
    val category_id: Int,
    val project_name: String,
    val project_deadline: String,
    val project_progress: String,
)

data class Category(
    val category_id: Int,
    val category_name: String,
    var isSelected: MutableState<Boolean> = mutableStateOf(false)
)

data class ProjectsResponse(
    var projects: MutableList<Project>,
    var categories: MutableList<Category>
)

data class ProjectCreate(
    val project_name: String,
    val project_description: String?,
    val project_daystart: String,
    val project_deadline: String,
    val worker_id: Int,
    val category_id: Int?,
    val team: List<Int>?
)

data class Project_catecory_friends(
    val categories: List<Category>?,
    val friends: List<WorkerSearch>?
)

data class Role(
    val role_id: Int,
    val role_name: String
)

data class Technology(
    val technology_id: Int,
    val technology_name: String,
    val role_id: Int
)

data class ProjectOut(
    val worker_id: Int? = null,
    val project_name: String,
    val project_description: String?,
    val project_daystart: String,
    val project_deadline: String,
    val project_status: Boolean,
    val category_name: String?,
    val tasks: List<TaskProject>?,
    val report_title: ReportFile?,
    val contents: List<ProjectContent>?,
    val workers: List<WorkerSearch>?
)

data class ReportFile(
    val report_title: String,
    val report_type: String
)

data class TaskProject(
    val task_id: Int,
    val task_name: String,
    val task_status: Boolean
)

data class ProjectContent(
    val content_id: Int,
    val worker: WorkerSearch,
    val content_name: String,
    val content_text: String?,
    val content_type: String,
    val content_filepath: String

)

data class TaskDetailsIn(
    val task_name: String,
    val task_description: String?,
    val task_daystart: String,
    val task_deadline: String,
    val team_members: List<Int>?,
)

data class TaskDetailsOut(
    val task_name: String,
    val task_description: String?,
    val task_daystart: String,
    val task_deadline: String,
    val team_members: List<WorkerSearch>?,
    var task_status: Boolean,
    val task_content: List<ProjectContent>?
)

data class WorkerProfileResponse(
    val worker_id: Int,
    val worker_nickname: String,
    val worker_name: String?,
    val worker_lastname: String?,
    val worker_patronymic: String?,
    val worker_email: String,
    val worker_filepath: String?,
    val roles: List<Role>?,
    val technologies: List<Technology>?,
    val active_projects_count: Int? = null,
    val finished_projects_count: Int? = null,
    val has_unread_notifications: Boolean = false
)

data class ViewedNotificationsRequest(
    val notificationIds: List<Int>
)

data class NotificationItem2(
    val notification_task_id: Int,
    val author: String,
    val notification_task_time: String,
    val notification_task_text: String,
    var viewed: MutableState<Boolean?> = mutableStateOf(null),
    var accepted: MutableState<Boolean?> = mutableStateOf(null)
)

data class NotificationItem(
    val notification_task_id: Int,
    val notification_task_text: String,
    val notification_task_time: String,
    val author: String,
    var viewed: Boolean? = null,
    var accepted: Boolean? = null
)

data class NotificationAcceptRequest(
    val notificationId: Int,
    val accepted: Boolean
)