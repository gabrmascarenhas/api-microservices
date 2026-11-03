package com.example.documentos.documentos.dto;
import jakarta.validation.constraints.*;
public class DocumentoDtos {
 public record Entrada(@NotBlank @Size(max=150) String titulo,@NotBlank String modeloId){}
 public record Saida(String id,String titulo,String conteudo,String proprietarioId,String modeloId,int modeloVersao,java.time.Instant criadoEm){}
}
