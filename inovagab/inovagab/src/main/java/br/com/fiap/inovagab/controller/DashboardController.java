package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.service.ProjetoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjetoService projetoService;

    // Resumo executivo calculado via Aggregation Pipeline do MongoDB (ver ProjetoService)
    @GetMapping("/resultados")
    public ResponseEntity<Map<String, Object>> obterResultadosGlobais() {
        return ResponseEntity.ok(projetoService.consolidarResultadosGlobais());
    }
}
