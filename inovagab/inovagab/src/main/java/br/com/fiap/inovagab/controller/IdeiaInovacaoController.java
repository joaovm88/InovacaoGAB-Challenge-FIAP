package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.dto.AnaliseIaDTO;
import br.com.fiap.inovagab.model.IdeiaInovacao;
import br.com.fiap.inovagab.model.NivelAcesso;
import br.com.fiap.inovagab.model.StatusIdeia;
import br.com.fiap.inovagab.model.Usuario;
import br.com.fiap.inovagab.repository.EstrategiaRepository;
import br.com.fiap.inovagab.repository.IdeiaInovacaoRepository;
import br.com.fiap.inovagab.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // Edição da própria ideia pelo operador autor, ou por gestor/lider
    @PutMapping("/{id}")
    public ResponseEntity<IdeiaInovacao> atualizar(
            @PathVariable String id,
            @RequestBody IdeiaInovacao dados,
            Authentication authentication) {
        return ideiaRepository.findById(id).map(ideia -> {
            if (!podeGerenciar(ideia, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<IdeiaInovacao>build();
            }
            ideia.setTitulo(dados.getTitulo());
            ideia.setDescricao(dados.getDescricao());
            ideia.setSetor(dados.getSetor());
            return ResponseEntity.ok(ideiaRepository.save(ideia));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Remoção da própria ideia pelo operador autor, ou por gestor/lider
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id, Authentication authentication) {
        return ideiaRepository.findById(id).map(ideia -> {
            if (!podeGerenciar(ideia, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            }
            ideiaRepository.deleteById(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Somente o autor da ideia ou perfis GESTOR/LIDER podem editar ou excluir
    private boolean podeGerenciar(IdeiaInovacao ideia, Authentication authentication) {
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        boolean isAutor = usuarioLogado.getId().equals(ideia.getAutorId());
        boolean isGestorOuLider = usuarioLogado.getNivelAcesso() != NivelAcesso.OPERADOR;
        return isAutor || isGestorOuLider;
    }
}
