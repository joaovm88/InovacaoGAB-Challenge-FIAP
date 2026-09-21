package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.dto.AnaliseIaDTO;
import br.com.fiap.inovagab.model.IdeiaInovacao;
import br.com.fiap.inovagab.model.StatusIdeia;
import br.com.fiap.inovagab.repository.EstrategiaRepository;
import br.com.fiap.inovagab.repository.IdeiaInovacaoRepository;
import br.com.fiap.inovagab.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ideias")
@RequiredArgsConstructor
public class IdeiaInovacaoController {

    private final IdeiaInovacaoRepository ideiaRepository;
    private final EstrategiaRepository estrategiaRepository;
    private final GeminiService geminiService;

    // Submissão de nova ideia pelo Operador com enriquecimento via IA (Requisito Plus)
    @PostMapping
    public ResponseEntity<IdeiaInovacao> cadastrar(@RequestBody IdeiaInovacao ideia) {
        ideia.setStatus(StatusIdeia.PENDENTE);
        ideia.setDataSubmissao(LocalDateTime.now());

        // Busca o título da estratégia para fornecer contexto à IA
        String tituloEstrategia = "Diretriz Geral do Grupo Águia Branca";
        if (ideia.getEstrategiaId() != null) {
            tituloEstrategia = estrategiaRepository.findById(ideia.getEstrategiaId())
                    .map(e -> e.getCampanha() + " - " + e.getDescricao())
                    .orElse(tituloEstrategia);
        }

        // Executa a análise via Google Gemini
        AnaliseIaDTO analiseIa = geminiService.avaliarIdeia(
                ideia.getTitulo(),
                ideia.getDescricao(),
                ideia.getSetor(),
                tituloEstrategia
        );

        ideia.setPontuacaoIa(analiseIa.pontuacao());
        ideia.setJustificativaIa(analiseIa.justificativa());

        IdeiaInovacao salva = ideiaRepository.save(ideia);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<IdeiaInovacao>> listarPorAutor(@PathVariable String autorId) {
        return ResponseEntity.ok(ideiaRepository.findByAutorId(autorId));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<IdeiaInovacao>> listarPendentes() {
        return ResponseEntity.ok(ideiaRepository.findByStatus(StatusIdeia.PENDENTE));
    }

    @PatchMapping("/{id}/avaliacao")
    public ResponseEntity<IdeiaInovacao> avaliar(
            @PathVariable String id,
            @RequestParam StatusIdeia status,
            @RequestParam(defaultValue = "") String parecer) {
        return ideiaRepository.findById(id).map(ideia -> {
            ideia.setStatus(status);
            ideia.setFeedbackGestor(parecer);
            return ResponseEntity.ok(ideiaRepository.save(ideia));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
