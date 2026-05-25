package br.com.fiap.inovacaogab.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

data class FeedbackMensagem(
    val id: String = "",
    val tituloIdeia: String = "",
    val mensagem: String = "",
    val status: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen() {
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val listaFeedbacks = remember { mutableStateListOf<FeedbackMensagem>() }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val dbRef = Firebase.database.getReference("user_feedbacks").child(userId)

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaFeedbacks.clear()
                for (child in snapshot.children) {
                    child.getValue(FeedbackMensagem::class.java)?.let { listaFeedbacks.add(it) }
                }
                listaFeedbacks.reverse()
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
                title = { Text("Meus Feedbacks", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = gabBlueDark)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Retorno da Liderança", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = gabBlueDark, modifier = Modifier.padding(20.dp))

            if (listaFeedbacks.isEmpty()) {
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
                                    Text(fb.tituloIdeia, fontWeight = FontWeight.Bold, color = gabBlueDark)
                                    Text(fb.mensagem, fontSize = 14.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Status: ${fb.status}", fontWeight = FontWeight.Bold, color = gabBlueLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

