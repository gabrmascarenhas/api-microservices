package com.example.documentos.modelos.repository;

import com.example.documentos.modelos.model.Modelo;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class ModeloRepository {

    private final ArquivoRegistros<Modelo> arquivo;

    public ModeloRepository(
        com.fasterxml.jackson.databind.ObjectMapper mapper,
        @org.springframework.beans.factory.annotation.Value(
            "${app.data-dir:.dados/modelos}"
        ) String pasta
    ) {
        arquivo = new ArquivoRegistros<>(mapper, pasta, "modelos.json", Modelo.class);
        for (var registro : arquivo.ler()) dados.put(registro.id(), registro);
    }

    private void gravar(Modelo registro) {
        var copia = new HashMap<>(dados);
        copia.put(registro.id(), registro);
        arquivo.salvar(copia.values());
        dados.put(registro.id(), registro);
    }

    private final Map<String, Modelo> dados = new java.util.concurrent.ConcurrentHashMap<>();

    public synchronized boolean salvar(Modelo m) {
        if (
            dados
                .values()
                .stream()
                .anyMatch(x -> x.nome().equalsIgnoreCase(m.nome()))
        ) return false;
        gravar(m);
        return true;
    }

    public List<Modelo> listar() {
        return dados.values().stream().sorted(Comparator.comparing(Modelo::nome)).toList();
    }

    public Optional<Modelo> buscar(String id) {
        return Optional.ofNullable(dados.get(id));
    }

    public synchronized Optional<Modelo> revisar(String id, String conteudo) {
        var atual = dados.get(id);
        if (atual == null) return Optional.empty();
        var revisado = new Modelo(id, atual.nome(), conteudo, atual.versao() + 1);
        gravar(revisado);
        return Optional.of(revisado);
    }
}
