package com.example.documentos.acesso.service;

import com.example.documentos.acesso.client.ServiceClient;
import com.example.documentos.acesso.dto.AcessoDtos.Pedido;
import com.example.documentos.acesso.dto.AcessoDtos.Resultado;
import java.util.function.BiPredicate;

final class CadeiaAutorizacao {

    record Decisao(String usuario, Resultado resultado) {}

    private record Contexto(String usuario, Pedido pedido) {}

    private interface Etapa {
        Decisao avaliar(String token, Pedido pedido, Contexto contexto);
    }

    private final Etapa inicio;

    CadeiaAutorizacao(ServiceClient cliente, BiPredicate<String, String> temConcessao) {
        var politica = new PoliticaAcesso();
        Etapa regra = (token, pedido, contexto) -> {
            var podeLer =
                pedido.documentoId() != null &&
                temConcessao.test(pedido.documentoId(), contexto.usuario());
            var permitido = politica.avaliar(
                pedido.acao(),
                new PoliticaAcesso.Contexto(contexto.usuario(), pedido.proprietarioId(), podeLer)
            );
            return decidir(
                contexto,
                permitido,
                permitido ? "Operação autorizada" : "Operação negada"
            );
        };
        Etapa validarAcao = (token, pedido, contexto) -> {
            if (!"LER".equals(pedido.acao()) && !"EDITAR".equals(pedido.acao())) {
                return decidir(contexto, false, "Ação desconhecida");
            }
            return regra.avaliar(token, pedido, contexto);
        };
        inicio = (token, pedido, ignorado) -> {
            var contexto = new Contexto(cliente.user(token), pedido);
            return validarAcao.avaliar(token, pedido, contexto);
        };
    }

    Decisao avaliar(String token, Pedido pedido) {
        return inicio.avaliar(token, pedido, null);
    }

    private static Decisao decidir(Contexto contexto, boolean permitido, String motivo) {
        return new Decisao(contexto.usuario(), new Resultado(permitido, motivo));
    }
}
