package com.example.documentos.documentos.dto;
import com.example.documentos.documentos.criacao.CriadorDocumento.Tipo;
import jakarta.validation.constraints.*;
public class DocumentoDtos {
 public record Entrada(@NotBlank @Size(max=150) String titulo,@NotBlank @Pattern(regexp="[a-f0-9-]{36}") String modeloId, Tipo tipo){}
 public record Edicao(@NotBlank @Size(max=150) String titulo,@NotBlank @Size(max=21000) String conteudo){}
 public record Saida(String id,String titulo,String conteudo,String proprietarioId,String modeloId,int modeloVersao,java.time.Instant criadoEm){}
}
