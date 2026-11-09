package com.example.documentos.acesso.controller;

import com.example.documentos.acesso.client.InternalGuard;
import com.example.documentos.acesso.dto.AcessoDtos.*;
import com.example.documentos.acesso.repository.ConcessaoRepository.Solicitacao;
import com.example.documentos.acesso.service.AcessoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class AcessoController {
    private final AcessoService service;
    private final InternalGuard guard;

    public AcessoController(AcessoService service, InternalGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @GetMapping("/api/acesso/status")
    public Map<String, String> status() {
        return Map.of("servico", "acesso", "regra",
                "Proprietário edita; proprietário ou usuário com concessão lê");
    }

    @PostMapping("/interno/autorizar")
    public Resultado avaliar(@RequestHeader(value = "X-Internal-Key", required = false) String chave,
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody Pedido pedido) {
        guard.check(chave);
        return service.avaliar(token, pedido);
    }

    @PostMapping("/interno/documentos/{id}/solicitar")
    public Solicitacao solicitar(@RequestHeader("X-Internal-Key") String chave,
            @RequestHeader("Authorization") String token, @PathVariable String id,
            @Valid @RequestBody ContextoDocumento contexto) {
        guard.check(chave);
        return service.solicitar(token, id, contexto.proprietarioId());
    }

    @PostMapping("/interno/documentos/{id}/solicitacoes")
    public List<Solicitacao> listar(@RequestHeader("X-Internal-Key") String chave,
            @RequestHeader("Authorization") String token, @PathVariable String id,
            @Valid @RequestBody ContextoDocumento contexto) {
        guard.check(chave);
        return service.listar(token, id, contexto.proprietarioId());
    }

    @PostMapping("/interno/documentos/{id}/solicitacoes/{usuario}")
    public Solicitacao decidir(@RequestHeader("X-Internal-Key") String chave,
            @RequestHeader("Authorization") String token, @PathVariable String id,
            @PathVariable String usuario, @RequestParam boolean aprovar,
            @Valid @RequestBody ContextoDocumento contexto) {
        guard.check(chave);
        return service.decidir(token, id, contexto.proprietarioId(), usuario, aprovar);
    }
}
