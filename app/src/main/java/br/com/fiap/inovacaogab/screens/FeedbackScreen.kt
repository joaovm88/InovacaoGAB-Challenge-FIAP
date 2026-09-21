package br.com.fiap.inovacaogab.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.IdeiaDto
import br.com.fiap.inovacaogab.data.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen() {
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val autorId = sessionManager.getUserId() ?: ""

    val listaFeedbacks = remember { mutableStateListOf<IdeiaDto>() }
    var carregando by remember { mutableStateOf(true) }

    // Feedbacks são as ideias do próprio operador já avaliadas (aprovadas ou arquivadas)
    LaunchedEffect(Unit) {
        try {
            val resposta = ApiClient.service.listarIdeiasPorAutor(autorId)
            if (resposta.isSuccessful) {
                listaFeedbacks.clear()
                resposta.body()
                    ?.filter { it.status == "APROVADA" || it.status == "ARQUIVADA" }
                    ?.let { listaFeedbacks.addAll(it.reversed()) }
            }
        } finally {
            carregando = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Meus Feedbacks", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Retorno da Liderança", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = gabBlueDark, modifier = Modifier.padding(20.dp))

            if (carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = gabBlueLight)
                }
            } else if (listaFeedbacks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Você ainda não possui feedbacks registrados.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(listaFeedbacks) { fb ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = gabBlueLight)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(fb.titulo, fontWeight = FontWeight.Bold, color = gabBlueDark)
                                    Text(fb.feedbackGestor ?: "Sem observações adicionais.", fontSize = 14.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val statusLabel = if (fb.status == "APROVADA") "Aprovada" else "Arquivada"
                                    Text("Status: $statusLabel", fontWeight = FontWeight.Bold, color = gabBlueLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
