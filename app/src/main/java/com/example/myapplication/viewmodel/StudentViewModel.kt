package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.StudentDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StudentDatabase(application)
    private val _students = MutableStateFlow<List<String>>(emptyList())
    val students: StateFlow<List<String>> = _students

    fun loadStudents() {
        viewModelScope.launch {
            _students.value = emptyList() // Limpiar la lista actual
            
            // Cargar datos de SQLite en un hilo de fondo
            val studentList = withContext(Dispatchers.IO) {
                database.getAllStudents()
            }
            
            _students.value = studentList
        }
    }

    fun addStudent(name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (database.addStudent(name)) {
                    // Si se agregó exitosamente, recargar la lista
                    val updatedList = database.getAllStudents()
                    _students.value = updatedList
                }
            }
        }
    }
} 