package br.com.fiap.inovacaogab.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.*

data class OrientacaoEstrategica(
    val id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val dataPublicacao: String = ""
)

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

    var mostrarDialogNovo by remember { mutableStateOf(false) }
    var novoTitulo by remember { mutableStateOf("") }
    var novaMensagem by remember { mutableStateOf("") }

    // Estado para o Pop-up de Leitura do Mural
    var orientacaoSelecionada by remember { mutableStateOf<OrientacaoEstrategica?>(null) }

    val listaOrientacoes = remember { mutableStateListOf<OrientacaoEstrategica>() }
    val dbRef = Firebase.database.getReference("orientacoes")

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                    val dataMock = formatter.format(Date())

                    val mock1 = OrientacaoEstrategica(dbRef.push().key ?: "1", "ESG: Novas Metas para a Reserva", "O Grupo investiu R$ 300 mil na preservação da Mata Atlântica e avança com veículos elétricos na Vix Logística. Orientação aos Gestores: priorizem a aprovação de ideias que reduzam impacto ambiental nas operações diárias.", dataMock)
                    val mock2 = OrientacaoEstrategica(dbRef.push().key ?: "2", "Programa de Inovação Aberta & UX Labs", "Abrimos 3 desafios estratégicos focados em eficiência de transporte, estoque e redução de turnover. Colaborador, utilize o espaço do UX Labs para modelar suas ideias. As melhores soluções concorrerão ao próximo 'Oscar da Inovação'!", dataMock)

                    dbRef.child(mock1.id).setValue(mock1)
                    dbRef.child(mock2.id).setValue(mock2)
                } else {
                    listaOrientacoes.clear()
                    for (child in snapshot.children) {
                        child.getValue(OrientacaoEstrategica::class.java)?.let { listaOrientacoes.add(it) }
                    }
                    listaOrientacoes.reverse()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.addValueEventListener(listener)
        onDispose { dbRef.removeEventListener(listener) }
    }

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

            items(listaOrientacoes) { aviso ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { orientacaoSelecionada = aviso }, // CLICÁVEL PARA ABRIR LEITURA
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
                                Text(aviso.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = gabBlueDark)
                                Text(aviso.dataPublicacao, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            if (userRole == "Líder") {
                                IconButton(onClick = { dbRef.child(aviso.id).removeValue() }) {
                                    Icon(Icons.Default.Delete, "Apagar", tint = Color.Red)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = aviso.mensagem,
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
                    Text(orientacaoSelecionada!!.titulo, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = gabBlueDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Publicado em: ${orientacaoSelecionada!!.dataPublicacao}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = gabBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = orientacaoSelecionada!!.mensagem,
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
                    OutlinedTextField(value = novoTitulo, onValueChange = { novoTitulo = it }, label = { Text("Assunto") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = novaMensagem, onValueChange = { novaMensagem = it }, label = { Text("Mensagem") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (novoTitulo.isNotEmpty()) {
                        val key = dbRef.push().key ?: ""
                        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                        dbRef.child(key).setValue(OrientacaoEstrategica(key, novoTitulo, novaMensagem, formatter.format(Date())))
                        novoTitulo = ""; novaMensagem = ""; mostrarDialogNovo = false
                    }
                }) { Text("Publicar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogNovo = false }) { Text("Cancelar") } }
        )
    }
}