package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import android.util.Log


// Data classes para la API
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String
)

// Data classes para la API de tareas
data class Task(
    val id: Int,
    val title: String,
    val description: String
)

data class TaskRequest(
    val title: String,
    val description: String
)

// Modifica la data class para manejar la respuesta del servidor
data class TaskResponse(
    val tasks: List<Task>
)

// Asegúrate de que estas clases estén definidas correctamente
data class CreateTaskResponse(
    val success: Boolean,
    val task: Task?
)

// Interface para la API
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

// Singleton para Retrofit
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                
                if (isLoggedIn) {
                    TaskScreen()
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoginContent(onLoginSuccess)
            }
        }
    )
}

@Composable
fun LoginContent(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Login Image",
            modifier = Modifier.size(120.dp)
        )

        Text(
            text = "Bienvenido, Luis David",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Mostrar mensaje de error si existe
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val response = RetrofitClient.apiService.login(
                            LoginRequest(username, password)
                        )
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            errorMessage = "Credenciales incorrectas"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(text = "Iniciar sesión", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen() {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Modificar la carga inicial de tareas
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val response = RetrofitClient.apiService.getTasks()
            if (response.isSuccessful) {
                response.body()?.let { taskResponse ->
                    tasks = taskResponse.tasks  // Acceder a la lista a través de .tasks
                } ?: run {
                    tasks = emptyList()
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestor de Tareas") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                label = { Text("Título de la tarea") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Button(
                onClick = {
                    if (newTaskTitle.isNotBlank() && newTaskDescription.isNotBlank()) {
                        scope.launch {
                            try {
                                isLoading = true
                                val taskRequest = TaskRequest(
                                    title = newTaskTitle.trim(),
                                    description = newTaskDescription.trim()
                                )
                                
                                val response = RetrofitClient.apiService.createTask(taskRequest)
                                if (response.isSuccessful) {
                                    response.body()?.let { responseBody ->
                                        // Asumiendo que la respuesta contiene la tarea en task
                                        responseBody.task?.let { newTask ->
                                            tasks = tasks + newTask
                                            newTaskTitle = ""
                                            newTaskDescription = ""
                                            Toast.makeText(context, "Tarea creada con éxito", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Error al crear la tarea", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("CreateTask", "Error: ${e.message}", e)
                                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && newTaskTitle.isNotBlank() && newTaskDescription.isNotBlank()
            ) {
                Text(if (isLoading) "Creando..." else "Crear Tarea")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onDelete = { taskId ->
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.deleteTask(taskId)
                                        if (response.isSuccessful) {
                                            tasks = tasks.filter { it.id != taskId }
                                            Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                                        } else {
                                            errorMessage = "Error al eliminar la tarea"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error de conexión: ${e.message}"
                                    }
                                }
                            },
                            onUpdate = { updatedTask ->
                                tasks = tasks.map { if (it.id == updatedTask.id) updatedTask else it }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDelete: (Int) -> Unit,
    onUpdate: (Task) -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row {
                // Botón de editar
                IconButton(onClick = { showUpdateDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar tarea",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón de eliminar
                IconButton(onClick = { onDelete(task.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar tarea",
                        tint = Color.Red
                    )
                }
            }
        }
    }
    
    if (showUpdateDialog) {
        UpdateTaskDialog(
            task = task,
            onUpdate = { updatedTask ->
                onUpdate(updatedTask)
                showUpdateDialog = false
            },
            onDismiss = { showUpdateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTaskDialog(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualizar Tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        errorMessage = "Todos los campos son requeridos"
                        return@Button
                    }
                    
                    scope.launch {
                        try {
                            isLoading = true
                            val response = RetrofitClient.apiService.updateTask(
                                taskId = task.id,
                                task = TaskRequest(title.trim(), description.trim())
                            )
                            
                            if (response.isSuccessful) {
                                response.body()?.task?.let { updatedTask ->
                                    onUpdate(updatedTask)
                                    Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show()
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
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Actualizando..." else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}