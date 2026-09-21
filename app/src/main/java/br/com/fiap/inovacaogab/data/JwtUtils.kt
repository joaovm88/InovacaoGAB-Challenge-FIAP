package br.com.fiap.inovacaogab.data

import android.util.Base64
import org.json.JSONObject

/**
 * O backend nao retorna o id/e-mail do usuario no corpo do login, apenas o
 * token JWT (que carrega "id" como claim e o e-mail como "sub"). Decodifica
 * o payload localmente para recuperar esses dados sem precisar de outro
 * endpoint.
 */
object JwtUtils {

    fun parsePayload(token: String): JSONObject? {
        return try {
            val partes = token.split(".")
            if (partes.size < 2) return null
            val payloadBytes = Base64.decode(partes[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            JSONObject(String(payloadBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    fun extrairId(token: String): String? = parsePayload(token)?.optString("id", null)

    fun extrairEmail(token: String): String? = parsePayload(token)?.optString("sub", null)
}
