package com.example.documentos.acesso.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class ConcessaoRepository {
    public record Solicitacao(String documentoId, String proprietarioId, String solicitanteId,
                              String status, Instant atualizadaEm) {}
    private record Chave(String documento, String solicitante) {}
    private final Map<Chave, Solicitacao> dados = new HashMap<>();
    private final ArquivoRegistros<Solicitacao> arquivo;

    public ConcessaoRepository(ObjectMapper mapper, @Value("${app.data-dir:.dados/acesso}") String pasta) {
        arquivo = new ArquivoRegistros<>(mapper, pasta, "concessoes.json", Solicitacao.class);
        for (var item : arquivo.ler()) dados.put(chave(item), item);
    }

    public synchronized Solicitacao solicitar(String documento, String proprietario, String usuario) {
        var chave = new Chave(documento, usuario);
        var anterior = dados.get(chave);
        if (anterior != null && !"RECUSADA".equals(anterior.status())) return anterior;
        return salvar(new Solicitacao(documento, proprietario, usuario, "PENDENTE", Instant.now()));
    }

    public synchronized Solicitacao decidir(String documento, String usuario, boolean aprovar) {
        var anterior = dados.get(new Chave(documento, usuario));
        if (anterior == null) return null;
        return salvar(new Solicitacao(documento, anterior.proprietarioId(), usuario,
                aprovar ? "APROVADA" : "RECUSADA", Instant.now()));
    }

    public synchronized List<Solicitacao> listar(String documento) {
        return dados.values().stream().filter(item -> item.documentoId().equals(documento))
                .sorted(java.util.Comparator.comparing(Solicitacao::atualizadaEm)).toList();
    }

    public synchronized boolean permiteLeitura(String documento, String usuario) {
        var item = dados.get(new Chave(documento, usuario));
        return item != null && "APROVADA".equals(item.status());
    }

    private Solicitacao salvar(Solicitacao item) {
        var copia = new HashMap<>(dados);
        copia.put(chave(item), item);
        arquivo.salvar(copia.values());
        dados.put(chave(item), item);
        return item;
    }

    private static Chave chave(Solicitacao item) {
        return new Chave(item.documentoId(), item.solicitanteId());
    }
}
