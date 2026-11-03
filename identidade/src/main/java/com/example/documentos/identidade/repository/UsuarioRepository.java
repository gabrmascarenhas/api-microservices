package com.example.documentos.identidade.repository;
import com.example.documentos.identidade.model.Usuario;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository public class UsuarioRepository {
 private final Map<String,Usuario> usuarios=new java.util.concurrent.ConcurrentHashMap<>();
 public synchronized boolean salvar(Usuario u){return usuarios.putIfAbsent(u.email(),u)==null;}
 public Optional<Usuario> email(String email){return Optional.ofNullable(usuarios.get(email));}
}
