package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.model.Estrategia;
import br.com.fiap.inovagab.repository.EstrategiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estrategias")
@RequiredArgsConstructor
public class EstrategiaController {

    private final EstrategiaRepository estrategiaRepository;

    // Apenas LIDER cria diretrizes estratégicas
    @PostMapping
    public ResponseEntity<Estrategia> criar(@RequestBody Estrategia estrategia) {
        Estrategia salva = estrategiaRepository.save(estrategia);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    // Todos os perfis autenticados consultam as diretrizes vigentes
    @GetMapping
    public ResponseEntity<List<Estrategia>> listarAtivas() {
        return ResponseEntity.ok(estrategiaRepository.findByAtivaTrue());
    }

    // Consulta de histórico completo
    @GetMapping("/todas")
    public ResponseEntity<List<Estrategia>> listarTodas() {
        return ResponseEntity.ok(estrategiaRepository.findAll());
    }

    // Encerramento ou reativação de campanha estratégica
    @PatchMapping("/{id}/status")
    public ResponseEntity<Estrategia> alternarStatus(@PathVariable String id, @RequestParam boolean ativa) {
        return estrategiaRepository.findById(id).map(estrategia -> {
            estrategia.setAtiva(ativa);
            return ResponseEntity.ok(estrategiaRepository.save(estrategia));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}