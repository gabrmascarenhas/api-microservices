package com.example.documentos.documentos.client;

import com.example.documentos.documentos.criacao.PrototipoDocumento;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class ModeloHttpAdapter implements CatalogoModelos {
    private final ServiceClient cliente;

    public ModeloHttpAdapter(ServiceClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public PrototipoDocumento buscar(String id, String token) {
        var modelo = cliente.call("http://modelos/api/modelos/" + id,
                HttpMethod.GET, null, token, ModeloRemoto.class);
        return new PrototipoDocumento(modelo.id(), modelo.nome(), modelo.conteudo(), modelo.versao());
    }

    private record ModeloRemoto(String id, String nome, String conteudo, int versao) {}
}
