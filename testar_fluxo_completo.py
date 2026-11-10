"""Valida o fluxo evoluído pelo Gateway e confere dados após reiniciar."""

import argparse
import json
import os
from pathlib import Path
import urllib.request
import uuid

from testar_mvp import requisicao, verificar_infraestrutura

SENHA = "Senha-de-teste-123"


def cadastrar(email):
    return requisicao(
        "/api/identidade/usuarios",
        "POST",
        {"nome": "Usuário de teste", "email": email, "senha": SENHA},
        status_esperado=201,
    )


def login(email, senha=SENHA):
    return requisicao(
        "/api/identidade/login", "POST", {"email": email, "senha": senha}
    )["token"]


def baixar(caminho, formato, token):
    pedido = urllib.request.Request(
        f"http://localhost:8080{caminho}/exportar?formato={formato}",
        headers={"Authorization": f"Bearer {token}"},
    )
    with urllib.request.urlopen(pedido, timeout=20) as resposta:
        return resposta.headers["Content-Type"], resposta.read()


def validar_fluxo(estado):
    verificar_infraestrutura()
    identificador = uuid.uuid4().hex[:10]
    email_dono = f"dono-{identificador}@example.com"
    email_leitor = f"leitor-{identificador}@example.com"
    cadastrar(email_dono)
    leitor = cadastrar(email_leitor)
    dono, outro = login(email_dono), login(email_leitor)
    modelo = requisicao(
        "/api/modelos",
        "POST",
        {"nome": f"Modelo {identificador}", "conteudo": "Versão inicial"},
        dono,
        201,
    )
    entrada = {"titulo": "Relatório de demonstração", "modeloId": modelo["id"]}
    relatorio = requisicao(
        "/api/documentos", "POST", {**entrada, "tipo": "RELATORIO"}, dono, 201
    )
    contrato = requisicao(
        "/api/documentos", "POST", {**entrada, "tipo": "CONTRATO"}, dono, 201
    )
    assert relatorio["conteudo"] == "Versão inicial"
    assert contrato["conteudo"].startswith("Versão inicial")
    assert "Assinaturas:" in contrato["conteudo"]
    assert contrato["id"] != relatorio["id"]

    revisao = requisicao(
        f"/api/modelos/{modelo['id']}", "PUT", {"conteudo": "Versão revisada"}, dono
    )
    assert revisao["versao"] == 2
    novo = requisicao("/api/documentos", "POST", entrada, dono, 201)
    assert novo["modeloVersao"] == 2 and novo["conteudo"] == "Versão revisada"
    caminho = f"/api/documentos/{relatorio['id']}"
    assert requisicao(caminho, token=dono)["conteudo"] == "Versão inicial"
    requisicao(caminho, token=outro, status_esperado=403)
    requisicao(caminho + "/exportar?formato=pdf", token=outro, status_esperado=403)
    pedido = requisicao(caminho + "/solicitacoes", "POST", token=outro)
    assert pedido["status"] == "PENDENTE"
    assert (
        requisicao(caminho + "/solicitacoes", token=dono)[0]["solicitanteId"]
        == leitor["id"]
    )
    decisao = f"{caminho}/solicitacoes/{leitor['id']}"
    requisicao(decisao + "?aprovar=true", "PUT", token=outro, status_esperado=403)
    requisicao(decisao + "?aprovar=true", "PUT", token=dono)
    assert requisicao(caminho, token=outro)["id"] == relatorio["id"]
    edicao = {"titulo": "Relatório revisado", "conteudo": "Ação, versão e aprovação."}
    requisicao(caminho, "PUT", edicao, outro, 403)
    revisado = requisicao(caminho, "PUT", edicao, dono)
    assert revisado["modeloVersao"] == 1 and revisado["conteudo"] == edicao["conteudo"]
    tipo_txt, txt = baixar(caminho, "txt", outro)
    tipo_pdf, pdf = baixar(caminho, "pdf", outro)
    assert tipo_txt.startswith("text/plain") and edicao["conteudo"] in txt.decode()
    assert tipo_pdf == "application/pdf" and pdf.startswith(b"%PDF-")
    requisicao(decisao + "?aprovar=false", "PUT", token=dono)
    requisicao(caminho, token=outro, status_esperado=403)
    requisicao(decisao + "?aprovar=true", "PUT", token=dono)

    email_admin = os.environ.get("ADMIN_EMAIL")
    senha_admin = os.environ.get("ADMIN_SENHA")
    if email_admin and senha_admin:
        admin = login(email_admin, senha_admin)
        bloqueio = f"/api/identidade/usuarios/{leitor['id']}/bloqueio"
        requisicao(bloqueio + "?bloqueado=true", "PUT", token=dono, status_esperado=403)
        requisicao(bloqueio + "?bloqueado=true", "PUT", token=admin)
        requisicao(caminho, token=outro, status_esperado=401)
        requisicao(bloqueio + "?bloqueado=false", "PUT", token=admin)
        assert (
            requisicao(caminho, token=login(email_leitor))["conteudo"]
            == edicao["conteudo"]
        )
        print("OK: perfil administrativo, bloqueio e invalidação da sessão anterior.")

    requisicao("/api/identidade/sair", "POST", token=dono, status_esperado=204)
    requisicao(caminho, token=dono, status_esperado=401)
    estado.write_text(
        json.dumps(
            {
                "dono": email_dono,
                "leitor": email_leitor,
                "documento": caminho,
                "modelo": modelo["id"],
                "conteudo": edicao["conteudo"],
            }
        )
    )
    print("OK: criação por tipo, versão de modelo, edição, acesso, TXT/PDF e logout.")


def validar_reinicio(estado):
    dados = json.loads(estado.read_text())
    dono, leitor = login(dados["dono"]), login(dados["leitor"])
    assert requisicao("/api/modelos/" + dados["modelo"], token=dono)["versao"] == 2
    assert requisicao(dados["documento"], token=leitor)["conteudo"] == dados["conteudo"]
    print("OK: contas, modelo, documento e concessão recuperados após reinício.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--estado", type=Path, required=True)
    parser.add_argument("--apos-reinicio", action="store_true")
    args = parser.parse_args()
    if args.apos_reinicio:
        validar_reinicio(args.estado)
    else:
        validar_fluxo(args.estado)


if __name__ == "__main__":
    main()
