package br.com.fiap.inovacaogab.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.inovacaogab.R
import br.com.fiap.inovacaogab.ui.theme.InovacaoGABTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estaCarregando by remember { mutableStateOf(false) }

    // Estados para o Menu Suspenso de Função (Role)
    var expandirMenu by remember { mutableStateOf(false) }
    var funcaoSelecionada by remember { mutableStateOf("operador") }
    val funcoesDisponiveis = listOf("Operador(a)", "Gestor(a)", "Líder")

    val autentica = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // Paleta de Cores
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val gabBackground = Color(0xFFF8FAFC)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gabBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_gab),
            contentDescription = "Logo Grupo Águia Branca",
            modifier = Modifier.height(64.dp).fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Criar Conta Corporativa", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = gabBlueDark)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha (mínimo 6 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Menu Suspenso para Escolher a Função
        ExposedDropdownMenuBox(
            expanded = expandirMenu,
            onExpandedChange = { expandirMenu = !expandirMenu }
        ) {
            OutlinedTextField(
                value = funcaoSelecionada.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Nível de Acesso") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirMenu) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expandirMenu,
                onDismissRequest = { expandirMenu = false }
            ) {
                funcoesDisponiveis.forEach { selecao ->
                    DropdownMenuItem(
                        text = { Text(selecao.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            funcaoSelecionada = selecao
                            expandirMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.length >= 6) {
                    estaCarregando = true
                    autentica.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { tarefa ->
                            if (tarefa.isSuccessful) {
                                val userId = autentica.currentUser?.uid ?: ""
                                val database = FirebaseDatabase.getInstance("https://inovacaogab-b43c6-default-rtdb.firebaseio.com/")
                                val userRef = database.getReference("users").child(userId)

                                // Salva a função exata que a pessoa escolheu na tela
                                val newUser = mapOf(
                                    "email" to email.trim(),
                                    "role" to funcaoSelecionada
                                )

                                userRef.setValue(newUser).addOnCompleteListener {
                                    estaCarregando = false
                                    navController.navigate("home") {
                                        popUpTo("signup") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } else {
                                estaCarregando = false
                                Toast.makeText(context, "Erro: ${tarefa.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Preencha corretamente.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = gabBlueDark)
        ) {
            if (estaCarregando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Já tem uma conta? Entrar", color = gabBlueLight)
        }
    }
}