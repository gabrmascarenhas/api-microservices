const el = id => document.getElementById(id);
const mensagem = texto => { el('mensagem').textContent = texto; };
const valor = id => el(id).value;
let sessao;
let documento;

function encerrarInterface() {
    sessao = undefined;
    documento = undefined;
    el('autenticacao').hidden = false;
    el('sistema').hidden = true;
    el('detalhe').hidden = true;
    for (const id of ['conteudo', 'compartilhar']) el(id).textContent = '';
    for (const id of ['documentos', 'usuarios', 'solicitacoes']) el(id).replaceChildren();
    el('editar').reset();
}

async function api(caminho, { metodo = 'GET', corpo, arquivo = false } = {}) {
    const headers = {};
    if (sessao) headers.Authorization = 'Bearer ' + sessao.token;
    if (corpo !== undefined) headers['Content-Type'] = 'application/json';
    const resposta = await fetch('/api/' + caminho, {
        method: metodo,
        headers,
        body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
    if (!resposta.ok) {
        const dados = await resposta.json().catch(() => ({}));
        if (resposta.status === 401 && sessao) encerrarInterface();
        const erros = {400: 'Confira os campos.', 401: 'Sessão ou credenciais inválidas.',
            403: 'Acesso negado.', 404: 'Recurso não encontrado.', 409: 'Operação em conflito.',
            503: 'Serviço indisponível. Verifique o Eureka.'};
        throw Error(dados.erro || erros[resposta.status] || 'A operação falhou.');
    }
    if (arquivo) return resposta.blob();
    return resposta.status === 204 ? undefined : resposta.json();
}

function tarefa(acao) {
    return async evento => {
        evento.preventDefault();
        const botao = evento.submitter || (evento.currentTarget.tagName === 'BUTTON' ? evento.currentTarget : null);
        if (botao) botao.disabled = true;
        try {
            await acao();
        } catch (erro) {
            mensagem(erro.message);
        } finally {
            if (botao) botao.disabled = false;
        }
    };
}

function botao(texto, acao) {
    const item = document.createElement('button');
    item.textContent = texto;
    item.onclick = tarefa(acao);
    return item;
}

async function atualizar() {
    const [modelos, documentos] = await Promise.all([api('modelos'), api('documentos')]);
    for (const id of ['modelos', 'modeloRevisao']) {
        el(id).replaceChildren(...modelos.map(m => new Option(m.nome + ' (v' + m.versao + ')', m.id)));
    }
    el('documentos').replaceChildren();
    for (const d of documentos) {
        const item = document.createElement('li');
        item.append(botao(d.titulo, () => abrir(d.id)));
        el('documentos').append(item);
    }
}

async function abrir(id) {
    el('detalhe').hidden = true;
    documento = await api('documentos/' + encodeURIComponent(id));
    const dono = documento.proprietarioId === sessao.usuario.id;
    el('detalheTitulo').textContent = documento.titulo;
    el('conteudo').textContent = documento.conteudo;
    el('editar').hidden = !dono;
    el('editarTitulo').value = dono ? documento.titulo : '';
    el('editarConteudo').value = dono ? documento.conteudo : '';
    el('gestao').hidden = !dono && sessao.usuario.perfil !== 'ADMINISTRADOR';
    el('solicitacoes').replaceChildren();
    el('compartilhar').hidden = !dono;
    el('compartilhar').textContent = dono ? 'Código para compartilhar: ' + documento.id : '';
    el('detalhe').hidden = false;
    mensagem('Consulta autorizada.');
}

async function baixar(formato) {
    const blob = await api('documentos/' + documento.id + '/exportar?formato=' + formato, { arquivo: true });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'documento.' + formato;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}

async function solicitacoes() {
    const id = documento.id;
    const caminho = 'documentos/' + id + '/solicitacoes';
    const pedidos = await api(caminho);
    el('solicitacoes').replaceChildren();
    for (const pedido of pedidos) {
        const item = document.createElement('li');
        item.textContent = pedido.solicitanteId + ' · ' + pedido.status + ' ';
        for (const [rotulo, aprovar] of [['Aprovar leitura', true], ['Recusar ou revogar', false]]) {
            item.append(botao(rotulo, async () => {
                await api(caminho + '/' + pedido.solicitanteId + '?aprovar=' + aprovar, { metodo: 'PUT' });
                await solicitacoes();
            }));
        }
        el('solicitacoes').append(item);
    }
}

async function usuarios() {
    const lista = await api('identidade/usuarios');
    el('usuarios').replaceChildren();
    for (const u of lista) {
        const item = document.createElement('li');
        item.textContent = u.nome + ' · ' + u.email + ' · ' + u.perfil + (u.bloqueado ? ' · Bloqueado' : ' ');
        if (u.perfil !== 'ADMINISTRADOR') {
            item.append(botao(u.bloqueado ? 'Desbloquear' : 'Bloquear', async () => {
                await api('identidade/usuarios/' + u.id + '/bloqueio?bloqueado=' + !u.bloqueado, { metodo: 'PUT' });
                await usuarios();
            }));
        }
        el('usuarios').append(item);
    }
}

el('cadastro').onsubmit = tarefa(async () => {
    await api('identidade/usuarios', { metodo: 'POST', corpo: {
        nome: valor('nome'), email: valor('novoEmail'), senha: valor('novaSenha'),
    }});
    el('email').value = valor('novoEmail');
    el('cadastro').reset();
    mensagem('Conta cadastrada. Entre com email e senha.');
});
el('login').onsubmit = tarefa(async () => {
    sessao = await api('identidade/login', { metodo: 'POST', corpo: {
        email: valor('email'), senha: valor('senha'),
    }});
    el('senha').value = '';
    el('usuario').textContent = sessao.usuario.nome + ' · ' + sessao.usuario.perfil;
    el('admin').hidden = sessao.usuario.perfil !== 'ADMINISTRADOR';
    el('autenticacao').hidden = true;
    el('sistema').hidden = false;
    await atualizar();
    mensagem('Acesso realizado.');
});
el('sair').onclick = tarefa(async () => {
    await api('identidade/sair', { metodo: 'POST' });
    encerrarInterface();
    mensagem('Sessão encerrada.');
});
el('modelo').onsubmit = tarefa(async () => {
    await api('modelos', { metodo: 'POST', corpo: {
        nome: valor('modeloNome'), conteudo: valor('modeloConteudo'),
    }});
    el('modelo').reset();
    await atualizar();
    mensagem('Modelo cadastrado.');
});
el('revisao').onsubmit = tarefa(async () => {
    await api('modelos/' + valor('modeloRevisao'), { metodo: 'PUT', corpo: {
        conteudo: valor('revisaoConteudo'),
    }});
    el('revisao').reset();
    await atualizar();
    mensagem('Nova versão publicada.');
});
el('documento').onsubmit = tarefa(async () => {
    const criado = await api('documentos', { metodo: 'POST', corpo: {
        titulo: valor('titulo'), modeloId: valor('modelos'), tipo: valor('tipo'),
    }});
    el('titulo').value = '';
    await atualizar();
    await abrir(criado.id);
    mensagem('Documento criado a partir do modelo.');
});
el('editar').onsubmit = tarefa(async () => {
    const id = documento.id;
    await api('documentos/' + id, { metodo: 'PUT', corpo: {
        titulo: valor('editarTitulo'), conteudo: valor('editarConteudo'),
    }});
    await atualizar();
    await abrir(id);
    mensagem('Documento atualizado.');
});
el('compartilhado').onsubmit = tarefa(() => abrir(valor('codigo').trim()));
el('pedir').onclick = tarefa(async () => {
    const id = valor('codigo').trim();
    if (!id) throw Error('Informe o código do documento.');
    const pedido = await api('documentos/' + encodeURIComponent(id) + '/solicitacoes', { metodo: 'POST' });
    mensagem('Solicitação: ' + pedido.status);
});
el('atualizar').onclick = tarefa(atualizar);
el('baixarTxt').onclick = tarefa(() => baixar('txt'));
el('baixarPdf').onclick = tarefa(() => baixar('pdf'));
el('verSolicitacoes').onclick = tarefa(solicitacoes);
el('verUsuarios').onclick = tarefa(usuarios);
