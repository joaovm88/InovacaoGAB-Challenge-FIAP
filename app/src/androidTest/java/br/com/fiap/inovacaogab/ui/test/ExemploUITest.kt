package br.com.fiap.inovacaogab.ui.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import br.com.fiap.inovacaogab.screens.LoginScreen
import br.com.fiap.inovacaogab.ui.theme.InovacaoGABTheme
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ExemploUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        // Garante que o usuário está deslogado para os testes de login
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun verificarSeElementosPrincipaisEstaoVisiveis() {
        composeTestRule.setContent {
            InovacaoGABTheme {
                LoginScreen(navController = rememberNavController())
            }
        }

        // Verifica os novos textos da interface do Grupo Águia Branca
        composeTestRule.onNodeWithText("Plataforma de Inovação Corporativa").assertIsDisplayed()
        composeTestRule.onNodeWithText("E-mail corporativo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Senha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entrar").assertIsDisplayed()
    }

    @Test
    fun testarDigitacaoEmailESenha() {
        composeTestRule.setContent {
            InovacaoGABTheme {
                LoginScreen(navController = rememberNavController())
            }
        }

        // Usa os TestTags ou os labels atualizados
        composeTestRule.onNodeWithTag("email_field").performTextInput("usuario@teste.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")

        // Clica no botão entrar usando a tag
        composeTestRule.onNodeWithTag("login_button").performClick()
        
        // Verifica se o loader aparece (estado estaCarregando = true)
        composeTestRule.onNodeWithTag("loader").assertIsDisplayed()
    }
}
