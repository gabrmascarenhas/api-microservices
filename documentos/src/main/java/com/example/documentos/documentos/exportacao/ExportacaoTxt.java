package com.example.documentos.documentos.exportacao;

import com.example.documentos.documentos.model.Documento;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class ExportacaoTxt implements EstrategiaExportacao {

    @Override
    public Arquivo exportar(Documento documento) {
        var texto = documento.titulo() + "\n\n" + documento.conteudo();
        return new Arquivo(
            texto.getBytes(StandardCharsets.UTF_8),
            "text/plain;charset=UTF-8",
            "txt"
        );
    }
}
