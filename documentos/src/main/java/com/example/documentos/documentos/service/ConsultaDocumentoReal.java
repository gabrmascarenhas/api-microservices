package com.example.documentos.documentos.service;

import com.example.documentos.documentos.model.Documento;
import com.example.documentos.documentos.repository.DocumentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ConsultaDocumentoReal implements ConsultaDocumento {

    private final DocumentoRepository repositorio;

    public ConsultaDocumentoReal(DocumentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Documento buscar(String id) {
        return repositorio
            .buscar(id)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado")
            );
    }
}
