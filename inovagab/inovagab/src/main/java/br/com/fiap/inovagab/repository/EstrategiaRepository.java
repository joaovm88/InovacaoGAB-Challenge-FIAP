package br.com.fiap.inovagab.repository;

import br.com.fiap.inovagab.model.Estrategia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstrategiaRepository extends MongoRepository<Estrategia, String> {


    List<Estrategia> findByAtivaTrue();
}