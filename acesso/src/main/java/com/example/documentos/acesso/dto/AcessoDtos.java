package com.example.documentos.acesso.dto;
import jakarta.validation.constraints.*;
public class AcessoDtos {
 public record Pedido(@NotBlank String proprietarioId,@NotBlank @Pattern(regexp="LER|EDITAR") String acao){}
 public record Resultado(boolean permitido,String motivo){}
}
