package com.example.documentos.documentos.client;

import com.example.documentos.documentos.criacao.PrototipoDocumento;

public interface CatalogoModelos {
    PrototipoDocumento buscar(String id, String token);
}
