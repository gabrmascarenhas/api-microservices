package com.example.documentos.documentos.service;

import com.example.documentos.documentos.client.CatalogoModelos;
import com.example.documentos.documentos.client.ServiceClient;
import com.example.documentos.documentos.criacao.CriadorDocumento;
import com.example.documentos.documentos.dto.DocumentoDtos.*;
import com.example.documentos.documentos.exportacao.EstrategiaExportacao.Arquivo;
import com.example.documentos.documentos.exportacao.ExportacaoPdf;
import com.example.documentos.documentos.exportacao.ExportacaoTxt;
import com.example.documentos.documentos.model.Documento;
import com.example.documentos.documentos.repository.DocumentoRepository;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Fachada das operações de criação, consulta, edição e exportação. */
@Service
public class DocumentoService {
    private final DocumentoRepository repositorio;
    private final ServiceClient cliente;
    private final CatalogoModelos catalogo;
    private final DocumentoProxy proxy;
    private final ConsultaDocumentoReal consulta;
    private final ExportacaoTxt txt;
    private final ExportacaoPdf pdf;

    public DocumentoService(DocumentoRepository repositorio, ServiceClient cliente,
            CatalogoModelos catalogo, DocumentoProxy proxy, ConsultaDocumentoReal consulta,
            ExportacaoTxt txt, ExportacaoPdf pdf) {
        this.repositorio = repositorio;
        this.cliente = cliente;
        this.catalogo = catalogo;
        this.proxy = proxy;
        this.consulta = consulta;
        this.txt = txt;
        this.pdf = pdf;
    }

    public Saida criar(String token, Entrada entrada) {
        var usuario = cliente.user(token);
        var modelo = catalogo.buscar(entrada.modeloId(), token);
        var documento = CriadorDocumento.para(entrada.tipo()).criar(modelo, entrada.titulo(), usuario);
        repositorio.salvar(documento);
        return saida(documento);
    }

    public List<Saida> listar(String token) {
        return repositorio.listar(cliente.user(token)).stream().map(this::saida).toList();
    }

    public Saida buscar(String token, String id) {
        return saida(proxy.para(token, "LER").buscar(id));
    }

    public Saida editar(String token, String id, Edicao entrada) {
        var anterior = proxy.para(token, "EDITAR").buscar(id);
        var documento = new Documento(id, entrada.titulo().strip(), entrada.conteudo(),
                anterior.proprietarioId(), anterior.modeloId(), anterior.modeloVersao(), anterior.criadoEm());
        repositorio.salvar(documento);
        return saida(documento);
    }

    public Arquivo exportar(String token, String id, String formato) throws IOException {
        var estrategia = switch (formato) {
            case "txt" -> txt;
            case "pdf" -> pdf;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use TXT ou PDF");
        };
        return estrategia.exportar(proxy.para(token, "LER").buscar(id));
    }


    public Object solicitacao(String token, String id, String operacao) {
        cliente.user(token);
        var documento = consulta.buscar(id);
        return cliente.call("http://acesso/interno/documentos/" + documento.id() + "/" + operacao,
                org.springframework.http.HttpMethod.POST,
                java.util.Map.of("proprietarioId", documento.proprietarioId()), token, Object.class);
    }

    private Saida saida(Documento d) {
        return new Saida(d.id(), d.titulo(), d.conteudo(), d.proprietarioId(),
                d.modeloId(), d.modeloVersao(), d.criadoEm());
    }
}
