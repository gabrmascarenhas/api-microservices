package com.example.documentos.acesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AcessoDtos {

    public record Pedido(
        String documentoId,
        @NotBlank String proprietarioId,
        @NotBlank @Pattern(regexp = "LER|EDITAR") String acao
    ) {}

    public record ContextoDocumento(@NotBlank String proprietarioId) {}

    public record Resultado(boolean permitido, String motivo) {}
}
