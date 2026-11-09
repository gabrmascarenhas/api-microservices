# Registros de Decisões Arquiteturais

Os ADRs registram escolhas já implementadas na versão atual, seus motivos, alternativas e consequências. O status **Adotado no MVP** descreve o estado do código; não representa uma ata de votação da equipe nem a conclusão dos requisitos futuros.

| Registro | Decisão | Situação |
| --- | --- | --- |
| [ADR-001](001-divisao-em-microservicos.md) | Divisão do domínio em quatro microserviços | Adotado no MVP |
| [ADR-002](002-gateway-e-eureka.md) | Entrada pelo Gateway e descoberta pelo Eureka | Adotado no MVP |
| [ADR-003](003-arquitetura-em-camadas-e-dtos.md) | Organização interna em camadas e contratos com DTOs | Adotado no MVP |
| [ADR-004](004-armazenamento-em-memoria-no-mvp.md) | Armazenamento em memória na demonstração inicial | Substituído pelo ADR-006 |
| [ADR-005](005-autenticacao-e-autorizacao-do-mvp.md) | Sessão temporária e autorização pelo proprietário | Atualizado pelo ADR-006 |

| [ADR-006](006-persistencia-e-fluxos-da-demonstracao.md) | Persistência local, concessão de leitura e bloqueio de conta | Adotado na demonstração |

## Evolução dos registros

Cada nova decisão relevante recebe um arquivo numerado. Propostas ainda não implementadas devem ser identificadas como **Proposto**. Quando uma decisão for substituída, mantenha o registro anterior, altere sua situação para **Substituído pelo ADR-XXX** e vincule o novo registro. Não reescreva o histórico como se a decisão original nunca tivesse existido.

A implementação de demonstração usa persistência local e aplica os nove padrões nas operações descritas no README principal.
