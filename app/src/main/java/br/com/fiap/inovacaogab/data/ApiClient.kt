package br.com.fiap.inovacaogab.data

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Endereco do backend Spring Boot (modulo inovagab).
 *
 * 10.0.2.2 e o alias que o EMULADOR Android usa para acessar o "localhost"
 * da maquina onde ele roda. Se for testar em um APARELHO FISICO, troque
 * pelo IP da maquina que esta rodando o backend na mesma rede Wi-Fi
 * (ex.: "http://192.168.0.10:8080/") e adicione esse IP em
 * res/xml/network_security_config.xml.
 */
object ApiClient {

    var baseUrl: String = "http://10.0.2.2:8080/"

    private lateinit var sessionManager: SessionManager
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    val service: ApiService
        get() = obterRetrofit().create(ApiService::class.java)

    private fun obterRetrofit(): Retrofit {
        return retrofit ?: construirRetrofit().also { retrofit = it }
    }

    private fun construirRetrofit(): Retrofit {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            sessionManager.getToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Permite trocar o servidor em tempo de execucao (ex.: tela de configuracao) sem reinstalar o app. */
    fun atualizarBaseUrl(novaUrl: String) {
        baseUrl = if (novaUrl.endsWith("/")) novaUrl else "$novaUrl/"
        retrofit = null
    }
}
