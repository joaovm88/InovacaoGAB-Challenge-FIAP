package br.com.fiap.inovagab.service;

import br.com.fiap.inovagab.model.Projeto;
import br.com.fiap.inovagab.repository.ProjetoRepository;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProjetoService projetoService;

    @Test
    @DisplayName("Deve consolidar resultados globais via Aggregation Pipeline e calcular ROI corretamente (Cenário Positivo)")
    void consolidarResultadosGlobais_deveCalcularRoiCorretamente() {
        // Arrange: simula o documento já agregado ($group) que o MongoDB devolveria
        Document resultadoAgregacao = new Document();
        resultadoAgregacao.put("totalProjetos", 1);
        resultadoAgregacao.put("investimentoTotal", 100000);
        resultadoAgregacao.put("retornoTotal", 150000);
        resultadoAgregacao.put("reducaoCustosTotal", 10000);
        resultadoAgregacao.put("co2EvitadoToneladas", 120.0);
        resultadoAgregacao.put("aguaPoupadaLitros", 450000.0);

        AggregationResults<Document> resultados =
                new AggregationResults<>(List.of(resultadoAgregacao), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("projetos"), eq(Document.class)))
                .thenReturn(resultados);

        // Act
        Map<String, Object> resultado = projetoService.consolidarResultadosGlobais();

        // Assert
        assertEquals(1, resultado.get("totalProjetos"));
        assertEquals(0, new BigDecimal("100000").compareTo((BigDecimal) resultado.get("investimentoTotal")));
        assertEquals(0, new BigDecimal("150000").compareTo((BigDecimal) resultado.get("retornoTotal")));
        assertEquals(0, new BigDecimal("50000").compareTo((BigDecimal) resultado.get("lucroLiquido"))); // 150.000 - 100.000
        assertEquals(0, new BigDecimal("50.00").compareTo((BigDecimal) resultado.get("roiPercentual"))); // (50.000 / 100.000) * 100 = 50%
        assertEquals(120.0, resultado.get("co2EvitadoToneladas"));
        assertEquals(450000.0, resultado.get("aguaPoupadaLitros"));
    }

    @Test
    @DisplayName("Deve devolver zeros quando não há projetos cadastrados (coleção vazia)")
    void consolidarResultadosGlobais_deveDevolverZerosQuandoColecaoVazia() {
        // Arrange: $group não produz nenhum documento de saída para uma coleção vazia
        AggregationResults<Document> resultadosVazios =
                new AggregationResults<>(List.of(), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("projetos"), eq(Document.class)))
                .thenReturn(resultadosVazios);

        // Act
        Map<String, Object> resultado = projetoService.consolidarResultadosGlobais();

        // Assert
        assertEquals(0, resultado.get("totalProjetos"));
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) resultado.get("investimentoTotal")));
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) resultado.get("roiPercentual")));
        assertEquals(0.0, resultado.get("co2EvitadoToneladas"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o investimento for nulo ou menor igual a zero (Cenário Negativo)")
    void criarProjeto_deveLancarExcecaoQuandoInvestimentoInvalido() {
        // Arrange
        Projeto projetoInvalido = new Projeto();
        projetoInvalido.setInvestimento(BigDecimal.ZERO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projetoService.criarProjeto(projetoInvalido);
        });

        assertEquals("O investimento inicial deve ser maior que zero.", exception.getMessage());
        verify(projetoRepository, never()).save(any(Projeto.class));
    }
}
