package com.example.documentos.documentos.criacao;

/** Modelo imutável que produz uma cópia de trabalho por documento. */
public record PrototipoDocumento(String id, String nome, String conteudo, int versao) {
    public Rascunho copiar() {
        return new Rascunho(id, versao, conteudo);
    }

    public record Rascunho(String modeloId, int modeloVersao, String conteudo) {}
}
