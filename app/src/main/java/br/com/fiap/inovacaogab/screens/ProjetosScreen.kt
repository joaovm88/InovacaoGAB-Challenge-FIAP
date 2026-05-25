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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
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
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

// Modelo do Projeto alinhado às verticais do GAB
data class ProjetoAndamento(
    val id: String = "",
    val titulo: String = "",
    val setor: String = "",
    val fase: String = "Planejamento",
    val progresso: Float = 0.1f,
    val economiaAtual: String = "R$ 0,00"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjetosScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userRole: String = "Operador(a)"
) {
    val context = LocalContext.current
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val greenSuccess = Color(0xFF10B981)

    var mostrarDialogAtualizar by remember { mutableStateOf(false) }
    var projetoSelecionado by remember { mutableStateOf<ProjetoAndamento?>(null) }

    // Estados para o formulário de atualização do Gestor
    var faseSelecionada by remember { mutableStateOf("Planejamento") }
    var novaEconomia by remember { mutableStateOf("") }
    var progressoSlider by remember { mutableStateOf(0.1f) }
    val fasesOpcoes = listOf("Planejamento", "Piloto/Testes", "Homologação", "Implementado")

    val listaProjetos = remember { mutableStateListOf<ProjetoAndamento>() }
    val dbRef = Firebase.database.getReference("projetos_andamento")

    // Escuta ativa do Firebase com Auto-Populate
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Se o banco estiver zerado, cria projetos baseados em dores reais do GAB
                    val p1 = ProjetoAndamento(dbRef.push().key ?: "p1", "Telemetria Inteligente Frota VIX", "Logística", "Piloto/Testes", 0.45f, "R$ 28.000,00")
                    val p2 = ProjetoAndamento(dbRef.push().key ?: "p2", "Otimização de Escalas Viação Águia Branca", "Passageiros", "Planejamento", 0.15f, "R$ 0,00")
                    val p3 = ProjetoAndamento(dbRef.push().key ?: "p3", "Totens Autoatendimento Vitória Motors", "Comércio", "Homologação", 0.80f, "R$ 15.400,00")

                    dbRef.child(p1.id).setValue(p1)
                    dbRef.child(p2.id).setValue(p2)
                    dbRef.child(p3.id).setValue(p3)
                } else {
                    listaProjetos.clear()
                    for (child in snapshot.children) {
                        child.getValue(ProjetoAndamento::class.java)?.let { listaProjetos.add(it) }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.addValueEventListener(listener)
        onDispose { dbRef.removeEventListener(listener) }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Projetos do Ecossistema", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)
            )
        }
    ) { paddingValues ->
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
                                Text(projeto.titulo, fontWeight = FontWeight.Bold, color = gabBlueDark, fontSize = 16.sp)
                                Text("Vertical: ${projeto.setor}", color = Color.Gray, fontSize = 12.sp)
                            }

                            // Gestor(a) pode clicar para editar e atualizar resultados
                            if (userRole == "Gestor(a)") {
                                IconButton(onClick = {
                                    projetoSelecionado = projeto
                                    faseSelecionada = projeto.fase
                                    novaEconomia = projeto.economiaAtual
                                    progressoSlider = projeto.progresso
                                    mostrarDialogAtualizar = true
                                }) {
                                    Icon(Icons.Default.Edit, "Atualizar", tint = gabBlueLight)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fase Atual: ${projeto.fase}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = gabBlueDark)
                            Text("Economia: ${projeto.economiaAtual}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = greenSuccess)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Progresso Linear Dinâmico
                        LinearProgressIndicator(
                            progress = projeto.progresso,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (projeto.progresso >= 0.8f) greenSuccess else gabBlueLight,
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

                    // Input de Economia
                    OutlinedTextField(
                        value = novaEconomia,
                        onValueChange = { novaEconomia = it },
                        label = { Text("Ganhos/Economia Atual (Ex: R$ 5.000)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Seletor de Fase
                    var expandirFases by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandirFases, onExpandedChange = { expandirFases = !expandirFases }) {
                        OutlinedTextField(
                            value = faseSelecionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fase de Desenvolvimento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirFases) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandirFases, onDismissRequest = { expandirFases = false }) {
                            fasesOpcoes.forEach { fase ->
                                DropdownMenuItem(text = { Text(fase) }, onClick = {
                                    faseSelecionada = fase
                                    // Ajusta o progresso da barra automaticamente com base na fase escolhida
                                    progressoSlider = when(fase) {
                                        "Planejamento" -> 0.15f
                                        "Piloto/Testes" -> 0.45f
                                        "Homologação" -> 0.75f
                                        else -> 1.0f
                                    }
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
                        val projetoAtualizado = projetoSelecionado!!.copy(
                            fase = faseSelecionada,
                            economiaAtual = novaEconomia,
                            progresso = progressoSlider
                        )
                        dbRef.child(projetoAtualizado.id).setValue(projetoAtualizado)
                        Toast.makeText(context, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                        mostrarDialogAtualizar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gabBlueDark)
                ) { Text("Salvar Alterações", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogAtualizar = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }
}