# ADR-001: Divisão do domínio em quatro microserviços

- **Data:** 03/11/2026
- **Status:** Adotado no MVP
- **Escopo:** Versão inicial funcional do gerenciador de documentos.

## Contexto

O projeto exige no mínimo quatro microserviços independentes. A base anterior possuía serviços genéricos, sem responsabilidades associadas ao gerenciamento de documentos.

## Decisão

Adotar os serviços Identidade, Modelos, Documentos e Acesso, cada um como aplicação Spring Boot independente. Identidade gerencia usuários e sessões; Modelos mantém o catálogo; Documentos cria e consulta documentos; Acesso avalia a autorização de consulta individual. Gateway e Eureka são aplicações de infraestrutura e não compõem a contagem dos quatro serviços de domínio.

Os serviços trocam informações por APIs HTTP e referências por identificador, sem consultar diretamente os repositórios de outros serviços.

## Alternativas consideradas

- Aplicação monolítica: simplificaria a execução, mas não atenderia ao requisito obrigatório de microserviços.
- Manter serviços genéricos ou separar somente por camada técnica: dificultaria associar cada aplicação a uma responsabilidade do domínio.

## Consequências e limites

- A divisão torna os limites do domínio visíveis e permite evoluir cada aplicação separadamente.
- A execução exige vários processos, configuração de descoberta e tratamento de falhas de rede.
- A comunicação síncrona cria dependências de disponibilidade; autonomia de implantação não significa ausência de dependências funcionais.
- No MVP, cada processo possui seu próprio armazenamento em memória. Persistência durável ainda não foi implementada.

## Evidências no repositório

- `pom.xml`: agregação dos seis módulos.
- `identidade/`, `modelos/`, `documentos/` e `acesso/`: aplicações independentes.
- `testar_mvp.py`: fluxo integrado pelos serviços.
