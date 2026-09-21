package br.com.fiap.inovacaogab.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.DashboardResumo
import br.com.fiap.inovacaogab.data.ProjetoDto
import java.util.Locale

private fun formatarMoeda(valor: Double): String = String.format(Locale("pt", "BR"), "R$ %,.2f", valor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(modifier: Modifier = Modifier, navController: NavController) {
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val greenSuccess = Color(0xFF10B981)
    val orangeAttention = Color(0xFFF59E0B)
    val context = LocalContext.current

    var resumo by remember { mutableStateOf<DashboardResumo?>(null) }
    var projetos by remember { mutableStateOf<List<ProjetoDto>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    var animar by remember { mutableStateOf(false) }
    var showRoiDetails by remember { mutableStateOf(false) }
    var showCostDetails by remember { mutableStateOf(false) }
    var showEsgDetails by remember { mutableStateOf(false) }
    var showDivisoesDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val respostaResumo = ApiClient.service.obterResultadosDashboard()
            if (respostaResumo.isSuccessful) resumo = respostaResumo.body()

            val respostaProjetos = ApiClient.service.listarProjetos()
            if (respostaProjetos.isSuccessful) projetos = respostaProjetos.body() ?: emptyList()

            animar = true
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao carregar o painel: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            carregando = false
        }
    }

    val resumoAtual = resumo ?: DashboardResumo()
    val progRoi by animateFloatAsState(if (animar) (resumoAtual.roiPercentual / 100.0).toFloat().coerceIn(0f, 1f) else 0f, tween(1200))
    val progCustos by animateFloatAsState(if (animar && resumoAtual.investimentoTotal > 0) (resumoAtual.reducaoCustosTotal / resumoAtual.investimentoTotal).toFloat().coerceIn(0f, 1f) else 0f, tween(1200))

    val divisoesAgrupadas = remember(projetos) {
        projetos.groupBy { it.divisao.ifBlank { "Não informado" } }
            .mapValues { it.value.size }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = { TopAppBar(title = { Text("Painel de Resultados", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)) }
    ) { padding ->
        if (carregando) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = gabBlueLight)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Visão Executiva Global", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = gabBlueDark) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDivisoesDetails = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Projetos por Divisão", fontWeight = FontWeight.Bold, color = gabBlueDark, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.PieChart, null, tint = gabBlueLight)
                        }
                        Text("${projetos.size} projeto(s) cadastrado(s) no total. Toque para ver a distribuição.", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val cores = listOf(gabBlueLight, greenSuccess, orangeAttention, gabBlueDark)
                            divisoesAgrupadas.entries.take(4).forEachIndexed { index, (nome, qtd) ->
                                val percentual = if (projetos.isNotEmpty()) (qtd * 100 / projetos.size) else 0
                                DivisionIndicator(name = nome, value = "$percentual%", color = cores[index % cores.size])
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showEsgDetails = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, greenSuccess.copy(0.5f))
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = greenSuccess, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Indicadores ESG", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            Text("Toque para expandir sustentabilidade", color = greenSuccess, fontSize = 12.sp)
                        }
                    }
                }
            }

            item { MetricCard("ROI do Funil Corporativo", "${String.format(Locale("pt", "BR"), "%.1f", resumoAtual.roiPercentual)}%", progRoi, gabBlueLight) { showRoiDetails = true } }
            item { MetricCard("Redução de Custos", formatarMoeda(resumoAtual.reducaoCustosTotal), progCustos, greenSuccess) { showCostDetails = true } }
        }
    }

    // Dialogs de Detalhes (com dados reais vindos do backend)

    if (showDivisoesDetails) {
        AlertDialog(
            onDismissRequest = { showDivisoesDetails = false },
            containerColor = Color.White,
            title = { Text("Detalhamento por Divisão", fontWeight = FontWeight.Bold, color = gabBlueDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (divisoesAgrupadas.isEmpty()) {
                        Text("Nenhum projeto cadastrado ainda.", color = Color.Gray)
                    } else {
                        divisoesAgrupadas.forEach { (nome, qtd) ->
                            ItemDetalheDash(Icons.Default.Assignment, nome, "$qtd Projeto(s) Ativo(s)", gabBlueLight)
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showDivisoesDetails = false }, colors = ButtonDefaults.buttonColors(gabBlueDark)) { Text("Fechar") } }
        )
    }

    if (showRoiDetails) {
        AlertDialog(
            onDismissRequest = { showRoiDetails = false },
            containerColor = Color.White,
            title = { Text("Composição do ROI", fontWeight = FontWeight.Bold, color = gabBlueDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemDetalheDash(Icons.Default.AttachMoney, "Investimento Total", formatarMoeda(resumoAtual.investimentoTotal), gabBlueLight)
                    ItemDetalheDash(Icons.Default.AttachMoney, "Retorno Total", formatarMoeda(resumoAtual.retornoTotal), greenSuccess)
                    ItemDetalheDash(Icons.Default.AttachMoney, "Lucro Líquido", formatarMoeda(resumoAtual.lucroLiquido), orangeAttention)
                }
            },
            confirmButton = { Button(onClick = { showRoiDetails = false }, colors = ButtonDefaults.buttonColors(gabBlueLight)) { Text("Fechar") } }
        )
    }

    if (showCostDetails) {
        AlertDialog(
            onDismissRequest = { showCostDetails = false },
            containerColor = Color.White,
            title = { Text("Economia Gerada", fontWeight = FontWeight.Bold, color = gabBlueDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemDetalheDash(Icons.Default.AttachMoney, "Redução de Custos Total", formatarMoeda(resumoAtual.reducaoCustosTotal), greenSuccess)
                    ItemDetalheDash(Icons.Default.Assignment, "Projetos considerados", "${resumoAtual.totalProjetos}", gabBlueLight)
                }
            },
            confirmButton = { Button(onClick = { showCostDetails = false }, colors = ButtonDefaults.buttonColors(greenSuccess)) { Text("Fechar") } }
        )
    }

    if (showEsgDetails) {
        AlertDialog(
            onDismissRequest = { showEsgDetails = false },
            containerColor = Color.White,
            title = { Text("Impacto Ambiental", fontWeight = FontWeight.Bold, color = Color(0xFF166534)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemDetalheDash(Icons.Default.Co2, "CO2 Evitado", "${resumoAtual.co2EvitadoToneladas} Toneladas", Color.Gray)
                    ItemDetalheDash(Icons.Default.WaterDrop, "Água Poupada", "${resumoAtual.aguaPoupadaLitros} Litros", gabBlueLight)
                }
            },
            confirmButton = { Button(onClick = { showEsgDetails = false }, colors = ButtonDefaults.buttonColors(greenSuccess)) { Text("Fechar") } }
        )
    }
}

@Composable
fun ItemDetalheDash(icone: ImageVector, titulo: String, valor: String, corBase: Color) {
    Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(corBase.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icone, contentDescription = null, tint = corBase)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(valor, color = Color(0xFF0A2540), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, progress: Float, accentColor: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF0A2540))
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp), color = accentColor, strokeCap = StrokeCap.Round, trackColor = accentColor.copy(0.15f))
        }
    }
}

@Composable
fun DivisionIndicator(name: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(64.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Text(value, fontWeight = FontWeight.Black, color = color, fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Bold)
    }
}
