package br.com.fiap.inovagab.service;

import br.com.fiap.inovagab.model.Projeto;
import br.com.fiap.inovagab.repository.ProjetoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    @Test
    @DisplayName("Deve consolidar resultados globais e calcular ROI corretamente (Cenário Positivo)")
    void consolidarResultadosGlobais_deveCalcularRoiCorretamente() {
        // Arrange
        Projeto p1 = new Projeto();
        p1.setInvestimento(BigDecimal.valueOf(100000));
        p1.setRetornoFinanceiro(BigDecimal.valueOf(150000));
        p1.setReducaoCustos(BigDecimal.valueOf(10000));
        p1.setCo2EvitadoToneladas(120.0);
        p1.setAguaPoupadaLitros(450000.0);

        when(projetoRepository.findAll()).thenReturn(List.of(p1));

        // Act
        Map<String, Object> resultado = projetoService.consolidarResultadosGlobais();

        // Assert
        assertEquals(1, resultado.get("totalProjetos"));
        assertEquals(BigDecimal.valueOf(100000), resultado.get("investimentoTotal"));
        assertEquals(BigDecimal.valueOf(150000), resultado.get("retornoTotal"));
        assertEquals(BigDecimal.valueOf(50000), resultado.get("lucroLiquido")); // 150.000 - 100.000
        assertEquals(new BigDecimal("50.00"), resultado.get("roiPercentual")); // (50.000 / 100.000) * 100 = 50%
        assertEquals(120.0, resultado.get("co2EvitadoToneladas"));
        assertEquals(450000.0, resultado.get("aguaPoupadaLitros"));

        verify(projetoRepository, times(1)).findAll();
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