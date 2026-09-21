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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.inovacaogab.data.SessionManager

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var abaAtual by remember { mutableStateOf("mural") }

    // Perfil do usuário logado (OPERADOR, GESTOR ou LIDER), lido da sessão local
    // salva no login, sem depender mais do Firebase Realtime Database.
    val userRole = sessionManager.getRoleUi()

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

                // ABA VISÃO EXECUTIVA: exclusiva do Líder (mesma regra aplicada no backend)
                if (userRole == "Líder") {
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
