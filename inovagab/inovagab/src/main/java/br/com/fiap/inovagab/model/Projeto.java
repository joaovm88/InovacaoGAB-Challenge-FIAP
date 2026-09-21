package br.com.fiap.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projetos")
public class Projeto {

    @Id
    private String id;

    private String nome;
    private String descricao;
    private String etapa;
    private String divisao;

    // Rastreabilidade do projeto
    private String ideiaOrigemId;
    private String estrategiaId;

    // Métricas para o Dashboard Executivo
    private BigDecimal investimento;
    private BigDecimal retornoFinanceiro;
    private BigDecimal reducaoCustos;
    private Double roi;

    // Métricas de impacto ESG
    private Double co2EvitadoToneladas;
    private Double aguaPoupadaLitros;

    private LocalDate dataInicio;
    private LocalDate prazoFinal;
    private boolean finalizado = false;
}