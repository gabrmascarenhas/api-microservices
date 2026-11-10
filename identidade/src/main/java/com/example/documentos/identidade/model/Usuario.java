package com.example.documentos.identidade.model;

public record Usuario(
    String id,
    String nome,
    String email,
    String senhaHash,
    String perfil,
    boolean bloqueado
) {
    public boolean administrador() {
        return "ADMINISTRADOR".equals(perfil);
    }
}
