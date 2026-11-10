package com.example.documentos.documentos.repository;

import com.example.documentos.documentos.model.Documento;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentoRepository {

    private final ArquivoRegistros<Documento> arquivo;

    public DocumentoRepository(
        com.fasterxml.jackson.databind.ObjectMapper mapper,
        @org.springframework.beans.factory.annotation.Value(
            "${app.data-dir:.dados/documentos}"
        ) String pasta
    ) {
        arquivo = new ArquivoRegistros<>(mapper, pasta, "documentos.json", Documento.class);
        for (var registro : arquivo.ler()) dados.put(registro.id(), registro);
    }

    private void gravar(Documento registro) {
        var copia = new HashMap<>(dados);
        copia.put(registro.id(), registro);
        arquivo.salvar(copia.values());
        dados.put(registro.id(), registro);
    }

    private final Map<String, Documento> dados = new java.util.concurrent.ConcurrentHashMap<>();

    public synchronized void salvar(Documento d) {
        gravar(d);
    }

    public Optional<Documento> buscar(String id) {
        return Optional.ofNullable(dados.get(id));
    }

    public List<Documento> listar(String usuario) {
        return dados
            .values()
            .stream()
            .filter(d -> d.proprietarioId().equals(usuario))
            .sorted(Comparator.comparing(Documento::criadoEm))
            .toList();
    }
}
