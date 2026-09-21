package br.com.fiap.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ideias")
public class IdeiaInovacao {

    @Id
    private String id;

    private String titulo;
    private String descricao;
    private String setor;

    // Referências aos outros documentos
    private String autorId;
    private String estrategiaId;

    private StatusIdeia status = StatusIdeia.PENDENTE;
    private String feedbackGestor;

    // Campos preparados para receber a análise da Inteligência Artificial
    private Integer pontuacaoIa;
    private String justificativaIa;

    private LocalDateTime dataSubmissao = LocalDateTime.now();
}