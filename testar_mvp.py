"""Verifica o fluxo integrado do MVP com os serviços já em execução."""

import json
import urllib.error
import urllib.request
import uuid

URL_GATEWAY = "http://localhost:8080"
URL_REGISTROS_EUREKA = "http://localhost:8761/eureka/apps"


def requisicao(
    caminho: str,
    metodo: str = "GET",
    dados: dict | None = None,
    token: str | None = None,
    status_esperado: int = 200,
):
    """Faz uma chamada pelo Gateway e confere o código HTTP retornado."""
    cabecalhos = {"Content-Type": "application/json"}
    if token:
        cabecalhos["Authorization"] = f"Bearer {token}"

    corpo = json.dumps(dados).encode("utf-8") if dados is not None else None
    pedido = urllib.request.Request(
        URL_GATEWAY + caminho,
        data=corpo,
        headers=cabecalhos,
        method=metodo,
    )

    try:
        with urllib.request.urlopen(pedido, timeout=15) as resposta:
            status_recebido = resposta.status
            conteudo = resposta.read().decode("utf-8")
    except urllib.error.HTTPError as erro:
        # Respostas de erro também são esperadas nos cenários de rejeição.
        status_recebido = erro.code
        conteudo = erro.read().decode("utf-8")

    assert status_recebido == status_esperado, (
        f"{metodo} {caminho}: esperado HTTP {status_esperado}, "
        f"recebido HTTP {status_recebido}. Resposta: {conteudo}"
    )

    try:
        return json.loads(conteudo)
    except json.JSONDecodeError:
        # A página inicial retorna HTML, enquanto as APIs retornam JSON.
        return conteudo


def verificar_infraestrutura() -> None:
    """Confirma os cinco registros no Eureka e a interface no Gateway."""
    with urllib.request.urlopen(URL_REGISTROS_EUREKA, timeout=10) as resposta:
        registros = resposta.read().decode("utf-8")

    for nome in ("IDENTIDADE", "MODELOS", "DOCUMENTOS", "ACESSO", "GATEWAY"):
        assert f"<name>{nome}</name>" in registros, f"Registro ausente: {nome}"

    assert "Gerenciamento de Documentos" in requisicao("/")


def verificar_validacao_inicial() -> None:
    """Rejeita a consulta sem sessão e um cadastro com campos inválidos."""
    requisicao("/api/modelos", status_esperado=401)
    requisicao(
        "/api/identidade/usuarios",
        metodo="POST",
        dados={"nome": "", "email": "invalido", "senha": "x"},
        status_esperado=400,
    )


def cadastrar_e_autenticar_usuario(nome: str, sufixo: str) -> tuple[dict, str]:
    """Cria uma conta, testa duplicidade e senha incorreta e obtém a sessão."""
    cadastro = {
        "nome": nome,
        "email": f"{nome}{sufixo}@example.com",
        "senha": "MvpTeste2026!",
    }
    usuario = requisicao(
        "/api/identidade/usuarios",
        metodo="POST",
        dados=cadastro,
        status_esperado=201,
    )
    assert "senha" not in str(usuario).lower(), "A API não deve expor a senha."

    requisicao(
        "/api/identidade/usuarios",
        metodo="POST",
        dados=cadastro,
        status_esperado=409,
    )
    requisicao(
        "/api/identidade/login",
        metodo="POST",
        dados={"email": cadastro["email"], "senha": "errada"},
        status_esperado=401,
    )
    sessao = requisicao(
        "/api/identidade/login",
        metodo="POST",
        dados={"email": cadastro["email"], "senha": cadastro["senha"]},
    )
    return usuario, sessao["token"]


def cadastrar_modelo(token: str, sufixo: str) -> dict:
    """Cadastra um modelo e confirma que seu nome não pode ser duplicado."""
    nome = f"Contrato {sufixo}"
    modelo = requisicao(
        "/api/modelos",
        metodo="POST",
        dados={"nome": nome, "conteudo": "Conteúdo do modelo original"},
        token=token,
        status_esperado=201,
    )
    requisicao(
        "/api/modelos",
        metodo="POST",
        dados={"nome": nome, "conteudo": "Duplicado"},
        token=token,
        status_esperado=409,
    )
    return modelo


def criar_documento(token: str, usuario: dict, modelo: dict) -> dict:
    """Cria um documento e confere conteúdo, versão e proprietário."""
    documento = requisicao(
        "/api/documentos",
        metodo="POST",
        dados={"titulo": "Meu contrato", "modeloId": modelo["id"]},
        token=token,
        status_esperado=201,
    )
    assert documento["conteudo"] == modelo["conteudo"]
    assert documento["modeloVersao"] == 1
    assert documento["proprietarioId"] == usuario["id"]
    return documento


def verificar_acesso_documento(
    documento: dict,
    token_proprietario: str,
    token_outro_usuario: str,
) -> None:
    """Permite acesso ao dono e rejeita outro usuário ou um token inválido."""
    caminho = f"/api/documentos/{documento['id']}"

    assert len(requisicao("/api/documentos", token=token_proprietario)) == 1
    assert requisicao(caminho, token=token_proprietario)["id"] == documento["id"]
    assert requisicao("/api/documentos", token=token_outro_usuario) == []

    requisicao(caminho, token=token_outro_usuario, status_esperado=403)
    requisicao(caminho, token="invalido", status_esperado=401)


def verificar_rotas_restritas(token: str, usuario: dict) -> None:
    """Rejeita um modelo inexistente e não expõe as rotas internas no Gateway."""
    requisicao(
        "/api/documentos",
        metodo="POST",
        dados={"titulo": "Modelo ausente", "modeloId": str(uuid.uuid4())},
        token=token,
        status_esperado=404,
    )
    requisicao("/interno/sessao", token=token, status_esperado=404)
    requisicao(
        "/interno/autorizar",
        metodo="POST",
        dados={"proprietarioId": usuario["id"], "acao": "LER"},
        token=token,
        status_esperado=404,
    )


def main() -> None:
    # O sufixo evita conflito com usuários e modelos de execuções anteriores.
    sufixo = uuid.uuid4().hex[:8]

    verificar_infraestrutura()
    verificar_validacao_inicial()

    usuario, token = cadastrar_e_autenticar_usuario("ana", sufixo)
    _, token_outro_usuario = cadastrar_e_autenticar_usuario("bruno", sufixo)
    modelo = cadastrar_modelo(token, sufixo)
    documento = criar_documento(token, usuario, modelo)

    verificar_acesso_documento(documento, token, token_outro_usuario)
    verificar_rotas_restritas(token, usuario)

    print(
        "OK: Eureka com cinco registros, interface e fluxo completo pelo Gateway; "
        "validação 400, autenticação 401, autorização 403, "
        "inexistência 404 e duplicidade 409."
    )


if __name__ == "__main__":
    main()
