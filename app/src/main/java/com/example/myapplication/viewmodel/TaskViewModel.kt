package com.example.myapplication.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.Task
import com.example.myapplication.network.TaskRequest
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
        
    var newTaskTitle by mutableStateOf("")
        private set
        
    var newTaskDescription by mutableStateOf("")
        private set
        
    var isLoading by mutableStateOf(false)  
        private set
        
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadTasks()
    }

    fun updateNewTaskTitle(title: String) {
        newTaskTitle = title
    }

    fun updateNewTaskDescription(description: String) {
        newTaskDescription = description
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.apiService.getTasks()
                if (response.isSuccessful) {
                    response.body()?.let { taskResponse ->
                        tasks = taskResponse.tasks
                    }
                } else {
                    errorMessage = "Error al cargar las tareas"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun createTask(onSuccess: () -> Unit) {
        if (newTaskTitle.isBlank() || newTaskDescription.isBlank()) {
            errorMessage = "Todos los campos son requeridos"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                val taskRequest = TaskRequest(
                    title = newTaskTitle.trim(),
                    description = newTaskDescription.trim()
                )
                
                val response = RetrofitClient.apiService.createTask(taskRequest)
                if (response.isSuccessful) {
                    response.body()?.task?.let { newTask ->
                        tasks = tasks + newTask
                        newTaskTitle = ""
                        newTaskDescription = ""
                        onSuccess()
                    }
                } else {
                    errorMessage = "Error al crear la tarea"
                }
            } catch (e: Exception) {
                Log.e("CreateTask", "Error: ${e.message}", e)
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteTask(taskId)
                if (response.isSuccessful) {
                    tasks = tasks.filter { it.id != taskId }
                } else {
                    errorMessage = "Error al eliminar la tarea"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            }
        }
    }

    fun updateTask(taskId: Int, title: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.apiService.updateTask(
                    taskId = taskId,
                    task = TaskRequest(title.trim(), description.trim())
                )
                
                if (response.isSuccessful) {
                    response.body()?.task?.let { updatedTask ->
                        tasks = tasks.map { if (it.id == taskId) updatedTask else it }
                        onSuccess()
                    }
                } else {
                    errorMessage = "Error al actualizar la tarea"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
} 