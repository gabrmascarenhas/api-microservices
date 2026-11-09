# ADR 006 — Persistência e fluxos da demonstração

**Status:** adotado na versão de demonstração.

## Contexto

O MVP original descartava os registros ao encerrar a aplicação e autorizava leitura apenas ao proprietário. A demonstração precisa verificar recuperação de dados, edição, aprovação de leitura e exportação.

## Decisão

Cada serviço grava seus registros em um arquivo JSON próprio, substituído atomicamente após uma escrita bem-sucedida. Contas, modelos, documentos e concessões são recarregados na inicialização. Sessões permanecem em memória, com expiração, logout e revogação no bloqueio de conta.

O compartilhamento usa o identificador do documento. A posse desse identificador permite solicitar leitura. A concessão exige aprovação do proprietário ou de um administrador, pode ser revogada e permite consulta e exportação. A edição continua reservada ao proprietário.

## Alternativas e consequências

Um banco relacional por serviço foi considerado para produção. Os arquivos permitem executar a apresentação com Java e Maven, sem infraestrutura adicional de banco. Essa opção é adequada a uma instância por serviço e um volume privado por instância. Uma implantação com réplicas ou consultas complexas exigirá outro armazenamento.

Este registro atualiza as decisões de persistência e autorização dos ADRs 004 e 005. Execute `testar_fluxo_completo.py` antes e depois de reiniciar para verificar as garantias da demonstração.
