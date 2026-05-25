package br.com.fiap.inovacaogab.model

data class IdeiaInovacao(
    var id: String = "",
    var autorId: String = "",
    val tituloIdeia: String = "",
    val setor: String = "",
    val descricao: String = "",
    val dataRegistro: String = "",
    val foiAprovada: Boolean = false
){
    fun toJson(): Map<String, Any> =
        mapOf(
            "id" to id,
            "autorId" to autorId,
            "tituloIdeia" to tituloIdeia,
            "Setor" to setor,
            "descricao" to descricao,
            "dataRegistro" to dataRegistro,
            "foiAprovada" to foiAprovada
        )
}
