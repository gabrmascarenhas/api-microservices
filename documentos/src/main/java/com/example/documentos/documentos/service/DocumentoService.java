package com.example.documentos.documentos.service;
import com.example.documentos.documentos.client.ServiceClient;import com.example.documentos.documentos.repository.DocumentoRepository;import com.example.documentos.documentos.model.Documento;import com.example.documentos.documentos.dto.DocumentoDtos.*;
import org.springframework.stereotype.Service;import org.springframework.http.*;import org.springframework.web.server.ResponseStatusException;import java.util.*;
@Service public class DocumentoService {
 private final DocumentoRepository repo;private final ServiceClient client;public DocumentoService(DocumentoRepository r,ServiceClient c){repo=r;client=c;}
 public record Modelo(String id,String nome,String conteudo,int versao){}
 public record Pedido(String proprietarioId,String acao){}
 public record Resultado(boolean permitido,String motivo){}
 public Saida criar(String token,Entrada e){String usuario=client.user(token);var m=client.call("http://modelos/api/modelos/"+e.modeloId(),HttpMethod.GET,null,token,Modelo.class);var d=new Documento(UUID.randomUUID().toString(),e.titulo().trim(),m.conteudo(),usuario,m.id(),m.versao(),java.time.Instant.now());repo.salvar(d);return saida(d);}
 public List<Saida> listar(String token){return repo.listar(client.user(token)).stream().map(this::saida).toList();}
 public Saida buscar(String token,String id){client.user(token);var d=repo.buscar(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Documento não encontrado"));var r=client.call("http://acesso/interno/autorizar",HttpMethod.POST,new Pedido(d.proprietarioId(),"LER"),token,Resultado.class);if(!r.permitido())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Acesso negado");return saida(d);}
 private Saida saida(Documento d){return new Saida(d.id(),d.titulo(),d.conteudo(),d.proprietarioId(),d.modeloId(),d.modeloVersao(),d.criadoEm());}
}
