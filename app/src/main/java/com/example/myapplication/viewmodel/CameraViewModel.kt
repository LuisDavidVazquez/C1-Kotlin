package com.example.myapplication.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    var photoUri by mutableStateOf<Uri?>(null)
        private set

    fun updatePhotoUri(uri: Uri?) {
        photoUri = uri
    }
} 