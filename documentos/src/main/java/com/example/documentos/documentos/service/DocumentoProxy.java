package com.example.documentos.documentos.service;

import com.example.documentos.documentos.client.ServiceClient;
import com.example.documentos.documentos.model.Documento;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DocumentoProxy {
    private final ConsultaDocumentoReal consulta;
    private final ServiceClient cliente;

    public DocumentoProxy(ConsultaDocumentoReal consulta, ServiceClient cliente) {
        this.consulta = consulta;
        this.cliente = cliente;
    }

    public ConsultaDocumento para(String token, String acao) {
        return id -> autorizar(id, token, acao);
    }

    private Documento autorizar(String id, String token, String acao) {
        cliente.user(token);
        var documento = consulta.buscar(id);
        var resultado = cliente.call("http://acesso/interno/autorizar", HttpMethod.POST,
                new Pedido(documento.id(), documento.proprietarioId(), acao), token, Resultado.class);
        if (!resultado.permitido()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
        return documento;
    }

    private record Pedido(String documentoId, String proprietarioId, String acao) {}
    private record Resultado(boolean permitido, String motivo) {}
}
