package com.example.documentos.documentos.repository;
import com.example.documentos.documentos.model.Documento;import org.springframework.stereotype.Repository;import java.util.*;
@Repository public class DocumentoRepository {
 private final Map<String,Documento> dados=new java.util.concurrent.ConcurrentHashMap<>();public void salvar(Documento d){dados.put(d.id(),d);}
 public Optional<Documento> buscar(String id){return Optional.ofNullable(dados.get(id));}
 public List<Documento> listar(String usuario){return dados.values().stream().filter(d->d.proprietarioId().equals(usuario)).sorted(Comparator.comparing(Documento::criadoEm)).toList();}
}
