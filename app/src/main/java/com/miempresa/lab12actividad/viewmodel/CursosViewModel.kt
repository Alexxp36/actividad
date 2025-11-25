package com.miempresa.lab12actividad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.miempresa.lab12actividad.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CursosViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var listenerRegistration: ListenerRegistration? = null
    private var currentUserId: String? = null

    init {
        loadCursos()
    }

    fun loadCursos() {
        val userId = auth.currentUser?.uid ?: return

        // Si el usuario cambió, cancelar el listener anterior
        if (currentUserId != userId) {
            listenerRegistration?.remove()
            currentUserId = userId
            _cursos.value = emptyList() // Limpiar cursos anteriores
        }

        _isLoading.value = true
        _error.value = null

        listenerRegistration = firestore.collection("cursos")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val cursosList = snapshot.documents.mapNotNull { doc ->
                        Curso(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            codigo = doc.getString("codigo") ?: "",
                            creditos = doc.getLong("creditos")?.toInt() ?: 0,
                            descripcion = doc.getString("descripcion") ?: "",
                            userId = doc.getString("userId") ?: ""
                        )
                    }
                    _cursos.value = cursosList
                    _isLoading.value = false
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun addCurso(nombre: String, codigo: String, creditos: Int, descripcion: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val curso = hashMapOf(
                    "nombre" to nombre,
                    "codigo" to codigo,
                    "creditos" to creditos,
                    "descripcion" to descripcion,
                    "userId" to userId
                )

                firestore.collection("cursos")
                    .add(curso)
                    .await()

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateCurso(cursoId: String, nombre: String, codigo: String, creditos: Int, descripcion: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val updates = hashMapOf(
                    "nombre" to nombre,
                    "codigo" to codigo,
                    "creditos" to creditos,
                    "descripcion" to descripcion
                )

                firestore.collection("cursos")
                    .document(cursoId)
                    .update(updates as Map<String, Any>)
                    .await()

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun deleteCurso(cursoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                firestore.collection("cursos")
                    .document(cursoId)
                    .delete()
                    .await()

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
