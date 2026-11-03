package com.example.documentos.identidade.dto;
import jakarta.validation.constraints.*;
public class UsuarioDtos {
 public record Cadastro(@NotBlank @Size(max=100) String nome,@NotBlank @Email @Size(max=200) String email,@NotBlank @Size(min=8,max=72) String senha){}
 public record Login(@NotBlank @Email String email,@NotBlank String senha){}
 public record Saida(String id,String nome,String email){}
 public record Sessao(String token,Saida usuario,long expiraEmSegundos){}
}
