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
 private record Token(String email,java.time.Instant expira){}
 private final Map<String,Token> tokens=new java.util.concurrent.ConcurrentHashMap<>();

 public IdentidadeService(UsuarioRepository r,
         @org.springframework.beans.factory.annotation.Value("${ADMIN_EMAIL:}") String email,
         @org.springframework.beans.factory.annotation.Value("${ADMIN_SENHA:}") String senha) {
     repository = r;
     if (email.isBlank() && senha.isEmpty()) return;
     if (email.isBlank() || !email.contains("@") || senha.length() < 8
             || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
         throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_SENHA válidos");
     }
     var normalizado = email.strip().toLowerCase(Locale.ROOT);
     if (!repository.salvar(new Usuario(UUID.randomUUID().toString(), "Administrador", normalizado,
             encoder.encode(senha), "ADMINISTRADOR", false))
             && !repository.email(normalizado).orElseThrow().administrador()) {
         throw new IllegalStateException("O email administrativo já pertence a um usuário comum");
     }
 }
 public Saida cadastrar(Cadastro d){if(d.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Senha excede 72 bytes");var u=new Usuario(UUID.randomUUID().toString(),d.nome().trim(),d.email().trim().toLowerCase(Locale.ROOT),encoder.encode(d.senha()),"USUARIO",false);if(!repository.salvar(u))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email já cadastrado");return saida(u);}
 public synchronized Sessao login(Login d){var u=repository.email(d.email().trim().toLowerCase(Locale.ROOT)).filter(x->!x.bloqueado()&&encoder.matches(d.senha(),x.senhaHash())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas"));tokens.entrySet().removeIf(e->e.getValue().expira().isBefore(java.time.Instant.now()));var token=UUID.randomUUID().toString();tokens.put(token,new Token(u.email(),java.time.Instant.now().plusSeconds(3600)));return new Sessao(token,saida(u),3600);}
 public Saida validar(String h){var t=h!=null&&h.startsWith("Bearer ")?tokens.get(h.substring(7)):null;if(t==null||!t.expira().isAfter(java.time.Instant.now()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sessão inválida ou expirada");return saida(repository.email(t.email()).filter(u -> !u.bloqueado()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sessão inválida ou conta bloqueada")));}
 private Saida saida(Usuario u){return new Saida(u.id(),u.nome(),u.email(),u.perfil()==null?"USUARIO":u.perfil(),u.bloqueado());}

 public void sair(String token) {
     validar(token);
     tokens.remove(token.substring(7));
 }

 public List<Saida> listar(String token) {
     exigirAdministrador(token);
     return repository.listar().stream().map(this::saida).toList();
 }

 public synchronized Saida bloquear(String token, String id, boolean bloqueado) {
     exigirAdministrador(token);
     var usuario = repository.bloquear(id, bloqueado).orElseThrow(() ->
             new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuário comum não encontrado"));
     if (bloqueado) tokens.entrySet().removeIf(e -> e.getValue().email().equals(usuario.email()));
     return saida(usuario);
 }

 private void exigirAdministrador(String token) {
     if (!"ADMINISTRADOR".equals(validar(token).perfil())) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Operação reservada ao administrador");
     }
 }
}
