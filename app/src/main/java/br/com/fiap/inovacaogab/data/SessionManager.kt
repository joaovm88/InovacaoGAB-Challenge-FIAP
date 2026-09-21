package br.com.fiap.inovacaogab.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda a sessao do usuario logado (token JWT + perfil) localmente no
 * dispositivo, em substituicao ao FirebaseAuth/FirebaseDatabase usados na
 * Sprint 1. O id e o e-mail do usuario sao extraidos do proprio token, ja
 * que o backend nao os devolve no corpo da resposta de login.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("inovagab_session", Context.MODE_PRIVATE)

    fun salvarSessao(token: String, nivelAcesso: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_NIVEL_ACESSO, nivelAcesso)
            .apply()
    }

    fun limparSessao() {
        prefs.edit().clear().apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getNivelAcesso(): String? = prefs.getString(KEY_NIVEL_ACESSO, null)

    fun getUserId(): String? = getToken()?.let { JwtUtils.extrairId(it) }

    fun getEmail(): String? = getToken()?.let { JwtUtils.extrairEmail(it) }

    fun estaLogado(): Boolean = !getToken().isNullOrBlank()

    /** Converte o enum do backend (OPERADOR/GESTOR/LIDER) para o rotulo usado nas telas do app. */
    fun getRoleUi(): String = mapNivelAcessoParaUi(getNivelAcesso())

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_NIVEL_ACESSO = "nivel_acesso"

        fun mapNivelAcessoParaUi(nivelAcesso: String?): String = when (nivelAcesso?.uppercase()) {
            "GESTOR" -> "Gestor(a)"
            "LIDER" -> "Líder"
            else -> "Operador(a)"
        }

        fun mapUiParaNivelAcesso(roleUi: String): String = when (roleUi) {
            "Gestor(a)" -> "GESTOR"
            "Líder" -> "LIDER"
            else -> "OPERADOR"
        }
    }
}
