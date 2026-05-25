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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.inovacaogab.R
import br.com.fiap.inovacaogab.ui.theme.InovacaoGABTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estaCarregando by remember { mutableStateOf(false) }

    val autentica = FirebaseAuth.getInstance()
    val context = LocalContext.current


    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)
    val gabBackground = Color(0xFFF8FAFC)

    // Redireciona de forma segura se o usuário já estiver logado
    LaunchedEffect(Unit) {
        if (autentica.currentUser != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gabBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logotipo do Grupo Águia Branca
        Image(
            painter = painterResource(id = R.drawable.schedule),
            contentDescription = "Logo Grupo Águia Branca",
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Plataforma de Inovação Corporativa",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = gabBlueDark,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Campo de E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "E-mail corporativo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("email_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = gabBlueLight,
                focusedLabelColor = gabBlueLight,
                cursorColor = gabBlueLight
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Senha
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("password_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = gabBlueLight,
                focusedLabelColor = gabBlueLight,
                cursorColor = gabBlueLight
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão de Entrar
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    estaCarregando = true
                    autentica.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { tarefa ->
                            estaCarregando = false
                            if (tarefa.isSuccessful) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Autenticação falhou. Verifique e-mail e senha.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = gabBlueLight)
        ) {
            if (estaCarregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).testTag("loader"),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Entrar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de Cadastro
        TextButton(
            onClick = { navController.navigate("signup") }
        ) {
            Text("Não tem acesso? Cadastre-se aqui", color = gabBlueDark)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    InovacaoGABTheme {
        LoginScreen(navController = rememberNavController())
    }
}
