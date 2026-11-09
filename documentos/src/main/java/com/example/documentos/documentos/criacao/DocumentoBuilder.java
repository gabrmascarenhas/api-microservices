package com.example.documentos.documentos.criacao;

import com.example.documentos.documentos.model.Documento;
import java.time.Instant;
import java.util.UUID;

public final class DocumentoBuilder {
    private final PrototipoDocumento.Rascunho rascunho;
    private final String proprietario;
    private String titulo;
    private String conteudo;

    public DocumentoBuilder(PrototipoDocumento.Rascunho rascunho, String proprietario) {
        this.rascunho = rascunho;
        this.proprietario = proprietario;
        this.conteudo = rascunho.conteudo();
    }

    public DocumentoBuilder titulo(String titulo) {
        this.titulo = titulo.strip();
        return this;
    }

    public DocumentoBuilder conteudo(String conteudo) {
        this.conteudo = conteudo;
        return this;
    }

    public Documento construir() {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalStateException("O documento precisa de um título");
        }
        return new Documento(UUID.randomUUID().toString(), titulo, conteudo, proprietario,
                rascunho.modeloId(), rascunho.modeloVersao(), Instant.now());
    }
}
