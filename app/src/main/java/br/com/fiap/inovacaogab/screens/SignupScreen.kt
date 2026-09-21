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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import br.com.fiap.inovacaogab.R
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.LoginRequest
import br.com.fiap.inovacaogab.data.RegistroRequest
import br.com.fiap.inovacaogab.data.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estaCarregando by remember { mutableStateOf(false) }

    // Estados para o Menu Suspenso de Função (Role)
    var expandirMenu by remember { mutableStateOf(false) }
    val funcoesDisponiveis = listOf("Operador(a)", "Gestor(a)", "Líder")
    var funcaoSelecionada by remember { mutableStateOf(funcoesDisponiveis.first()) }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

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
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                value = funcaoSelecionada,
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
                        text = { Text(selecao) },
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
                if (nome.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
                    estaCarregando = true
                    coroutineScope.launch {
                        try {
                            val nivelAcesso = SessionManager.mapUiParaNivelAcesso(funcaoSelecionada)
                            val respostaRegistro = ApiClient.service.registrar(
                                RegistroRequest(nome.trim(), email.trim(), password, nivelAcesso)
                            )

                            if (respostaRegistro.isSuccessful) {
                                // Efetua login automaticamente para obter o token JWT
                                val respostaLogin = ApiClient.service.login(LoginRequest(email.trim(), password))
                                if (respostaLogin.isSuccessful && respostaLogin.body() != null) {
                                    val corpo = respostaLogin.body()!!
                                    sessionManager.salvarSessao(corpo.token, corpo.nivelAcesso)
                                    navController.navigate("home") {
                                        popUpTo("signup") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Cadastro criado! Faça login para continuar.", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                }
                            } else if (respostaRegistro.code() == 409) {
                                Toast.makeText(context, "Este e-mail já está cadastrado.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Não foi possível concluir o cadastro.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Não foi possível conectar ao servidor: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            estaCarregando = false
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
