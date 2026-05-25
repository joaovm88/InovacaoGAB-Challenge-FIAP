package br.com.fiap.inovacaogab.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun MainScreen(navController: NavController) {
    var userRole by remember { mutableStateOf("") }
    var abaAtual by remember { mutableStateOf("mural") }

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid


    LaunchedEffect(userId) {
        if (userId != null) {
            val database = FirebaseDatabase.getInstance("https://inovacaogab-b43c6-default-rtdb.firebaseio.com/")
            database.getReference("users").child(userId).child("role").get()
                .addOnSuccessListener { snapshot ->
                    val rawRole = snapshot.getValue(String::class.java) ?: "operador"

                    // Converte qualquer formato antigo para o padrão novo e exato da interface
                    userRole = when {
                        rawRole.contains("gestor", ignoreCase = true) -> "Gestor(a)"
                        rawRole.contains("lider", ignoreCase = true) || rawRole.contains("líder", ignoreCase = true) -> "Líder"
                        else -> "Operador(a)"
                    }
                }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                // Aba 1: Mural (Todos acessam)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    label = { Text("Mural") },
                    selected = abaAtual == "mural",
                    onClick = { abaAtual = "mural" }
                )

                // Aba 2: Ideias (Todos acessam)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    label = { Text("Ideias") },
                    selected = abaAtual == "ideias",
                    onClick = { abaAtual = "ideias" }
                )

                // ABA EXCLUSIVA OPERADOR(A): Feedbacks
                if (userRole == "Operador(a)") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                        label = { Text("Feedbacks") },
                        selected = abaAtual == "feedbacks",
                        onClick = { abaAtual = "feedbacks" }
                    )
                }

                // ABA PROJETOS: Visível para Gestor(a) e Líder
                if (userRole == "Gestor(a)" || userRole == "Líder") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                        label = { Text("Projetos") },
                        selected = abaAtual == "projetos",
                        onClick = { abaAtual = "projetos" }
                    )
                }

                // ABA VISÃO EXECUTIVA: Visível para Gestor(a) e Líder
                if (userRole == "Gestor(a)" || userRole == "Líder") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Visão Executiva") },
                        selected = abaAtual == "dashboard",
                        onClick = { abaAtual = "dashboard" }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (abaAtual) {
                "mural" -> MuralScreen(navController = navController, userRole = userRole)
                "ideias" -> HomeScreen(navController = navController, userRole = userRole)
                "feedbacks" -> FeedbackScreen()
                "projetos" -> ProjetosScreen(navController = navController, userRole = userRole)
                "dashboard" -> DashboardScreen(navController = navController)
            }
        }
    }
}