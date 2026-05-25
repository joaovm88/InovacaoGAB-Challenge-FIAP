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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.inovacaogab.R
import br.com.fiap.inovacaogab.model.IdeiaInovacao
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userRole: String = "Operador(a)"
) {
    val context = LocalContext.current
    var mostrarDialog by remember { mutableStateOf(false) }
    var operacaoAtual by remember { mutableStateOf("Criar") }
    var ideiaSelecionada by remember { mutableStateOf(IdeiaInovacao()) }

    var mostrarDialogFeedback by remember { mutableStateOf(false) }
    var ideiaParaFeedback by remember { mutableStateOf(IdeiaInovacao()) }
    var feedbackMotivo by remember { mutableStateOf("Orçamento focado em outras prioridades no momento.") }
    val motivosFeedback = listOf(
        "Orçamento focado em outras prioridades no momento.",
        "Exige infraestrutura tecnológica ainda não homologada.",
        "Já existe uma iniciativa similar em andamento no Grupo.",
        "Excelente sugestão! Guardaremos no banco de talentos para o futuro."
    )

    var mostrarDialogAprovacao by remember { mutableStateOf(false) }
    var ideiaParaAprovar by remember { mutableStateOf(IdeiaInovacao()) }

    val listaIdeias = remember { mutableStateListOf<IdeiaInovacao>() }
    val database = Firebase.database("https://inovacaogab-b43c6-default-rtdb.firebaseio.com/")
    val dbRef = database.getReference("ideias")

    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val gabBackground = Color(0xFFF8FAFC)
    val gabSurface = Color(0xFFFFFFFF)
    val greenSuccess = Color(0xFF10B981)

    val currentUser = FirebaseAuth.getInstance().currentUser
    val emailUsuario = currentUser?.email ?: "usuario@gab.com"
    val nomeUsuario = emailUsuario.substringBefore("@").split(".").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaIdeias.clear()
                for (child in snapshot.children) {
                    child.getValue(IdeiaInovacao::class.java)?.let { listaIdeias.add(it) }
                }
                listaIdeias.reverse()
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
                title = {
                    Row(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.logo_gab), contentDescription = "Logo", modifier = Modifier.height(32.dp).wrapContentWidth())

                        // BOTÃO SAIR DESCRITIVO
                        TextButton(onClick = {
                            FirebaseAuth.getInstance().signOut()
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
            ExtendedFloatingActionButton(
                onClick = {
                    ideiaSelecionada = IdeiaInovacao(autorId = currentUser?.uid ?: "")
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
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                val corBadge = when (userRole) {
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
                        Surface(color = corBadge.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                            Text(text = userRole, color = corBadge, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            items(listaIdeias) { ideia ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = gabSurface), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ideia.tituloIdeia, fontWeight = FontWeight.Bold, color = gabBlueDark, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(onClick = { ideiaSelecionada = ideia; operacaoAtual = "Editar"; mostrarDialog = true }) { Icon(Icons.Default.Edit, contentDescription = null, tint = gabBlueLight, modifier = Modifier.size(20.dp)) }
                                IconButton(onClick = { dbRef.child(ideia.id).removeValue() }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                            }
                        }
                        Text("Setor: ${ideia.setor}", color = gabBlueLight, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(ideia.descricao, fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = if (ideia.foiAprovada) Color(0xFFE0F2FE) else Color(0xFFF1F5F9)) {
                                Text(if (ideia.foiAprovada) "Aprovada" else "Pendente", color = if (ideia.foiAprovada) Color(0xFF0369A1) else Color(0xFF475569), modifier = Modifier.padding(4.dp))
                            }

                            if ((userRole == "Gestor(a)" || userRole == "Líder") && !ideia.foiAprovada) {
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
    if (mostrarDialogAprovacao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogAprovacao = false },
            title = { Text("Aprovar Sugestão", fontWeight = FontWeight.Bold) },
            text = { Text("O colaborador receberá: \"Parabéns! Sua ideia foi pré-aprovada! Vamos marcar um bate-papo.\"") },
            confirmButton = {
                Button(onClick = {
                    dbRef.child(ideiaParaAprovar.id).child("foiAprovada").setValue(true)
                    // Envia feedback para o autor
                    val fbRef = Firebase.database.getReference("user_feedbacks").child(ideiaParaAprovar.autorId).push()
                    fbRef.setValue(FeedbackMensagem(id = fbRef.key!!, tituloIdeia = ideiaParaAprovar.tituloIdeia, mensagem = "Sua ideia foi pré-aprovada! Vamos marcar um bate-papo.", status = "Aprovada"))
                    mostrarDialogAprovacao = false
                }, colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)) { Text("Confirmar") }
            }
        )
    }

    // DIALOG FEEDBACK (NEGATIVA)
    if (mostrarDialogFeedback) {
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
                    val fbRef = Firebase.database.getReference("user_feedbacks").child(ideiaParaFeedback.autorId).push()
                    fbRef.setValue(FeedbackMensagem(id = fbRef.key!!, tituloIdeia = ideiaParaFeedback.tituloIdeia, mensagem = feedbackMotivo, status = "Arquivada"))
                    dbRef.child(ideiaParaFeedback.id).removeValue()
                    mostrarDialogFeedback = false
                }) { Text("Enviar Feedback") }
            }
        )
    }

    if (mostrarDialog) {
        IdeiaDialog(onDismiss = { mostrarDialog = false }, onConfirm = { novaIdeia ->
            if (operacaoAtual == "Registrar Ideia") {
                val key = dbRef.push().key
                if (key != null) dbRef.child(key).setValue(novaIdeia.copy(id = key))
            } else {
                dbRef.child(novaIdeia.id).setValue(novaIdeia)
            }
            mostrarDialog = false
        }, ideia = ideiaSelecionada, operacao = operacaoAtual)
    }
}