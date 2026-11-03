package com.example.documentos.acesso.service;
import com.example.documentos.acesso.client.ServiceClient;import com.example.documentos.acesso.repository.AcessoRepository;import com.example.documentos.acesso.model.Decisao;import com.example.documentos.acesso.dto.AcessoDtos.*;
import org.springframework.stereotype.Service;
@Service public class AcessoService {private final ServiceClient client;private final AcessoRepository repository;public AcessoService(ServiceClient c,AcessoRepository r){client=c;repository=r;}
 public Resultado avaliar(String token,Pedido p){var id=client.user(token);boolean ok=id.equals(p.proprietarioId());repository.registrar(new Decisao(id,p.acao(),ok,java.time.Instant.now()));return new Resultado(ok,ok?"Proprietário autorizado":"Documento pertence a outro usuário");}
}
