package com.example.documentos.documentos.exportacao;

import com.example.documentos.documentos.model.Documento;
import java.io.IOException;

public interface EstrategiaExportacao {
    Arquivo exportar(Documento documento) throws IOException;

    record Arquivo(byte[] dados, String tipo, String extensao) {}
}
