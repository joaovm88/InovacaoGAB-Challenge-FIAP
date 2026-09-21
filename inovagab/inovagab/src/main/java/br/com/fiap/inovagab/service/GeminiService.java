package br.com.fiap.inovagab.service;

import br.com.fiap.inovagab.dto.AnaliseIaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiService(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    /**
     * Avalia a viabilidade da ideia em relação ao Grupo Águia Branca (Logística, Passageiros, ESG)
     * e devolve uma pontuação entre 0 e 100 com justificativa técnica.
     */
    public AnaliseIaDTO avaliarIdeia(String titulo, String descricao, String setor, String estrategiaTitulo) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("sua-chave-gemini-aqui")) {
            log.warn("Chave da API Gemini não configurada. Aplicando pontuação padrão.");
            return new AnaliseIaDTO(50, "Avaliação de IA pendente: Chave de API externa não configurada no ambiente.");
        }

        String prompt = String.format("""
                Você é um consultor sênior de inovação e ESG do Grupo Águia Branca.
                Avalie a seguinte proposta de melhoria submetida por um operador:
                - Título da Ideia: %s
                - Setor: %s
                - Descrição do Problema e Solução: %s
                - Estratégia Corporativa Vigente: %s

                Responda OBRIGATORIAMENTE E APENAS com um objeto JSON válido no formato exato abaixo, sem marcação markdown ou blocos de código:
                {"pontuacao": 85, "justificativa": "Texto explicativo curto destacando viabilidade, impacto operacional e alinhamento estratégico."}
                A pontuação deve ser um número inteiro de 0 a 100.
                """, titulo, setor, descricao, estrategiaTitulo);

        try {
            // Monta o payload conforme a especificação da API REST do Google Gemini
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String urlComChave = apiUrl + "?key=" + apiKey;

            String responseBody = restClient.post()
                    .uri(urlComChave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Extrai o texto gerado pelo modelo na estrutura candidates[0].content.parts[0].text
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String textoGerado = rootNode
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            // Limpa eventuais marcadores ```json que o modelo possa incluir
            String jsonLimpo = textoGerado
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return objectMapper.readValue(jsonLimpo, AnaliseIaDTO.class);

        } catch (Exception e) {
            log.error("Falha ao consultar API do Gemini: {}", e.getMessage());
            // Fallback elegante: não interrompe o cadastro do operador
            return new AnaliseIaDTO(60, "Avaliação preliminar registrada. Análise automatizada indisponível no momento.");
        }
    }
}