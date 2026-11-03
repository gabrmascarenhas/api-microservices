# ADR-004: Armazenamento em memória na demonstração inicial

- **Data:** 03/11/2026
- **Status:** Adotado no MVP
- **Escopo:** Versão inicial funcional do gerenciador de documentos.

## Contexto

A entrega inicial precisa demonstrar a viabilidade da arquitetura e um fluxo funcional. A configuração de bancos, migrações e armazenamento de arquivos ampliaria o trabalho necessário para essa primeira demonstração.

## Decisão

Manter usuários, modelos, documentos, sessões e registros de decisões de acesso em estruturas de memória privadas de cada processo. Encapsular o acesso aos dados nos Repositories; as sessões são mantidas pelo serviço Identidade.

Esta decisão é limitada ao MVP. A persistência privada por serviço descrita no Plano de Trabalho permanece uma evolução planejada, não uma funcionalidade concluída. A escolha e implantação dos bancos e das migrações deverão ser registradas em um ADR posterior, que poderá substituir esta decisão.

## Alternativas consideradas

- PostgreSQL desde a primeira versão: proporcionaria durabilidade, mas exigiria configuração, esquema e migrações.
- Banco embarcado: reduziria a infraestrutura inicial, mas ainda exigiria mapear o modelo e definir o ciclo de vida dos dados.

## Consequências e limites

- O MVP inicia sem banco externo e permite exercitar o fluxo entre os serviços.
- Reiniciar um serviço apaga seus dados locais; reiniciar Identidade também invalida as sessões.
- Não há garantia de consistência após reinícios parciais nem sincronização de dados entre réplicas.
- Esta versão não atende ao objetivo de persistência durável do projeto final.
- Os testes integrados criam seus próprios registros e não dependem de dados previamente cadastrados.

## Evidências no repositório

- `*/src/main/java/**/repository/`: repositórios em memória.
- `identidade/src/main/java/com/example/documentos/identidade/service/IdentidadeService.java`: sessões temporárias.
- `README.md`: limites atuais da entrega.
