package com.example.documentos.modelos.dto;
import jakarta.validation.constraints.*;
public class ModeloDtos {
 public record Entrada(@NotBlank @Size(max=100) String nome,@NotBlank @Size(max=20000) String conteudo){}
 public record Revisao(@NotBlank @Size(max=20000) String conteudo){}
 public record Saida(String id,String nome,String conteudo,int versao){}
}
