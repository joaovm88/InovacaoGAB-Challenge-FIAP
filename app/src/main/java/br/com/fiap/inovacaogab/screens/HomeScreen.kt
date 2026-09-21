package br.com.fiap.inovacaogab.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.inovacaogab.R
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.IdeiaDto
import br.com.fiap.inovacaogab.data.SessionManager
import br.com.fiap.inovacaogab.model.IdeiaInovacao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userRole: String = "Operador(a)"
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val autorId = sessionManager.getUserId() ?: ""

    var mostrarDialog by remember { mutableStateOf(false) }
    var operacaoAtual by remember { mutableStateOf("Criar") }
    var ideiaSelecionada by remember { mutableStateOf(IdeiaInovacao()) }

    var mostrarDialogFeedback by remember { mutableStateOf(false) }
    var ideiaParaFeedback by remember { mutableStateOf<IdeiaDto?>(null) }
    var feedbackMotivo by remember { mutableStateOf("Orçamento focado em outras prioridades no momento.") }
    val motivosFeedback = listOf(
        "Orçamento focado em outras prioridades no momento.",
        "Exige infraestrutura tecnológica ainda não homologada.",
        "Já existe uma iniciativa similar em andamento no Grupo.",
        "Excelente sugestão! Guardaremos no banco de talentos para o futuro."
    )

    var mostrarDialogAprovacao by remember { mutableStateOf(false) }
    var ideiaParaAprovar by remember { mutableStateOf<IdeiaDto?>(null) }

    val listaIdeias = remember { mutableStateListOf<IdeiaDto>() }
    var carregando by remember { mutableStateOf(true) }

    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val gabBackground = Color(0xFFF8FAFC)
    val gabSurface = Color(0xFFFFFFFF)
    val greenSuccess = Color(0xFF10B981)

    val emailUsuario = sessionManager.getEmail() ?: "usuario@gab.com"
    val nomeUsuario = emailUsuario.substringBefore("@").split(".").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    // Operador consulta apenas as próprias ideias; Gestor/Líder revisam a fila de pendentes
    suspend fun carregarIdeias() {
        carregando = true
        try {
            val resposta = if (userRole == "Operador(a)") {
                ApiClient.service.listarIdeiasPorAutor(autorId)
            } else {
                ApiClient.service.listarIdeiasPendentes()
            }
            if (resposta.isSuccessful) {
                listaIdeias.clear()
                resposta.body()?.let { listaIdeias.addAll(it.reversed()) }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao carregar ideias: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            carregando = false
        }
    }

    LaunchedEffect(userRole) { carregarIdeias() }

    Scaffold(
        containerColor = gabBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.logo_gab), contentDescription = "Logo", modifier = Modifier.height(32.dp).wrapContentWidth())

                        // BOTÃO SAIR DESCRITIVO
                        TextButton(onClick = {
                            sessionManager.limparSessao()
                            navController.navigate("login") { popUpTo("home") { inclusive = true } }
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sair", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)
            )
        },
        floatingActionButton = {
            if (userRole == "Operador(a)") {
                ExtendedFloatingActionButton(
                    onClick = {
                        ideiaSelecionada = IdeiaInovacao(autorId = autorId)
                        operacaoAtual = "Registrar Ideia"
                        mostrarDialog = true
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = gabBlueLight,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nova Ideia", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        if (carregando) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = gabBlueLight)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                val corBadgePerfil = when (userRole) {
                    "Líder" -> Color(0xFF0F172A)
                    "Gestor(a)" -> Color(0xFF0284C7)
                    else -> Color(0xFF10B981)
                }

                Row(modifier = Modifier.fillMaxWidth().background(gabSurface, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).background(gabBackground, CircleShape), contentAlignment = Alignment.Center) {
                        Text(text = nomeUsuario.take(1), fontWeight = FontWeight.Bold, color = gabBlueDark, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Olá, $nomeUsuario", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = gabBlueDark)
                        Surface(color = corBadgePerfil.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                            Text(text = userRole, color = corBadgePerfil, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            if (listaIdeias.isEmpty()) {
                item {
                    Text(
                        text = if (userRole == "Operador(a)") "Você ainda não registrou nenhuma ideia." else "Não há ideias pendentes de avaliação no momento.",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(listaIdeias) { ideia ->
                val (corBadge, textoBadge, corTexto) = when (ideia.status) {
                    "APROVADA" -> Triple(Color(0xFFE0F2FE), "Aprovada", Color(0xFF0369A1))
                    "ARQUIVADA" -> Triple(Color(0xFFFEE2E2), "Arquivada", Color(0xFFB91C1C))
                    else -> Triple(Color(0xFFF1F5F9), "Pendente", Color(0xFF475569))
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = gabSurface), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ideia.titulo, fontWeight = FontWeight.Bold, color = gabBlueDark, modifier = Modifier.weight(1f))

                            if (userRole == "Operador(a)") {
                                Row {
                                    IconButton(onClick = {
                                        ideiaSelecionada = IdeiaInovacao(
                                            id = ideia.id ?: "",
                                            autorId = ideia.autorId ?: autorId,
                                            tituloIdeia = ideia.titulo,
                                            setor = ideia.setor,
                                            descricao = ideia.descricao
                                        )
                                        operacaoAtual = "Editar"
                                        mostrarDialog = true
                                    }) { Icon(Icons.Default.Edit, contentDescription = null, tint = gabBlueLight, modifier = Modifier.size(20.dp)) }
                                    IconButton(onClick = {
                                        val id = ideia.id ?: return@IconButton
                                        coroutineScope.launch {
                                            try {
                                                ApiClient.service.excluirIdeia(id)
                                                carregarIdeias()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Falha ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                                }
                            }
                        }
                        Text("Setor: ${ideia.setor}", color = gabBlueLight, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(ideia.descricao, fontSize = 14.sp, color = Color.DarkGray)

                        if (ideia.pontuacaoIa != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pontuação da IA: ${ideia.pontuacaoIa}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = gabBlueLight)
                            ideia.justificativaIa?.let {
                                Text(it, fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = corBadge) {
                                Text(textoBadge, color = corTexto, modifier = Modifier.padding(4.dp))
                            }

                            if (userRole == "Gestor(a)" || userRole == "Líder") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { ideiaParaAprovar = ideia; mostrarDialogAprovacao = true }, colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)) { Text("Aprovar", fontSize = 12.sp) }
                                    Button(onClick = { ideiaParaFeedback = ideia; mostrarDialogFeedback = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Avaliar", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG APROVAÇÃO
    if (mostrarDialogAprovacao && ideiaParaAprovar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogAprovacao = false },
            title = { Text("Aprovar Sugestão", fontWeight = FontWeight.Bold) },
            text = { Text("O colaborador receberá: \"Parabéns! Sua ideia foi pré-aprovada! Vamos marcar um bate-papo.\"") },
            confirmButton = {
                Button(onClick = {
                    val id = ideiaParaAprovar?.id ?: return@Button
                    coroutineScope.launch {
                        try {
                            ApiClient.service.avaliarIdeia(id, "APROVADA", "Sua ideia foi pré-aprovada! Vamos marcar um bate-papo.")
                            mostrarDialogAprovacao = false
                            carregarIdeias()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Falha ao aprovar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogAprovacao = false }) { Text("Cancelar") } }
        )
    }

    // DIALOG FEEDBACK (NEGATIVA/ARQUIVAMENTO)
    if (mostrarDialogFeedback && ideiaParaFeedback != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogFeedback = false },
            title = { Text("Feedback de Análise", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecione a justificativa:")
                    motivosFeedback.forEach { motivo ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = feedbackMotivo == motivo, onClick = { feedbackMotivo = motivo })
                            Text(motivo, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val id = ideiaParaFeedback?.id ?: return@Button
                    coroutineScope.launch {
                        try {
                            ApiClient.service.avaliarIdeia(id, "ARQUIVADA", feedbackMotivo)
                            mostrarDialogFeedback = false
                            carregarIdeias()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Falha ao enviar feedback: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Enviar Feedback") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogFeedback = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarDialog) {
        IdeiaDialog(onDismiss = { mostrarDialog = false }, onConfirm = { novaIdeia ->
            coroutineScope.launch {
                try {
                    val dto = IdeiaDto(
                        id = if (novaIdeia.id.isBlank()) null else novaIdeia.id,
                        titulo = novaIdeia.tituloIdeia,
                        descricao = novaIdeia.descricao,
                        setor = novaIdeia.setor,
                        autorId = novaIdeia.autorId
                    )
                    if (operacaoAtual == "Registrar Ideia") {
                        ApiClient.service.criarIdeia(dto)
                    } else {
                        ApiClient.service.atualizarIdeia(novaIdeia.id, dto)
                    }
                    mostrarDialog = false
                    carregarIdeias()
                } catch (e: Exception) {
                    Toast.makeText(context, "Falha ao salvar ideia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }, ideia = ideiaSelecionada, operacao = operacaoAtual)
    }
}
