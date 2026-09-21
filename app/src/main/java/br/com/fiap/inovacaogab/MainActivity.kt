package br.com.fiap.inovacaogab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.inovacaogab.data.ApiClient
import br.com.fiap.inovacaogab.data.SessionManager
import br.com.fiap.inovacaogab.screens.LoginScreen
import br.com.fiap.inovacaogab.screens.MainScreen
import br.com.fiap.inovacaogab.screens.SignupScreen
import br.com.fiap.inovacaogab.ui.theme.InovacaoGABTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        setContent {
            InovacaoGABTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (sessionManager.estaLogado()) "home" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") { LoginScreen(navController = navController) }
                        composable("signup") { SignupScreen(navController = navController) }


                        composable("home") { MainScreen(navController = navController) }
                    }
                }
            }
        }
    }
}