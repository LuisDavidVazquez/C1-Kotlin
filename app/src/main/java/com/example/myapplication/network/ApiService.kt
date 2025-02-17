package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.*

// Data classes para la API
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String
)

data class Task(
    val id: Int,
    val title: String,
    val description: String
)

data class TaskRequest(
    val title: String,
    val description: String
)

data class TaskResponse(
    val tasks: List<Task>
)

data class CreateTaskResponse(
    val success: Boolean,
    val task: Task?
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @GET("tasks")
    suspend fun getTasks(): Response<TaskResponse>
    
    @POST("tasks")
    suspend fun createTask(@Body task: TaskRequest): Response<CreateTaskResponse>
    
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: Int): Response<Unit>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: Int,
        @Body task: TaskRequest
    ): Response<CreateTaskResponse>
}
