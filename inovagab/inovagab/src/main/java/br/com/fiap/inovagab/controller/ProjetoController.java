package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.model.FaseProjeto;
import br.com.fiap.inovagab.model.Projeto;
import br.com.fiap.inovagab.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    @Autowired
    private ProjetoRepository projetoRepository;

    // Gestor cria o projeto com base em uma ideia aprovada
    @PostMapping
    public ResponseEntity<Projeto> criar(@RequestBody Projeto projeto) {
        if (projeto.getInvestimento() == null || projeto.getInvestimento().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        projeto.setFinalizado(false);
        Projeto salvo = projetoRepository.save(projeto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Listar todos os projetos em andamento
    @GetMapping
    public ResponseEntity<List<Projeto>> listarTodos() {
        List<Projeto> projetos = projetoRepository.findAll();
        return ResponseEntity.ok(projetos);
    }

    // Buscar detalhes de um projeto específico
    @GetMapping("/{id}")
    public ResponseEntity<Projeto> buscarPorId(@PathVariable String id) {
        return projetoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualização de fase e ganho financeiro pelo Gestor
    @PutMapping("/{id}/progresso")
    public ResponseEntity<Projeto> atualizarProgresso(
            @PathVariable String id,
            @RequestParam FaseProjeto fase,
            @RequestParam(required = false) BigDecimal retornoFinanceiro,
            @RequestParam(defaultValue = "false") boolean finalizado) {

        return projetoRepository.findById(id).map(projeto -> {
            projeto.setEtapa(fase.name());
            if (retornoFinanceiro != null) {
                projeto.setRetornoFinanceiro(retornoFinanceiro);
            }
            projeto.setFinalizado(finalizado);
            Projeto atualizado = projetoRepository.save(projeto);
            return ResponseEntity.ok(atualizado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Edição completa dos dados cadastrais do projeto pelo Gestor
    @PutMapping("/{id}")
    public ResponseEntity<Projeto> atualizar(@PathVariable String id, @RequestBody Projeto dados) {
        return projetoRepository.findById(id).map(projeto -> {
            projeto.setNome(dados.getNome());
            projeto.setDescricao(dados.getDescricao());
            projeto.setDivisao(dados.getDivisao());
            projeto.setEstrategiaId(dados.getEstrategiaId());
            projeto.setIdeiaOrigemId(dados.getIdeiaOrigemId());
            projeto.setDataInicio(dados.getDataInicio());
            projeto.setPrazoFinal(dados.getPrazoFinal());
            if (dados.getInvestimento() != null) {
                projeto.setInvestimento(dados.getInvestimento());
            }
            return ResponseEntity.ok(projetoRepository.save(projeto));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Remoção definitiva de um projeto pelo Gestor
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        if (!projetoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projetoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
