package br.com.fiap.inovagab.service;

import br.com.fiap.inovagab.model.Projeto;
import br.com.fiap.inovagab.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final MongoTemplate mongoTemplate;

    public Projeto criarProjeto(Projeto projeto) {
        if (projeto.getInvestimento() == null || projeto.getInvestimento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O investimento inicial deve ser maior que zero.");
        }
        projeto.setFinalizado(false);
        return projetoRepository.save(projeto);
    }

    // Consolida os indicadores executivos via Aggregation Pipeline do MongoDB
    // ($group), sem carregar a coleção inteira de projetos na memória do backend.
    public Map<String, Object> consolidarResultadosGlobais() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group()
                        .count().as("totalProjetos")
                        .sum("investimento").as("investimentoTotal")
                        .sum("retornoFinanceiro").as("retornoTotal")
                        .sum("reducaoCustos").as("reducaoCustosTotal")
                        .sum("co2EvitadoToneladas").as("co2EvitadoToneladas")
                        .sum("aguaPoupadaLitros").as("aguaPoupadaLitros")
        );

        Document resultado = mongoTemplate
                .aggregate(aggregation, "projetos", Document.class)
                .getUniqueMappedResult();

        int totalProjetos = extrairInt(resultado, "totalProjetos");
        BigDecimal investimentoTotal = extrairBigDecimal(resultado, "investimentoTotal");
        BigDecimal retornoTotal = extrairBigDecimal(resultado, "retornoTotal");
        BigDecimal reducaoCustosTotal = extrairBigDecimal(resultado, "reducaoCustosTotal");
        double co2Total = extrairDouble(resultado, "co2EvitadoToneladas");
        double aguaTotal = extrairDouble(resultado, "aguaPoupadaLitros");

        BigDecimal lucroLiquido = retornoTotal.subtract(investimentoTotal);
        BigDecimal roiGlobal = BigDecimal.ZERO;

        if (investimentoTotal.compareTo(BigDecimal.ZERO) > 0) {
            roiGlobal = lucroLiquido
                    .divide(investimentoTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Map<String, Object> resumo = new HashMap<>();
        resumo.put("totalProjetos", totalProjetos);
        resumo.put("investimentoTotal", investimentoTotal);
        resumo.put("retornoTotal", retornoTotal);
        resumo.put("reducaoCustosTotal", reducaoCustosTotal);
        resumo.put("lucroLiquido", lucroLiquido);
        resumo.put("roiPercentual", roiGlobal);
        resumo.put("co2EvitadoToneladas", co2Total);
        resumo.put("aguaPoupadaLitros", aguaTotal);

        return resumo;
    }

    // A coleção pode estar vazia (nenhum projeto cadastrado ainda), caso em que o
    // $group não produz nenhum documento de saída — por isso os extratores abaixo
    // toleram um Document nulo ou um campo ausente, devolvendo o valor zero.
    private int extrairInt(Document doc, String campo) {
        if (doc == null) return 0;
        Object valor = doc.get(campo);
        return valor instanceof Number ? ((Number) valor).intValue() : 0;
    }

    private BigDecimal extrairBigDecimal(Document doc, String campo) {
        if (doc == null) return BigDecimal.ZERO;
        Object valor = doc.get(campo);
        if (valor instanceof Decimal128) {
            return ((Decimal128) valor).bigDecimalValue();
        }
        if (valor instanceof Number) {
            return BigDecimal.valueOf(((Number) valor).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private double extrairDouble(Document doc, String campo) {
        if (doc == null) return 0.0;
        Object valor = doc.get(campo);
        if (valor instanceof Decimal128) {
            return ((Decimal128) valor).doubleValue();
        }
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return 0.0;
    }
}
