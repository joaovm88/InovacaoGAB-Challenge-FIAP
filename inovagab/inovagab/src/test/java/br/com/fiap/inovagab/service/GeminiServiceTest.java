package br.com.fiap.inovagab.service;

import br.com.fiap.inovagab.dto.AnaliseIaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class GeminiServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiService geminiService = new GeminiService(objectMapper);

    @Test
    @DisplayName("Deve acionar fallback seguro quando a chave de API não estiver configurada")
    void avaliarIdeia_deveRetornarFallbackQuandoChaveNula() {
        // Arrange: injeta chave nula simulando ausência de variável de ambiente
        ReflectionTestUtils.setField(geminiService, "apiKey", null);

        // Act
        AnaliseIaDTO resultado = geminiService.avaliarIdeia(
                "Otimização de rotas",
                "Redução de km vazio",
                "Logística",
                "Diretriz Geral ESG"
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(50, resultado.pontuacao());
        assertTrue(resultado.justificativa().contains("Chave de API externa não configurada"));
    }
}