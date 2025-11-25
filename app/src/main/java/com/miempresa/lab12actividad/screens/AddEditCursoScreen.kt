package com.miempresa.lab12actividad.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.lab12actividad.viewmodel.CursosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCursoScreen(
    cursoId: String?,
    cursosViewModel: CursosViewModel,
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var creditos by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val cursos by cursosViewModel.cursos.collectAsState()
    val isLoading by cursosViewModel.isLoading.collectAsState()

    val isEditMode = cursoId != null

    LaunchedEffect(cursoId) {
        if (isEditMode) {
            val curso = cursos.find { it.id == cursoId }
            curso?.let {
                nombre = it.nombre
                codigo = it.codigo
                creditos = it.creditos.toString()
                descripcion = it.descripcion
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Editar Curso" else "Agregar Curso",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del curso") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código del curso") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = creditos,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        creditos = it
                    }
                },
                label = { Text("Créditos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(bottom = 24.dp),
                maxLines = 5
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)
                )
            }

            Button(
                onClick = {
                    val creditosInt = creditos.toIntOrNull() ?: 0
                    if (isEditMode && cursoId != null) {
                        cursosViewModel.updateCurso(
                            cursoId,
                            nombre,
                            codigo,
                            creditosInt,
                            descripcion
                        )
                    } else {
                        cursosViewModel.addCurso(
                            nombre,
                            codigo,
                            creditosInt,
                            descripcion
                        )
                    }
                    showSuccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = nombre.isNotBlank() &&
                         codigo.isNotBlank() &&
                         creditos.isNotBlank() &&
                         !isLoading
            ) {
                Text(
                    text = if (isEditMode) "Actualizar Curso" else "Guardar Curso",
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateBack()
            },
            title = { Text("Éxito") },
            text = {
                Text(
                    if (isEditMode) "Curso actualizado correctamente"
                    else "Curso agregado correctamente"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}
