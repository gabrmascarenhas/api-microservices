package com.example.documentos.acesso.service;

import java.util.function.Predicate;

/** Interpreta as expressões de leitura e edição sobre o contexto de acesso. */
public final class PoliticaAcesso {

    public record Contexto(String usuario, String proprietario, boolean leituraConcedida) {}

    private interface Expressao extends Predicate<Contexto> {}

    private record Proprietario() implements Expressao {
        public boolean test(Contexto contexto) {
            return contexto.usuario().equals(contexto.proprietario());
        }
    }

    private record Concessao() implements Expressao {
        public boolean test(Contexto contexto) {
            return contexto.leituraConcedida();
        }
    }

    private record Ou(Expressao esquerda, Expressao direita) implements Expressao {
        public boolean test(Contexto contexto) {
            return esquerda.test(contexto) || direita.test(contexto);
        }
    }

    private static final Expressao EDITAR = new Proprietario();
    private static final Expressao LER = new Ou(EDITAR, new Concessao());

    public boolean avaliar(String acao, Contexto contexto) {
        return switch (acao) {
            case "LER" -> LER.test(contexto);
            case "EDITAR" -> EDITAR.test(contexto);
            default -> false;
        };
    }
}
