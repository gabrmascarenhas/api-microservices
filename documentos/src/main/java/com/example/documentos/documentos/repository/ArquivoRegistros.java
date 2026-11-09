package com.example.documentos.documentos.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ArquivoRegistros<T> {
    private final ObjectMapper mapper;
    private final Path arquivo;
    private final Class<T> tipo;

    ArquivoRegistros(ObjectMapper mapper, String pasta, String nome, Class<T> tipo) {
        this.mapper = mapper;
        this.arquivo = Path.of(pasta).toAbsolutePath().resolve(nome);
        this.tipo = tipo;
    }

    List<T> ler() {
        if (Files.notExists(arquivo)) return new ArrayList<>();
        try {
            return mapper.readValue(arquivo.toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, tipo));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + arquivo, e);
        }
    }

    void salvar(Collection<T> registros) {
        try {
            Files.createDirectories(arquivo.getParent());
            var temporario = Files.createTempFile(arquivo.getParent(), "registros-", ".tmp");
            try {
                mapper.writeValue(temporario.toFile(), registros);
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temporario);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar " + arquivo, e);
        }
    }
}
