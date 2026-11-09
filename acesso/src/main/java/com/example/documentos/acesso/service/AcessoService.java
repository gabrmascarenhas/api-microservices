package com.example.documentos.acesso.service;

import com.example.documentos.acesso.client.ServiceClient;
import com.example.documentos.acesso.dto.AcessoDtos.*;
import com.example.documentos.acesso.model.Decisao;
import com.example.documentos.acesso.repository.AcessoRepository;
import com.example.documentos.acesso.repository.ConcessaoRepository;
import com.example.documentos.acesso.repository.ConcessaoRepository.Solicitacao;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcessoService {
    private final ServiceClient cliente;
    private final AcessoRepository auditoria;
    private final ConcessaoRepository concessoes;
    private final CadeiaAutorizacao cadeia;

    public AcessoService(ServiceClient cliente, AcessoRepository auditoria, ConcessaoRepository concessoes) {
        this.cliente = cliente;
        this.auditoria = auditoria;
        this.concessoes = concessoes;
        this.cadeia = new CadeiaAutorizacao(cliente, concessoes::permiteLeitura);
    }

    public Resultado avaliar(String token, Pedido pedido) {
        var decisao = cadeia.avaliar(token, pedido);
        auditoria.registrar(new Decisao(decisao.usuario(), pedido.acao(),
                decisao.resultado().permitido(), Instant.now()));
        return decisao.resultado();
    }

    public Solicitacao solicitar(String token, String documento, String proprietario) {
        var usuario = cliente.user(token);
        if (usuario.equals(proprietario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O proprietário já possui acesso");
        }
        return concessoes.solicitar(documento, proprietario, usuario);
    }

    public List<Solicitacao> listar(String token, String documento, String proprietario) {
        exigirResponsavel(token, proprietario);
        return concessoes.listar(documento);
    }

    public Solicitacao decidir(String token, String documento, String proprietario,
                               String solicitante, boolean aprovar) {
        exigirResponsavel(token, proprietario);
        var decisao = concessoes.decidir(documento, solicitante, aprovar);
        if (decisao == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
        return decisao;
    }

    private void exigirResponsavel(String token, String proprietario) {
        var usuario = cliente.usuario(token);
        if (!usuario.id().equals(proprietario) && !"ADMINISTRADOR".equals(usuario.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o proprietário ou administrador decide o acesso");
        }
    }
}
