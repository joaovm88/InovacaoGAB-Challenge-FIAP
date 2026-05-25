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
import br.com.fiap.inovacaogab.screens.LoginScreen
import br.com.fiap.inovacaogab.screens.MainScreen
import br.com.fiap.inovacaogab.screens.SignupScreen
import br.com.fiap.inovacaogab.ui.theme.InovacaoGABTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InovacaoGABTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()


                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") { LoginScreen(navController = navController) }
                        composable("signup") { SignupScreen(navController = navController) }


                        composable("home") { MainScreen(navController = navController) }
                    }
                }
            }
        }
    }
}