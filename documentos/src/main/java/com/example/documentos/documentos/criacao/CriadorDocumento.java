package com.example.documentos.documentos.criacao;

import com.example.documentos.documentos.model.Documento;

public abstract class CriadorDocumento {

    public enum Tipo {
        RELATORIO,
        CONTRATO,
    }

    public static CriadorDocumento para(Tipo tipo) {
        return tipo == Tipo.CONTRATO ? new CriadorContrato() : new CriadorRelatorio();
    }

    public final Documento criar(PrototipoDocumento modelo, String titulo, String proprietario) {
        var rascunho = modelo.copiar();
        return preparar(rascunho, proprietario).titulo(titulo).construir();
    }

    protected abstract DocumentoBuilder preparar(
        PrototipoDocumento.Rascunho rascunho,
        String proprietario
    );

    private static final class CriadorRelatorio extends CriadorDocumento {

        @Override
        protected DocumentoBuilder preparar(
            PrototipoDocumento.Rascunho rascunho,
            String proprietario
        ) {
            return new DocumentoBuilder(rascunho, proprietario);
        }
    }

    private static final class CriadorContrato extends CriadorDocumento {

        @Override
        protected DocumentoBuilder preparar(
            PrototipoDocumento.Rascunho rascunho,
            String proprietario
        ) {
            return new DocumentoBuilder(rascunho, proprietario).conteudo(
                rascunho.conteudo() + "\n\nAssinaturas:\n____________________\n____________________"
            );
        }
    }
}
