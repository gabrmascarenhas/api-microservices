package com.example.documentos.modelos.controller;

import com.example.documentos.modelos.client.ServiceClient;
import com.example.documentos.modelos.dto.ModeloDtos.*;
import com.example.documentos.modelos.service.ModeloService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modelos")
public class ModeloController {

    private final ModeloService service;
    private final ServiceClient client;

    public ModeloController(ModeloService s, ServiceClient c) {
        service = s;
        client = c;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Saida criar(
        @RequestHeader(value = "Authorization", required = false) String token,
        @Valid @RequestBody Entrada d
    ) {
        client.user(token);
        return service.criar(d);
    }

    @GetMapping
    public List<Saida> listar(
        @RequestHeader(value = "Authorization", required = false) String token
    ) {
        client.user(token);
        return service.listar();
    }

    @GetMapping("/{id}")
    public Saida buscar(
        @PathVariable String id,
        @RequestHeader(value = "Authorization", required = false) String token
    ) {
        client.user(token);
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Saida revisar(
        @PathVariable String id,
        @RequestHeader(value = "Authorization", required = false) String token,
        @Valid @RequestBody Revisao revisao
    ) {
        client.user(token);
        return service.revisar(id, revisao);
    }
}
