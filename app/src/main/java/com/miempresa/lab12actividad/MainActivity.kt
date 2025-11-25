package com.miempresa.lab12actividad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.miempresa.lab12actividad.navigation.NavigationGraph
import com.miempresa.lab12actividad.ui.theme.Lab12actividadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab12actividadTheme {
                val navController = rememberNavController()
                NavigationGraph(navController = navController)
            }
        }
    }
}