package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {
    private val jobs = mutableListOf<Job>()
    
    protected fun launchSafely(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend () -> Unit
    ): Job {
        return viewModelScope.launch(dispatcher + SupervisorJob()) {
            try {
                block()
            } catch (ce: CancellationException) {
                throw ce // Siempre re-lanzar CancellationException
            } catch (e: Exception) {
                // Manejar la excepción según sea necesario
                handleError(e)
            }
        }.also { jobs.add(it) }
    }

    protected open fun handleError(error: Exception) {
        // Implementar manejo de errores específico si es necesario
    }

    override fun onCleared() {
        super.onCleared()
        // Cancelar todos los jobs pendientes
        jobs.forEach { it.cancel() }
        jobs.clear()
        // Limpiar recursos
        clearResources()
    }

    protected open fun clearResources() {
        // Sobrescribir en las subclases para limpiar recursos específicos
    }
} 