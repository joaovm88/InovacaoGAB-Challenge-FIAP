package br.com.fiap.inovagab.repository;

import br.com.fiap.inovagab.model.IdeiaInovacao;
import br.com.fiap.inovagab.model.StatusIdeia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdeiaInovacaoRepository extends MongoRepository<IdeiaInovacao, String> {


    List<IdeiaInovacao> findByAutorId(String autorId);


    List<IdeiaInovacao> findByStatus(StatusIdeia status);


    List<IdeiaInovacao> findByEstrategiaId(String estrategiaId);
}