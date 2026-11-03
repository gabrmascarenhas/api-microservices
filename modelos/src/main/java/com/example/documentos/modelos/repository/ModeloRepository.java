package com.example.documentos.modelos.repository;
import com.example.documentos.modelos.model.Modelo;
import org.springframework.stereotype.Repository;import java.util.*;
@Repository public class ModeloRepository {
 private final Map<String,Modelo> dados=new java.util.concurrent.ConcurrentHashMap<>();
 public synchronized boolean salvar(Modelo m){if(dados.values().stream().anyMatch(x->x.nome().equalsIgnoreCase(m.nome())))return false;dados.put(m.id(),m);return true;}
 public List<Modelo> listar(){return dados.values().stream().sorted(Comparator.comparing(Modelo::nome)).toList();}
 public Optional<Modelo> buscar(String id){return Optional.ofNullable(dados.get(id));}
}
