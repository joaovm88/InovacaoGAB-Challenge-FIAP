package br.com.fiap.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "estrategias")
public class Estrategia {

    @Id
    private String id;

    private String categoria;
    private String campanha;
    private String descricao;

    private LocalDate dataCriacao = LocalDate.now();
    private boolean ativa = true;
}