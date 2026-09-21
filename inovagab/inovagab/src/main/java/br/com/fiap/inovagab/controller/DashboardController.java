package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.model.Projeto;
import br.com.fiap.inovagab.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ProjetoRepository projetoRepository;

    @GetMapping("/resultados")
    public ResponseEntity<Map<String, Object>> obterResultadosGlobais() {
        List<Projeto> projetos = projetoRepository.findAll();

        BigDecimal investimentoTotal = BigDecimal.ZERO;
        BigDecimal retornoTotal = BigDecimal.ZERO;
        BigDecimal reducaoCustosTotal = BigDecimal.ZERO;
        double co2Total = 0.0;
        double aguaTotal = 0.0;

        for (Projeto p : projetos) {
            if (p.getInvestimento() != null) {
                investimentoTotal = investimentoTotal.add(p.getInvestimento());
            }
            if (p.getRetornoFinanceiro() != null) {
                retornoTotal = retornoTotal.add(p.getRetornoFinanceiro());
            }
            if (p.getReducaoCustos() != null) {
                reducaoCustosTotal = reducaoCustosTotal.add(p.getReducaoCustos());
            }
            if (p.getCo2EvitadoToneladas() != null) {
                co2Total += p.getCo2EvitadoToneladas();
            }
            if (p.getAguaPoupadaLitros() != null) {
                aguaTotal += p.getAguaPoupadaLitros();
            }
        }

        BigDecimal lucroLiquido = retornoTotal.subtract(investimentoTotal);
        BigDecimal roiGlobal = BigDecimal.ZERO;

        if (investimentoTotal.compareTo(BigDecimal.ZERO) > 0) {
            roiGlobal = lucroLiquido
                    .divide(investimentoTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Map<String, Object> resumo = new HashMap<>();
        resumo.put("totalProjetos", projetos.size());
        resumo.put("investimentoTotal", investimentoTotal);
        resumo.put("retornoTotal", retornoTotal);
        resumo.put("reducaoCustosTotal", reducaoCustosTotal);
        resumo.put("lucroLiquido", lucroLiquido);
        resumo.put("roiPercentual", roiGlobal);
        resumo.put("co2EvitadoToneladas", co2Total);
        resumo.put("aguaPoupadaLitros", aguaTotal);

        return ResponseEntity.ok(resumo);
    }
}