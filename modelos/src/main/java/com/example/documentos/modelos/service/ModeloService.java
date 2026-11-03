package com.example.documentos.modelos.service;
import com.example.documentos.modelos.model.Modelo;import com.example.documentos.modelos.repository.ModeloRepository;import com.example.documentos.modelos.dto.ModeloDtos.*;
import org.springframework.stereotype.Service;import org.springframework.web.server.ResponseStatusException;import org.springframework.http.HttpStatus;import java.util.*;
@Service public class ModeloService {
 private final ModeloRepository repo;public ModeloService(ModeloRepository r){repo=r;}
 public Saida criar(Entrada d){var m=new Modelo(UUID.randomUUID().toString(),d.nome().trim(),d.conteudo(),1);if(!repo.salvar(m))throw new ResponseStatusException(HttpStatus.CONFLICT,"Nome de modelo duplicado");return saida(m);}
 public List<Saida> listar(){return repo.listar().stream().map(this::saida).toList();}
 public Saida buscar(String id){return saida(repo.buscar(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Modelo não encontrado")));}
 private Saida saida(Modelo m){return new Saida(m.id(),m.nome(),m.conteudo(),m.versao());}
}
