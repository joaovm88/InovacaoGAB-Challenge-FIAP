package br.com.fiap.inovagab.repository;

import br.com.fiap.inovagab.model.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoRepository extends MongoRepository<Projeto, String> {


    List<Projeto> findByFinalizado(boolean finalizado);


    List<Projeto> findByEstrategiaId(String estrategiaId);


    List<Projeto> findByDivisao(String divisao);
}