package br.com.fiap.inovacaogab.data

// DTOs espelhando exatamente os campos JSON usados pelo backend Spring Boot
// (br.com.fiap.inovagab). Campos de data/hora e valores monetarios trafegam
// como String/Double para evitar a necessidade de adaptadores Gson extras
// para LocalDate/LocalDateTime/BigDecimal.

data class LoginRequest(
    val email: String,
    val senha: String
)

data class RegistroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val nivelAcesso: String
)

data class TokenResponse(
    val token: String,
    val tipo: String,
    val nivelAcesso: String
)

data class EstrategiaDto(
    val id: String? = null,
    val categoria: String = "",
    val campanha: String = "",
    val descricao: String = "",
    val dataCriacao: String? = null,
    val ativa: Boolean = true
)

data class IdeiaDto(
    val id: String? = null,
    val titulo: String = "",
    val descricao: String = "",
    val setor: String = "",
    val autorId: String? = null,
    val estrategiaId: String? = null,
    val status: String? = null,
    val feedbackGestor: String? = null,
    val pontuacaoIa: Int? = null,
    val justificativaIa: String? = null,
    val dataSubmissao: String? = null
)

data class ProjetoDto(
    val id: String? = null,
    val nome: String = "",
    val descricao: String = "",
    val etapa: String? = null,
    val divisao: String = "",
    val ideiaOrigemId: String? = null,
    val estrategiaId: String? = null,
    val investimento: Double? = null,
    val retornoFinanceiro: Double? = null,
    val reducaoCustos: Double? = null,
    val roi: Double? = null,
    val co2EvitadoToneladas: Double? = null,
    val aguaPoupadaLitros: Double? = null,
    val dataInicio: String? = null,
    val prazoFinal: String? = null,
    val finalizado: Boolean = false
)

data class DashboardResumo(
    val totalProjetos: Int = 0,
    val investimentoTotal: Double = 0.0,
    val retornoTotal: Double = 0.0,
    val reducaoCustosTotal: Double = 0.0,
    val lucroLiquido: Double = 0.0,
    val roiPercentual: Double = 0.0,
    val co2EvitadoToneladas: Double = 0.0,
    val aguaPoupadaLitros: Double = 0.0
)
