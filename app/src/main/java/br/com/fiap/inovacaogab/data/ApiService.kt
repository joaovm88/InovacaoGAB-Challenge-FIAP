package br.com.fiap.inovacaogab.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body dados: LoginRequest): Response<TokenResponse>

    @POST("api/auth/registro")
    suspend fun registrar(@Body dados: RegistroRequest): Response<Unit>

    @GET("api/estrategias")
    suspend fun listarEstrategiasAtivas(): Response<List<EstrategiaDto>>

    @POST("api/estrategias")
    suspend fun criarEstrategia(@Body dados: EstrategiaDto): Response<EstrategiaDto>

    @PUT("api/estrategias/{id}")
    suspend fun atualizarEstrategia(@Path("id") id: String, @Body dados: EstrategiaDto): Response<EstrategiaDto>

    @DELETE("api/estrategias/{id}")
    suspend fun excluirEstrategia(@Path("id") id: String): Response<Unit>

    @POST("api/ideias")
    suspend fun criarIdeia(@Body dados: IdeiaDto): Response<IdeiaDto>

    @GET("api/ideias/autor/{autorId}")
    suspend fun listarIdeiasPorAutor(@Path("autorId") autorId: String): Response<List<IdeiaDto>>

    @GET("api/ideias/pendentes")
    suspend fun listarIdeiasPendentes(): Response<List<IdeiaDto>>

    @PATCH("api/ideias/{id}/avaliacao")
    suspend fun avaliarIdeia(
        @Path("id") id: String,
        @Query("status") status: String,
        @Query("parecer") parecer: String
    ): Response<IdeiaDto>

    @PUT("api/ideias/{id}")
    suspend fun atualizarIdeia(@Path("id") id: String, @Body dados: IdeiaDto): Response<IdeiaDto>

    @DELETE("api/ideias/{id}")
    suspend fun excluirIdeia(@Path("id") id: String): Response<Unit>

    @GET("api/projetos")
    suspend fun listarProjetos(): Response<List<ProjetoDto>>

    @POST("api/projetos")
    suspend fun criarProjeto(@Body dados: ProjetoDto): Response<ProjetoDto>

    @PUT("api/projetos/{id}/progresso")
    suspend fun atualizarProgressoProjeto(
        @Path("id") id: String,
        @Query("fase") fase: String,
        @Query("retornoFinanceiro") retornoFinanceiro: Double?,
        @Query("finalizado") finalizado: Boolean
    ): Response<ProjetoDto>

    @DELETE("api/projetos/{id}")
    suspend fun excluirProjeto(@Path("id") id: String): Response<Unit>

    @GET("api/dashboard/resultados")
    suspend fun obterResultadosDashboard(): Response<DashboardResumo>
}
