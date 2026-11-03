package com.example.documentos.identidade.service;
import com.example.documentos.identidade.model.Usuario;
import com.example.documentos.identidade.repository.UsuarioRepository;
import com.example.documentos.identidade.dto.UsuarioDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service public class IdentidadeService {
 private final UsuarioRepository repository;private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
 private record Token(Usuario usuario,java.time.Instant expira){}
 private final Map<String,Token> tokens=new java.util.concurrent.ConcurrentHashMap<>();
 public IdentidadeService(UsuarioRepository r){repository=r;}
 public Saida cadastrar(Cadastro d){var u=new Usuario(UUID.randomUUID().toString(),d.nome().trim(),d.email().trim().toLowerCase(Locale.ROOT),encoder.encode(d.senha()));if(!repository.salvar(u))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email já cadastrado");return saida(u);}
 public Sessao login(Login d){var u=repository.email(d.email().trim().toLowerCase(Locale.ROOT)).filter(x->encoder.matches(d.senha(),x.senhaHash())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas"));tokens.entrySet().removeIf(e->e.getValue().expira().isBefore(java.time.Instant.now()));var token=UUID.randomUUID().toString();tokens.put(token,new Token(u,java.time.Instant.now().plusSeconds(3600)));return new Sessao(token,saida(u),3600);}
 public Saida validar(String h){var t=h!=null&&h.startsWith("Bearer ")?tokens.get(h.substring(7)):null;if(t==null||!t.expira().isAfter(java.time.Instant.now()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sessão inválida ou expirada");return saida(t.usuario());}
 private Saida saida(Usuario u){return new Saida(u.id(),u.nome(),u.email());}
}
