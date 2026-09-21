package br.com.fiap.inovacaogab.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.EstrategiaDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userRole: String = "Operador(a)"
) {
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val gabBackground = Color(0xFFF8FAFC)
    val gabSurface = Color(0xFFFFFFFF)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mostrarDialogNovo by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf("") }
    var novoTitulo by remember { mutableStateOf("") }
    var novaMensagem by remember { mutableStateOf("") }

    var orientacaoSelecionada by remember { mutableStateOf<EstrategiaDto?>(null) }
    val listaOrientacoes = remember { mutableStateListOf<EstrategiaDto>() }
    var carregando by remember { mutableStateOf(true) }

    suspend fun carregarEstrategias() {
        carregando = true
        try {
            val resposta = ApiClient.service.listarEstrategiasAtivas()
            if (resposta.isSuccessful) {
                listaOrientacoes.clear()
                resposta.body()?.let { listaOrientacoes.addAll(it.reversed()) }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao carregar o mural: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            carregando = false
        }
    }

    LaunchedEffect(Unit) { carregarEstrategias() }

    Scaffold(
        containerColor = gabBackground,
        topBar = {
            TopAppBar(
                title = { Text("Comunicações Internas", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)
            )
        },
        floatingActionButton = {
            if (userRole == "Líder") {
                ExtendedFloatingActionButton(
                    onClick = { mostrarDialogNovo = true },
                    containerColor = gabBlueLight,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Novo Comunicado", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        if (carregando) {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = gabBlueLight)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Mural Estratégico", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = gabBlueDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fique por dentro das diretrizes de negócios e inovação do GAB.", color = Color.Gray)
                }
            }

            if (listaOrientacoes.isEmpty()) {
                item {
                    Text("Nenhuma orientação estratégica publicada ainda.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }

            items(listaOrientacoes) { aviso ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { orientacaoSelecionada = aviso },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = gabSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(gabBlueLight.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Campaign, null, tint = gabBlueLight)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(aviso.campanha, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = gabBlueDark)
                                Text("${aviso.categoria} · ${aviso.dataCriacao ?: ""}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            if (userRole == "Líder") {
                                IconButton(onClick = {
                                    val id = aviso.id ?: return@IconButton
                                    coroutineScope.launch {
                                        try {
                                            ApiClient.service.excluirEstrategia(id)
                                            carregarEstrategias()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Falha ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, "Apagar", tint = Color.Red)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = aviso.descricao,
                            color = Color(0xFF475569),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ler comunicado completo...", color = gabBlueLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // DIALOG DE LEITURA DO MURAL
    if (orientacaoSelecionada != null) {
        AlertDialog(
            onDismissRequest = { orientacaoSelecionada = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = gabBlueLight, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Comunicado Oficial", fontWeight = FontWeight.Bold, color = gabBlueDark)
                }
            },
            text = {
                Column {
                    Divider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(orientacaoSelecionada!!.campanha, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = gabBlueDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${orientacaoSelecionada!!.categoria} · Publicado em: ${orientacaoSelecionada!!.dataCriacao ?: ""}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = gabBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = orientacaoSelecionada!!.descricao,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF334155),
                            lineHeight = 22.sp,
                            fontSize = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { orientacaoSelecionada = null },
                    colors = ButtonDefaults.buttonColors(containerColor = gabBlueLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // DIALOG NOVO COMUNICADO
    if (mostrarDialogNovo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogNovo = false },
            title = { Text("Nova Orientação", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = novaCategoria, onValueChange = { novaCategoria = it }, label = { Text("Categoria (ex: ESG, Inovação)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = novoTitulo, onValueChange = { novoTitulo = it }, label = { Text("Assunto") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = novaMensagem, onValueChange = { novaMensagem = it }, label = { Text("Mensagem") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (novoTitulo.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                val categoriaFinal = novaCategoria.ifEmpty { "Geral" }
                                ApiClient.service.criarEstrategia(
                                    EstrategiaDto(categoria = categoriaFinal, campanha = novoTitulo, descricao = novaMensagem)
                                )
                                novaCategoria = ""; novoTitulo = ""; novaMensagem = ""; mostrarDialogNovo = false
                                carregarEstrategias()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Falha ao publicar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Publicar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogNovo = false }) { Text("Cancelar") } }
        )
    }
}
