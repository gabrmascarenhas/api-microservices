package com.example.documentos.documentos.exportacao;

import com.example.documentos.documentos.model.Documento;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

@Component
public class ExportacaoPdf implements EstrategiaExportacao {

    @Override
    public Arquivo exportar(Documento documento) throws IOException {
        try (
            var pdf = new PDDocument();
            var saida = new ByteArrayOutputStream();
            var recurso = ExportacaoPdf.class.getResourceAsStream("/fonts/DejaVuSans.ttf")
        ) {
            if (recurso == null) {
                throw new IOException("Fonte para exportação PDF indisponível");
            }
            var fonte = PDType0Font.load(pdf, recurso);
            var linhas = new java.util.ArrayList<String>();
            var texto = documento.titulo() + "\n\n" + documento.conteudo();
            for (String linha : texto.split("\\R", -1)) {
                var trecho = new StringBuilder();
                float largura = 0;
                for (int cp : linha.codePoints().toArray()) {
                    String letra = new String(Character.toChars(cp));
                    float medida;
                    try {
                        medida = (fonte.getStringWidth(letra) * 11) / 1000;
                    } catch (IllegalArgumentException e) {
                        letra = "?";
                        medida = (fonte.getStringWidth(letra) * 11) / 1000;
                    }
                    if (largura + medida > 500) {
                        linhas.add(trecho.toString());
                        trecho.setLength(0);
                        largura = 0;
                    }
                    trecho.append(letra);
                    largura += medida;
                }
                linhas.add(trecho.toString());
            }
            for (int inicio = 0; inicio < linhas.size(); inicio += 47) {
                var pagina = new PDPage();
                pdf.addPage(pagina);
                try (var escrita = new PDPageContentStream(pdf, pagina)) {
                    escrita.beginText();
                    escrita.setFont(fonte, 11);
                    escrita.setLeading(15);
                    escrita.newLineAtOffset(48, 744);
                    for (String linha : linhas.subList(
                        inicio,
                        Math.min(inicio + 47, linhas.size())
                    )) {
                        escrita.showText(linha);
                        escrita.newLine();
                    }
                    escrita.endText();
                }
            }
            pdf.save(saida);
            return new Arquivo(saida.toByteArray(), "application/pdf", "pdf");
        }
    }
}
