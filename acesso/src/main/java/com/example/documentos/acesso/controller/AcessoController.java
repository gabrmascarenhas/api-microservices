package com.example.documentos.acesso.controller;
import com.example.documentos.acesso.service.AcessoService;import com.example.documentos.acesso.client.InternalGuard;import com.example.documentos.acesso.dto.AcessoDtos.*;
import org.springframework.web.bind.annotation.*;import jakarta.validation.Valid;
@RestController public class AcessoController {private final AcessoService service;private final InternalGuard guard;public AcessoController(AcessoService s,InternalGuard g){service=s;guard=g;}
 @GetMapping("/api/acesso/status") public java.util.Map<String,String> status(){return java.util.Map.of("servico","acesso","regraMvp","Somente proprietário pode ler e editar documentos");}
 @PostMapping("/interno/autorizar") public Resultado avaliar(@RequestHeader(value="X-Internal-Key",required=false) String key,@RequestHeader(value="Authorization",required=false) String token,@Valid @RequestBody Pedido p){guard.check(key);return service.avaliar(token,p);}
}
