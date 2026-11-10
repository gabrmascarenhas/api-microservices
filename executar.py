"""Compila e inicia o sistema. Use Ctrl+C para encerrar as seis aplicações."""

import os
import re
import shutil
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import TextIO

RAIZ_PROJETO = Path(__file__).resolve().parent
PASTA_LOGS = RAIZ_PROJETO / ".execucao"

# A ordem garante que o Eureka seja iniciado antes dos seus clientes.
APLICACOES = (
    ("eureka-server", 8761),
    ("identidade", 8661),
    ("modelos", 8662),
    ("acesso", 8664),
    ("documentos", 8663),
    ("gateway", 8080),
)


def localizar_java() -> Path:
    """Localiza o Java e verifica a versão compatível com o projeto."""
    candidatos = []
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        candidatos.append(Path(java_home) / "bin" / "java")

    java_home_macos = Path("/usr/libexec/java_home")
    if java_home_macos.is_file():
        instalado = subprocess.run(
            [str(java_home_macos), "-F", "-v", "21"],
            capture_output=True,
            text=True,
        )
        if instalado.returncode == 0:
            candidatos.append(Path(instalado.stdout.strip()) / "bin" / "java")

    if java_path := shutil.which("java"):
        candidatos.append(Path(java_path))

    for java in candidatos:
        try:
            resultado = subprocess.run(
                [str(java), "--version"], capture_output=True, text=True
            )
        except OSError:
            continue
        versao = re.search(r"(?:openjdk|java) (\d+)", resultado.stdout)
        if resultado.returncode == 0 and versao and 21 <= int(versao.group(1)) <= 25:
            return java.resolve()

    sys.exit("Configure JAVA_HOME com um JDK 21 a 25 (recomendado: JDK 21).")


def verificar_portas() -> None:
    """Evita iniciar o conjunto quando uma das portas já está ocupada."""
    for nome, porta in APLICACOES:
        with socket.socket() as conexao:
            conexao.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            try:
                conexao.bind(("127.0.0.1", porta))
            except OSError:
                sys.exit(
                    f"Porta {porta} ocupada ({nome}). "
                    "Encerre a aplicação correspondente antes de iniciar."
                )


def compilar_projeto(java: Path) -> None:
    """Usa o Maven Wrapper para compilar, executar testes e gerar os JARs."""
    subprocess.run(
        ["sh", str(RAIZ_PROJETO / "mvnw"), "-B", "-q", "package"],
        cwd=RAIZ_PROJETO,
        env={**os.environ, "JAVA_HOME": str(java.parent.parent)},
        check=True,
    )


def iniciar_aplicacao(
    java: Path,
    nome: str,
) -> tuple[subprocess.Popen, TextIO]:
    """Inicia um JAR e direciona sua saída para um arquivo de log próprio."""
    arquivo_jar = RAIZ_PROJETO / nome / "target" / f"{nome}-1.0.0.jar"
    if not arquivo_jar.exists():
        raise RuntimeError(f"Compile o projeto antes: {arquivo_jar}")

    arquivo_log = (PASTA_LOGS / f"{nome}.log").open("w", encoding="utf-8")
    try:
        processo = subprocess.Popen(
            [str(java), "-Xmx192m", "-jar", str(arquivo_jar)],
            cwd=RAIZ_PROJETO,
            stdout=arquivo_log,
            stderr=subprocess.STDOUT,
        )
    except OSError:
        arquivo_log.close()
        raise

    return processo, arquivo_log


def aguardar_aplicacao(
    nome: str,
    porta: int,
    processo: subprocess.Popen,
) -> None:
    """Aguarda até dois minutos pelo endpoint de saúde da aplicação."""
    prazo = time.monotonic() + 120
    endereco = f"http://localhost:{porta}/actuator/health"
    caminho_log = PASTA_LOGS / f"{nome}.log"

    while True:
        if processo.poll() is not None:
            raise RuntimeError(f"{nome} encerrou; consulte {caminho_log}")

        try:
            with urllib.request.urlopen(endereco, timeout=2) as resposta:
                if resposta.status == 200:
                    return
        except (urllib.error.URLError, OSError):
            # A conexão pode falhar enquanto o Spring Boot ainda está iniciando.
            pass

        if time.monotonic() > prazo:
            raise RuntimeError(
                f"Tempo excedido ao iniciar {nome}; consulte {caminho_log}"
            )

        time.sleep(1)


def encerrar_aplicacoes(
    processos: list[subprocess.Popen],
    arquivos_log: list[TextIO],
) -> None:
    """Encerra somente os processos criados por esta execução e fecha os logs."""
    for processo in reversed(processos):
        if processo.poll() is None:
            processo.terminate()

    for processo in processos:
        try:
            processo.wait(timeout=15)
        except subprocess.TimeoutExpired:
            processo.kill()
            processo.wait()

    for arquivo_log in arquivos_log:
        arquivo_log.close()


def main() -> None:
    java = localizar_java()
    verificar_portas()
    PASTA_LOGS.mkdir(exist_ok=True)

    processos = []
    arquivos_log = []

    try:
        # --sem-build permite reutilizar os JARs da última compilação.
        if "--sem-build" not in sys.argv:
            compilar_projeto(java)

        for nome, porta in APLICACOES:
            processo, arquivo_log = iniciar_aplicacao(java, nome)
            processos.append(processo)
            arquivos_log.append(arquivo_log)

            aguardar_aplicacao(nome, porta, processo)
            print(f"{nome} iniciado na porta {porta}", flush=True)

        print(
            "Aguarde alguns segundos pela descoberta. "
            "Sistema: http://localhost:8080 | Eureka: http://localhost:8761",
            flush=True,
        )

        # Mantém o iniciador aberto para permitir encerrar tudo com Ctrl+C.
        while all(processo.poll() is None for processo in processos):
            time.sleep(1)

        raise RuntimeError("Uma aplicação encerrou. Consulte .execucao/*.log")
    except KeyboardInterrupt:
        print("\nEncerrando aplicações...")
    finally:
        # Também libera os processos se houver falha durante a inicialização.
        encerrar_aplicacoes(processos, arquivos_log)


if __name__ == "__main__":
    main()
