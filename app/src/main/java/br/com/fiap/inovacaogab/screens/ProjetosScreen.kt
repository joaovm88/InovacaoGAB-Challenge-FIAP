package br.com.fiap.inovacaogab.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.ProjetoDto
import kotlinx.coroutines.launch
import java.util.Locale

// Rótulo exibido -> constante do enum FaseProjeto no backend
private val fasesProjeto = listOf(
    "Planejamento" to "PLANEJAMENTO",
    "Piloto" to "PILOTO",
    "Em Andamento" to "EM_ANDAMENTO",
    "Concluído" to "CONCLUIDO"
)

private fun progressoPorFase(etapa: String?): Float = when (etapa) {
    "PLANEJAMENTO" -> 0.15f
    "PILOTO" -> 0.45f
    "EM_ANDAMENTO" -> 0.75f
    "CONCLUIDO" -> 1.0f
    else -> 0.1f
}

private fun rotuloFase(etapa: String?): String = fasesProjeto.firstOrNull { it.second == etapa }?.first ?: (etapa ?: "Planejamento")

private fun formatarMoeda(valor: Double?): String = String.format(Locale("pt", "BR"), "R$ %,.2f", valor ?: 0.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjetosScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userRole: String = "Operador(a)"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val greenSuccess = Color(0xFF10B981)

    val divisoesOpcoes = listOf("Passageiros", "Logística", "Comércio", "Corporativo")

    var mostrarDialogAtualizar by remember { mutableStateOf(false) }
    var projetoSelecionado by remember { mutableStateOf<ProjetoDto?>(null) }
    var faseSelecionada by remember { mutableStateOf(fasesProjeto.first()) }
    var novoRetorno by remember { mutableStateOf("") }

    var mostrarDialogNovo by remember { mutableStateOf(false) }
    var novoNome by remember { mutableStateOf("") }
    var novaDescricao by remember { mutableStateOf("") }
    var novaDivisao by remember { mutableStateOf(divisoesOpcoes.first()) }
    var novoInvestimento by remember { mutableStateOf("") }

    val listaProjetos = remember { mutableStateListOf<ProjetoDto>() }
    var carregando by remember { mutableStateOf(true) }

    suspend fun carregarProjetos() {
        carregando = true
        try {
            val resposta = ApiClient.service.listarProjetos()
            if (resposta.isSuccessful) {
                listaProjetos.clear()
                resposta.body()?.let { listaProjetos.addAll(it) }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao carregar projetos: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            carregando = false
        }
    }

    LaunchedEffect(Unit) { carregarProjetos() }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Projetos do Ecossistema", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)
            )
        },
        floatingActionButton = {
            if (userRole == "Gestor(a)") {
                ExtendedFloatingActionButton(
                    onClick = {
                        novoNome = ""; novaDescricao = ""; novaDivisao = divisoesOpcoes.first(); novoInvestimento = ""
                        mostrarDialogNovo = true
                    },
                    containerColor = gabBlueLight,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Novo Projeto", fontWeight = FontWeight.Bold) }
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
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("Projetos em Execução", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = gabBlueDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Acompanhe o andamento das ideias que saíram do papel e viraram soluções.", color = Color.Gray)
                }
            }

            if (listaProjetos.isEmpty()) {
                item {
                    Text("Nenhum projeto cadastrado ainda.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }

            items(listaProjetos) { projeto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).background(gabBlueLight.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Assignment, null, tint = gabBlueLight)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(projeto.nome, fontWeight = FontWeight.Bold, color = gabBlueDark, fontSize = 16.sp)
                                Text("Vertical: ${projeto.divisao}", color = Color.Gray, fontSize = 12.sp)
                            }

                            if (userRole == "Gestor(a)") {
                                IconButton(onClick = {
                                    projetoSelecionado = projeto
                                    faseSelecionada = fasesProjeto.firstOrNull { it.second == projeto.etapa } ?: fasesProjeto.first()
                                    novoRetorno = projeto.retornoFinanceiro?.toString() ?: ""
                                    mostrarDialogAtualizar = true
                                }) {
                                    Icon(Icons.Default.Edit, "Atualizar", tint = gabBlueLight)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fase Atual: ${rotuloFase(projeto.etapa)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = gabBlueDark)
                            Text("Retorno: ${formatarMoeda(projeto.retornoFinanceiro)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = greenSuccess)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val progresso = progressoPorFase(projeto.etapa)
                        LinearProgressIndicator(
                            progress = progresso,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (progresso >= 0.8f) greenSuccess else gabBlueLight,
                            trackColor = Color(0xFFF1F5F9),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }

    // POP-UP DE ATUALIZAÇÃO EXCLUSIVO DO GESTOR
    if (mostrarDialogAtualizar && projetoSelecionado != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogAtualizar = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Atualizar Resultados", fontWeight = FontWeight.Bold, color = gabBlueDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Gerencie a evolução do projeto e registre o ganho financeiro obtido.", fontSize = 14.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = novoRetorno,
                        onValueChange = { novoRetorno = it },
                        label = { Text("Retorno financeiro (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    var expandirFases by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandirFases, onExpandedChange = { expandirFases = !expandirFases }) {
                        OutlinedTextField(
                            value = faseSelecionada.first,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fase de Desenvolvimento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirFases) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandirFases, onDismissRequest = { expandirFases = false }) {
                            fasesProjeto.forEach { opcao ->
                                DropdownMenuItem(text = { Text(opcao.first) }, onClick = {
                                    faseSelecionada = opcao
                                    expandirFases = false
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = projetoSelecionado?.id ?: return@Button
                        val retornoValor = novoRetorno.replace(",", ".").toDoubleOrNull()
                        val faseEnum = faseSelecionada.second
                        coroutineScope.launch {
                            try {
                                ApiClient.service.atualizarProgressoProjeto(
                                    id = id,
                                    fase = faseEnum,
                                    retornoFinanceiro = retornoValor,
                                    finalizado = faseEnum == "CONCLUIDO"
                                )
                                Toast.makeText(context, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                                mostrarDialogAtualizar = false
                                carregarProjetos()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Falha ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gabBlueDark)
                ) { Text("Salvar Alterações", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogAtualizar = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }

    // POP-UP DE CRIAÇÃO DE NOVO PROJETO (GESTOR)
    if (mostrarDialogNovo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogNovo = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Novo Projeto", fontWeight = FontWeight.Bold, color = gabBlueDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = novoNome, onValueChange = { novoNome = it }, label = { Text("Nome do projeto") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = novaDescricao, onValueChange = { novaDescricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                    var expandirDivisoes by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandirDivisoes, onExpandedChange = { expandirDivisoes = !expandirDivisoes }) {
                        OutlinedTextField(
                            value = novaDivisao,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vertical/Divisão") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDivisoes) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandirDivisoes, onDismissRequest = { expandirDivisoes = false }) {
                            divisoesOpcoes.forEach { opcao ->
                                DropdownMenuItem(text = { Text(opcao) }, onClick = { novaDivisao = opcao; expandirDivisoes = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = novoInvestimento,
                        onValueChange = { novoInvestimento = it },
                        label = { Text("Investimento inicial (R$)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val investimentoValor = novoInvestimento.replace(",", ".").toDoubleOrNull()
                    if (novoNome.isBlank() || investimentoValor == null || investimentoValor <= 0.0) {
                        Toast.makeText(context, "Informe nome e um investimento inicial válido.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        try {
                            ApiClient.service.criarProjeto(
                                ProjetoDto(
                                    nome = novoNome,
                                    descricao = novaDescricao,
                                    divisao = novaDivisao,
                                    etapa = "PLANEJAMENTO",
                                    investimento = investimentoValor
                                )
                            )
                            mostrarDialogNovo = false
                            carregarProjetos()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Falha ao criar projeto: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = gabBlueDark)) { Text("Criar Projeto", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogNovo = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }
}
