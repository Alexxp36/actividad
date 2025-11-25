package com.miempresa.lab12actividad.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.miempresa.lab12actividad.screens.AddEditCursoScreen
import com.miempresa.lab12actividad.screens.CursosScreen
import com.miempresa.lab12actividad.screens.LoginScreen
import com.miempresa.lab12actividad.viewmodel.AuthState
import com.miempresa.lab12actividad.viewmodel.AuthViewModel
import com.miempresa.lab12actividad.viewmodel.CursosViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Cursos : Screen("cursos")
    object AddEditCurso : Screen("add_edit_curso/{cursoId}") {
        fun createRoute(cursoId: String?) = if (cursoId != null) {
            "add_edit_curso/$cursoId"
        } else {
            "add_edit_curso/null"
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    cursosViewModel: CursosViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Cursos.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Cursos.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Cursos.route) {
            CursosScreen(
                authViewModel = authViewModel,
                cursosViewModel = cursosViewModel,
                onNavigateToAddEdit = { cursoId ->
                    navController.navigate(Screen.AddEditCurso.createRoute(cursoId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Cursos.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AddEditCurso.route,
            arguments = listOf(
                navArgument("cursoId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getString("cursoId")
            AddEditCursoScreen(
                cursoId = if (cursoId == "null") null else cursoId,
                cursosViewModel = cursosViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
