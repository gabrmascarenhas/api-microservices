package com.example.documentos.identidade.controller;
import com.example.documentos.identidade.service.IdentidadeService;
import com.example.documentos.identidade.client.InternalGuard;
import com.example.documentos.identidade.dto.UsuarioDtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController public class IdentidadeController {
 private final IdentidadeService service;private final InternalGuard guard;
 public IdentidadeController(IdentidadeService s,InternalGuard g){service=s;guard=g;}
 @PostMapping("/api/identidade/usuarios") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Saida cadastrar(@Valid @RequestBody Cadastro d){return service.cadastrar(d);}
 @PostMapping("/api/identidade/login") public Sessao login(@Valid @RequestBody Login d){return service.login(d);}
 @GetMapping("/api/identidade/me") public Saida me(@RequestHeader(value="Authorization",required=false) String token){return service.validar(token);}
 @GetMapping("/interno/sessao") public Saida sessao(@RequestHeader(value="X-Internal-Key",required=false) String key,@RequestHeader(value="Authorization",required=false) String token){guard.check(key);return service.validar(token);}
}
